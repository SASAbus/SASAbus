package it.sasabz.android.sasabus.timetable

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.davale.sasabus.core.inflate
import it.sasabz.android.sasabus.R
import it.sasabz.android.sasabus.data.network.model.Timetable
import kotlinx.android.synthetic.main.list_item_timetable.view.*
import java.io.File

internal class TimetableAdapter(private val mContext: Context, private val mItems: List<Timetable>) :
        RecyclerView.Adapter<TimetableAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder = ViewHolder(viewGroup)

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) = viewHolder.bind(mItems[position])

    override fun getItemCount(): Int = mItems.size

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            parent.inflate(R.layout.list_item_timetable)), View.OnClickListener {

        fun bind(item: Timetable) = with(itemView) {
            itemView.setOnClickListener(this@ViewHolder)

            val city = if (item.city == "ME") mContext.getString(R.string.merano)
            else itemView.context.getString(R.string.bolzano)


            val validity = mContext.getString(R.string.timetable_validity_format,
                    item.validFrom, item.validTo)

            list_timetable_line.text = mContext.getString(R.string.line_format, item.line)
            list_timetable_munic.text = city

            list_timetable_validity.text = validity
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            if (position == RecyclerView.NO_POSITION) return

            val item = mItems[position]

            val root = com.davale.sasabus.core.util.IOUtils.getTimetablesDir(mContext)
            val name = String.format(TimetableActivity.FILE_SCHEMA,
                    item.line, item.validFrom, item.validTo).replace("/", "_")

            val fullFile = File(root, name)

            if (fullFile.exists()) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.fromFile(fullFile), "application/pdf")
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY

                val chooser = Intent.createChooser(intent, mContext.getString(R.string.timetable_open))

                mContext.startActivity(chooser)
            } else {
                (mContext as? TimetableActivity)?.downloadTimetable(item.name, fullFile)
            }
        }
    }
}