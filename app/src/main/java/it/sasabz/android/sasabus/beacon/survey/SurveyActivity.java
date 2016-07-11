package it.sasabz.android.sasabus.beacon.survey;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.network.NetUtils;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.SurveyApi;
import it.sasabz.android.sasabus.realm.user.Survey;
import it.sasabz.android.sasabus.realm.user.Trip;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.ReportHelper;
import it.sasabz.android.sasabus.util.Utils;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.jakewharton.rxbinding.widget.RxRatingBar;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SurveyActivity extends AppCompatActivity {

    private static final String TAG = "SurveyActivity";

    @BindView(R.id.survey_form_email) EditText formEmail;
    @BindView(R.id.survey_form_message) EditText formMessage;
    @BindView(R.id.survey_form_email_layout) TextInputLayout formEmailLayout;
    @BindView(R.id.survey_rating_bar) RatingBar ratingBar;

    private String[] ratingTexts;

    private final Realm realm = Realm.getDefaultInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_survey);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ratingTexts = new String[] {
                getString(R.string.survey_rating_awful),
                getString(R.string.survey_rating_poor),
                getString(R.string.survey_rating_average),
                getString(R.string.survey_rating_good),
                getString(R.string.survey_rating_excellent)
        };

        Intent intent = getIntent();
        if (!intent.hasExtra(Config.EXTRA_TRIP_HASH)) {
            finish();
            return;
        }

        String tripHash = intent.getStringExtra(Config.EXTRA_TRIP_HASH);

        Realm realm = Realm.getDefaultInstance();

        Trip trip = realm.where(Trip.class).equalTo("hash", tripHash).findFirst();

        if (trip == null) {
            finish();
            return;
        }

        TextView ratingSubtitle = (TextView) findViewById(R.id.survey_rating_bar_subtitle);

        Button submit = (Button) findViewById(R.id.survey_submit);
        submit.setOnClickListener(v -> sendSurvey(trip));

        ratingBar.setRating(4);

        LayerDrawable layerDrawable = (LayerDrawable) ratingBar.getProgressDrawable();
        DrawableCompat.setTint(DrawableCompat.wrap(layerDrawable.getDrawable(0)),
                ContextCompat.getColor(this, R.color.material_grey_300));
        DrawableCompat.setTint(DrawableCompat.wrap(layerDrawable.getDrawable(1)),
                ContextCompat.getColor(this, R.color.material_grey_300));
        DrawableCompat.setTint(DrawableCompat.wrap(layerDrawable.getDrawable(2)),
                ContextCompat.getColor(this, R.color.primary_teal));

        RxRatingBar.ratingChanges(ratingBar)
                .subscribe(rating -> {
                    ratingSubtitle.setText(ratingTexts[(int) (float) rating - 1]);
                });

        formEmailLayout.setError(getString(R.string.dialog_report_email_invalid));
        formEmailLayout.setError(null);

        RxTextView.textChanges(formEmail)
                .map(charSequence -> {
                    formEmailLayout.setError(null);
                    return charSequence;
                })
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(charSequence -> {
                    if (charSequence.length() > 0) {
                        validateEmail(charSequence.toString());
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        realm.close();
    }

    /**
     * Checks if a entered email is valid.
     *
     * @return {@code true} if it is valid, {@code false} otherwise.
     */
    private boolean validateEmail(String email) {
        if (email.isEmpty() || !ReportHelper.isValidEmail(email)) {
            formEmailLayout.setError(getString(R.string.dialog_report_email_invalid));
            return false;
        } else {
            formEmailLayout.setErrorEnabled(false);
        }

        return true;
    }

    private void sendSurvey(Trip trip) {
        if (!validateEmail(formEmail.getText().toString())) {
            return;
        }

        String email = formEmail.getText().toString();
        String message = formMessage.getText().toString();

        ReportBody reportBody = new ReportBody(this, email, message,
                trip.getVehicle(), (int) ratingBar.getRating(), trip);

        LogUtils.e(TAG, new Gson().toJson(reportBody));

        if (!NetUtils.isOnline(this)) {
            LogUtils.e(TAG, "Not sending survey because device is OFFLINE");

            surveyError();

            realm.beginTransaction();

            Survey survey = realm.createObject(Survey.class);
            survey.setData(new Gson().toJson(reportBody));

            realm.commitTransaction();

            return;
        }

        ProgressDialog progress = new ProgressDialog(this, R.style.DialogStyle);
        progress.setMessage(getString(R.string.dialog_report_sending));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();

        SurveyApi surveyApi = RestClient.ADAPTER.create(SurveyApi.class);
        surveyApi.send(reportBody)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.handleException(e);

                        realm.beginTransaction();

                        Survey survey = realm.createObject(Survey.class);
                        survey.setData(new Gson().toJson(reportBody));

                        realm.commitTransaction();

                        progress.dismiss();
                        surveyError();
                    }

                    @Override
                    public void onNext(Void aVoid) {
                        progress.dismiss();

                        View view = LayoutInflater.from(SurveyActivity.this)
                                .inflate(R.layout.dialog_survey_success, null);

                        Button close = (Button) view.findViewById(android.R.id.closeButton);

                        Dialog success = new Dialog(SurveyActivity.this, R.style.DialogStyle);
                        success.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        success.setContentView(view);
                        success.setCancelable(false);
                        success.show();

                        close.setOnClickListener(v -> {
                            success.dismiss();
                            SurveyExitActivity.exit(SurveyActivity.this);
                        });
                    }
                });
    }

    private void surveyError() {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_survey_error, null);

        Button close = (Button) view.findViewById(android.R.id.closeButton);

        Dialog error = new Dialog(this, R.style.DialogStyle);
        error.requestWindowFeature(Window.FEATURE_NO_TITLE);
        error.setContentView(view);
        error.setCancelable(false);
        error.show();

        close.setOnClickListener(v -> {
            error.dismiss();
            SurveyExitActivity.exit(this);
        });
    }


    public static class ReportBody extends ReportHelper.ReportBody {

        private final it.sasabz.android.sasabus.model.trip.Trip trip;
        private final int rating;
        private int id;

        ReportBody(Context context, String email, String message, int vehicle,
                   int rating, Trip trip) {
            super(context, email, message, vehicle);

            this.trip = new it.sasabz.android.sasabus.model.trip.Trip(trip);
            this.rating = rating;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getName().equals("id");
        }
    }
}
