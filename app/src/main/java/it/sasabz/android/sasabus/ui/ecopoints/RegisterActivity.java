package it.sasabz.android.sasabus.ui.ecopoints;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.fcm.FcmSettings;
import it.sasabz.android.sasabus.network.NetUtils;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.UserApi;
import it.sasabz.android.sasabus.network.rest.response.RegisterResponse;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.AnswersHelper;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.ReportHelper;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    @BindView(R.id.register_form_email) TextView email;
    @BindView(R.id.register_form_name) TextView name;
    @BindView(R.id.register_form_password) TextView password;
    @BindView(R.id.register_form_birthday) TextView birthday;
    @BindView(R.id.register_form_male) CheckBox male;
    @BindView(R.id.register_form_female) CheckBox female;

    @BindView(R.id.register_form_email_layout) TextInputLayout emailLayout;
    @BindView(R.id.register_form_name_layout) TextInputLayout nameLayout;
    @BindView(R.id.register_form_password_layout) TextInputLayout passwordLayout;
    @BindView(R.id.register_form_birthday_layout) TextInputLayout birthdayLayout;

    @BindView(R.id.register_form_button) FloatingActionButton button;
    @BindView(R.id.register_form_loading) ProgressBar loading;

    private Calendar mBirthdayCalendar;
    private Date lowerDateBounds;
    private Date upperDateBounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        AnalyticsHelper.sendScreenView(TAG);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        emailLayout.setError(getString(R.string.register_email_invalid));
        emailLayout.setError(null);

        nameLayout.setError(getString(R.string.register_name_invalid));
        nameLayout.setError(null);

        passwordLayout.setError(getString(R.string.register_password_invalid));
        passwordLayout.setError(null);

        birthdayLayout.setError(getString(R.string.register_birthdate_invalid));
        birthdayLayout.setError(null);

        birthday.setKeyListener(null);

        lowerDateBounds = new Date();
        lowerDateBounds.setTime(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365 * 13));

        upperDateBounds = new Date();
        upperDateBounds.setTime(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365 * 120));

        RxTextView.textChanges(email)
                .map(charSequence -> {
                    emailLayout.setError(null);
                    return charSequence;
                })
                .debounce(500, TimeUnit.MILLISECONDS)
                .filter(charSequence -> charSequence.length() > 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(charSequence -> validateEmail(charSequence.toString()));

        RxTextView.textChanges(name)
                .map(charSequence -> {
                    nameLayout.setError(null);
                    return charSequence;
                })
                .debounce(500, TimeUnit.MILLISECONDS)
                .filter(charSequence -> charSequence.length() > 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(charSequence -> validateName(charSequence.toString()));

        RxTextView.textChanges(password)
                .map(charSequence -> {
                    passwordLayout.setError(null);
                    return charSequence;
                })
                .debounce(500, TimeUnit.MILLISECONDS)
                .filter(charSequence -> charSequence.length() > 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(charSequence -> ReportHelper.validatePassword(this,
                        passwordLayout, charSequence.toString()));

        male.setOnCheckedChangeListener((buttonView, isChecked) -> {
            female.setChecked(!isChecked);
        });

        female.setOnCheckedChangeListener((buttonView, isChecked) -> {
            male.setChecked(!isChecked);
        });

        button.setOnClickListener(this);

        mBirthdayCalendar = Calendar.getInstance();
        mBirthdayCalendar.set(Calendar.YEAR, 2000);
        mBirthdayCalendar.set(Calendar.MONTH, 0);
        mBirthdayCalendar.set(Calendar.DAY_OF_YEAR, 1);
        mBirthdayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        mBirthdayCalendar.set(Calendar.MINUTE, 0);
        mBirthdayCalendar.set(Calendar.SECOND, 0);

        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            mBirthdayCalendar.set(Calendar.YEAR, year);
            mBirthdayCalendar.set(Calendar.MONTH, monthOfYear);
            mBirthdayCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            updateDateFormPicker();
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.DatePicker, date, mBirthdayCalendar.get(Calendar.YEAR),
                mBirthdayCalendar.get(Calendar.MONTH), mBirthdayCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365 * 13));

        birthday.setOnClickListener(v -> {
            datePickerDialog.getDatePicker().init(mBirthdayCalendar.get(Calendar.YEAR),
                    mBirthdayCalendar.get(Calendar.MONTH), mBirthdayCalendar.get(Calendar.DAY_OF_MONTH), null);

            datePickerDialog.show();
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_form_button:
                validateForm();
                break;
        }
    }


    private void validateForm() {
        boolean error = !validateEmail(email.getText().toString());

        error |= !validateName(name.getText().toString());
        error |= !ReportHelper.validatePassword(this, passwordLayout, password.getText().toString());
        error |= !validateBirthdate();

        if (error) {
            return;
        }

        animateViews(true);
        new Handler().postDelayed(this::tryRegister, 500);
    }

    private void animateViews(boolean showProgress) {
        if (showProgress) {
            button.animate()
                    .alpha(0)
                    .setDuration(250)
                    .start();

            loading.setVisibility(View.VISIBLE);
            ViewCompat.setAlpha(loading, 0);

            loading.animate()
                    .alpha(1)
                    .setDuration(250)
                    .setStartDelay(150)
                    .start();
        } else {
            loading.animate()
                    .alpha(0)
                    .setDuration(250)
                    .start();

            button.setVisibility(View.VISIBLE);
            ViewCompat.setAlpha(button, 0);

            button.animate()
                    .alpha(1)
                    .setDuration(250)
                    .setStartDelay(150)
                    .start();
        }
    }

    private void tryRegister() {
        if (!NetUtils.isOnline(this)) {
            LogUtils.e(TAG, "No internet connection available");

            new AlertDialog.Builder(this, R.style.DialogStyle)
                    .setTitle(R.string.login_no_internet_title)
                    .setMessage(R.string.login_no_internet_subtitle)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss())
                    .create()
                    .show();

            return;
        }

        String email = this.email.getText().toString();
        String name = this.name.getText().toString();
        String password = this.password.getText().toString();
        int birthdateSeconds = (int) (mBirthdayCalendar.getTimeInMillis() / 1000L);

        UserApi.RegisterBody body = new UserApi.RegisterBody(email, name,
                password, FcmSettings.getGcmToken(this), birthdateSeconds, male.isChecked());

        Log.e(TAG, new Gson().toJson(body));

        UserApi api = RestClient.ADAPTER.create(UserApi.class);
        api.register(body)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RegisterResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();

                        registrationFailed();
                    }

                    @Override
                    public void onNext(RegisterResponse registerResponse) {
                        LogUtils.e(TAG, registerResponse.toString());

                        validateResponse(registerResponse);
                    }
                });
    }

    private void validateResponse(RegisterResponse response) {
        if (response.success) {
            LogUtils.e(TAG, "Registration successful");

            registrationCompleted();

            AnswersHelper.logSignUp();
        } else {
            LogUtils.e(TAG, "Registration failure, got error: " + response.error);

            TextInputLayout field = null;
            switch (response.param) {
                case "email":
                    field = emailLayout;
                    break;
                case "username":
                    field = nameLayout;
                    break;
                case "password":
                    field = passwordLayout;
                    break;
                case "birthdate":
                    field = birthdayLayout;
                    break;
                default:
                    LogUtils.e(TAG, "Invalid field " + response.param);
                    break;
            }

            AnswersHelper.logSignUp(response.param);

            if (field != null) {
                field.setError(response.errorMessage);
            }

            AnalyticsHelper.sendEvent(TAG, response.error);

            animateViews(false);
        }
    }

    private void registrationFailed() {
        animateViews(false);

        new AlertDialog.Builder(this, R.style.DialogStyle)
                .setTitle(R.string.register_failed_dialog_title)
                .setMessage(R.string.register_failed_dialog_subtitle)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) ->
                        dialogInterface.dismiss())
                .create()
                .show();
    }

    private void registrationCompleted() {
        animateViews(false);

        new AlertDialog.Builder(this, R.style.DialogStyle)
                .setTitle(R.string.register_success_dialog_title)
                .setMessage(R.string.register_success_dialog_subtitle)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    startActivity(new Intent(this, LoginActivity.class));
                })
                .create()
                .show();
    }

    private void updateDateFormPicker() {
        SimpleDateFormat sdf;
        switch (getResources().getConfiguration().locale.toString()) {
            case "it":
                sdf = new SimpleDateFormat("dd MMM yyyy", Locale.ITALY);
                break;
            case "de":
                sdf = new SimpleDateFormat("dd MMM yyyy", Locale.GERMAN);
                break;
            default:
                sdf = new SimpleDateFormat("dd MMM yyyy", Locale.US);
                break;
        }

        birthday.setText(sdf.format(mBirthdayCalendar.getTime()));
    }


    // =================================== FORM VALIDATION =========================================

    private boolean validateEmail(CharSequence email) {
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError(getString(R.string.register_email_empty));
            return false;
        }

        if (!ReportHelper.isEmailValid(email)) {
            emailLayout.setError(getString(R.string.register_email_invalid));
            return false;
        }

        emailLayout.setError(null);

        return true;
    }

    private boolean validateName(String name) {
        if (TextUtils.isEmpty(name)) {
            nameLayout.setError(getString(R.string.register_name_empty));
            return false;
        }

        if (name.length() < 6) {
            nameLayout.setError(getString(R.string.register_name_too_short));
            return false;
        }

        char[] chars = name.toCharArray();

        if (chars[0] == ' ' || chars[0] == '_') {
            nameLayout.setError(getString(R.string.register_name_first_letter));
            return false;
        }

        if (Character.isDigit(chars[0])) {
            nameLayout.setError(getString(R.string.register_name_first_letter));
            return false;
        }

        if (chars[chars.length - 1] == ' ') {
            nameLayout.setError(getString(R.string.register_name_invalid));
            return false;
        }

        for (char c : chars) {
            if (!Character.isLetter(c) && !Character.isDigit(c) && c != '_' && c != ' ') {
                nameLayout.setError(getString(R.string.register_name_invalid));
                return false;
            }
        }

        nameLayout.setError(null);

        return true;
    }

    private boolean validateBirthdate() {
        if (TextUtils.isEmpty(birthday.getText())) {
            birthdayLayout.setError(getString(R.string.register_birthdate_empty));
            return false;
        }

        if (mBirthdayCalendar.after(lowerDateBounds)) {
            birthdayLayout.setError(getString(R.string.register_birthdate_too_young));
            return false;
        }

        if (mBirthdayCalendar.before(upperDateBounds)) {
            birthdayLayout.setError(getString(R.string.register_birthdate_too_old));
            return false;
        }

        birthdayLayout.setError(null);

        return true;
    }
}
