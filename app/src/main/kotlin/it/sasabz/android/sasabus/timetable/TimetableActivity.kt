package it.sasabz.android.sasabus.timetable

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import it.sasabz.android.sasabus.Config
import it.sasabz.android.sasabus.data.network.response.TimetableResponse
import it.sasabz.android.sasabus.data.network.model.Timetable
import it.sasabz.android.sasabus.R
import it.sasabz.android.sasabus.data.network.NetUtils
import it.sasabz.android.sasabus.data.network.RestClient
import it.sasabz.android.sasabus.data.network.api.TimetableApi
import it.sasabz.android.sasabus.data.network.rest.Endpoint
import it.sasabz.android.sasabus.ui.BaseActivity
import it.sasabz.android.sasabus.util.AnalyticsHelper
import it.sasabz.android.sasabus.util.Utils
import it.sasabz.android.sasabus.util.rx.NextObserver
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Okio
import rx.Observable
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

import kotlinx.android.synthetic.main.activity_timetable.*
import kotlinx.android.synthetic.main.include_error_general.*
import kotlinx.android.synthetic.main.include_error_wifi.*

/**
 * Displays all the available timetables, inclusive a map of bz/me, in a list so the user
 * can open the timetables with an external pdf reader.
 * Handles downloading of timetables by using [RxJava][Observable] and updating them if a
 * timetable change occurs.
 *
 * @author Alex Lardschneider
 * @author David Dejori
 */
class TimetableActivity : BaseActivity() {

    /**
     * List to hold the timetable items.
     */
    private var mItems: ArrayList<Timetable>? = null

    /**
     * The [RecyclerView] adapter.
     */
    private var mAdapter: TimetableAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_timetable)

        AnalyticsHelper.sendScreenView("TimetableActivity")

        mItems = ArrayList<Timetable>()
        mAdapter = TimetableAdapter(this, mItems!!)

        refresh.setColorSchemeResources(*Config.REFRESH_COLORS)
        refresh.setOnRefreshListener { parseData() }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = mAdapter
        recycler.setHasFixedSize(true)

        parseData()
    }

    override fun getNavItem(): Int = BaseActivity.NAVDRAWER_ITEM_TIMETABLES

    private fun parseData() {
        if (!NetUtils.isOnline(this)) {
            error_general.visibility = View.GONE
            error_wifi.visibility = View.VISIBLE

            mItems!!.clear()
            mAdapter!!.notifyDataSetChanged()

            refresh.isRefreshing = false

            return
        }

        refresh.isRefreshing = true

        val timetableApi = RestClient.ADAPTER!!.create(TimetableApi::class.java)
        timetableApi.all()
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NextObserver<TimetableResponse>() {
                    override fun onNext(response: TimetableResponse) {
                        mItems!!.clear()
                        mItems!!.addAll(response.timetables)

                        mAdapter!!.notifyDataSetChanged()

                        error_general.visibility = View.GONE
                        error_wifi.visibility = View.GONE

                        refresh.isRefreshing = false

                        Thread {
                            deleteOldTimetables(response)
                        }.start()
                    }

                    override fun onError(e: Throwable) {
                        Utils.logException(e)

                        mItems!!.clear()
                        mAdapter!!.notifyDataSetChanged()

                        error_general.visibility = View.VISIBLE
                        error_wifi.visibility = View.GONE

                        refresh.isRefreshing = false
                    }
                })
    }

    private fun deleteOldTimetables(response: TimetableResponse) {
        val file = com.davale.sasabus.core.util.IOUtils.getTimetablesDir(this)
        val files = file.listFiles()

        val toDelete = arrayListOf<File>()

        if (files == null || files.isEmpty()) {
            Timber.i("No timetables to delete as directory is empty")
            return
        }

        for (timetable in files) {
            val found = response.timetables
                    .map { String.format(FILE_SCHEMA, it.title, it.validFrom, it.validTo).replace("/", "_") }
                    .any { timetable.name == it }

            if (!found) {
                toDelete.add(timetable)
            }
        }

        Timber.w("Deleting %d old timetables...", toDelete.size)

        for (delete in toDelete) {
            Timber.i("Deleting '%s'...", delete.name)
            if (delete.delete()) {
                Timber.e("Deletion of '%s' failed", delete.name)
            }
        }
    }


    fun downloadTimetable(name: String, fullFile: File) {
        val progress = ProgressDialog(this, R.style.DialogStyle)
        progress.setMessage(getString(R.string.timetable_download))
        progress.setCancelable(false)
        progress.isIndeterminate = true
        progress.show()

        downloadFile(name, fullFile)
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: Observer<Int> {

                    override fun onNext(p0: Int) = Unit

                    override fun onError(error: Throwable) {
                        Utils.logException(error)

                        progress.dismiss()

                        val snackbar = Snackbar.make(mainContent!!, R.string.snackbar_timetable_error, Snackbar.LENGTH_LONG)
                        snackbar.setActionTextColor(ContextCompat.getColor(this@TimetableActivity, R.color.primary))

                        snackbar.show()
                    }

                    override fun onCompleted() {
                        progress.dismiss()

                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(Uri.fromFile(fullFile), "application/pdf")
                        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY

                        val chooser = Intent.createChooser(intent, getString(R.string.timetable_open))

                        startActivity(chooser)
                    }
                })
    }

    private fun downloadFile(name: String, fullFile: File): Observable<Int> = Observable.create<Int> { subscriber ->
        try {
            Timber.e("Starting download of timetable '%s'", name)

            if (!fullFile.parentFile.exists()) {
                Timber.i("Plan data directory doesn't exist, creating...")

                if (!fullFile.parentFile.mkdirs()) {
                    subscriber.onError(IOException("Cannot create directory file."))
                    return@create
                }
            }

            val client = OkHttpClient.Builder()
                    .readTimeout(2, TimeUnit.MINUTES)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .build()

            val url = Endpoint.API + Endpoint.TIMETABLE_PDF + name
            Timber.i("Timetable url is: %s", url)

            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            val body = response.body()
            val contentLength = body.contentLength()
            val source = body.source()

            Timber.e("Content-Length: %d", contentLength)

            val sink = Okio.buffer(Okio.sink(fullFile))

            var totalBytesRead: Long = 0

            while (true) {
                val bytesRead = source.read(sink.buffer(), 1024)

                if (bytesRead == -1L) {
                    break
                }

                totalBytesRead += bytesRead
            }

            Timber.e("Finished reading %d bytes", totalBytesRead)

            sink.writeAll(source)
            sink.close()

            body.close()

            subscriber.onCompleted()
        } catch (e: Exception) {
            subscriber.onError(e)
        }
    }


    companion object {

        val FILE_SCHEMA = "TIMETABLE_%s_FROM_%s_TO_%s.pdf"
    }
}
