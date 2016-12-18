/*
 * Copyright (C) 2016 David Dejori, Alex Lardschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.sasabz.android.sasabus.ui.ecopoints;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.ViewCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.data.network.NetUtils;
import it.sasabz.android.sasabus.data.network.auth.AuthHelper;
import it.sasabz.android.sasabus.data.network.rest.RestClient;
import it.sasabz.android.sasabus.data.network.rest.api.UserApi;
import it.sasabz.android.sasabus.data.network.rest.response.LoginResponse;
import it.sasabz.android.sasabus.fcm.FcmSettings;
import it.sasabz.android.sasabus.ui.BaseActivity;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.AnswersHelper;
import it.sasabz.android.sasabus.util.ReportHelper;
import it.sasabz.android.sasabus.util.UIUtils;
import it.sasabz.android.sasabus.util.Utils;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    private static final String ERROR_ALREADY_VERIFIED = "already_verified";
    private static final String ERROR_VERIFICATION_FAILED = "verification_failed";

    @BindView(R.id.login_form_email) TextView email;
    @BindView(R.id.login_form_password) TextView password;

    @BindView(R.id.login_form_email_layout) TextInputLayout emailLayout;
    @BindView(R.id.login_form_password_layout) TextInputLayout passwordLayout;

    @BindView(R.id.login_form_button) FloatingActionButton button;
    @BindView(R.id.login_form_loading) ProgressBar loading;

    private boolean verify;
    private Uri verifyData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        AnalyticsHelper.sendScreenView(TAG);

        getSupportActionBar().setTitle("");

        Intent intent = getIntent();
        String action = intent.getAction();
        verifyData = intent.getData();

        if (action != null && verifyData != null) {
            Timber.e("Got intent: " + action + " " + verifyData);

            verify = true;
            loginOrVerify();
        }

        TextView signInText = (TextView) findViewById(R.id.login_sign_in_text);
        signInText.setText(Html.fromHtml("Not a member?<br><font color=\"#03A9F4\">Sign up</font> for eco points"));
        signInText.setOnClickListener(this);

        emailLayout.setError(getString(R.string.login_invalid_email));
        emailLayout.setError(null);

        passwordLayout.setError(getString(R.string.login_invalid_password));
        passwordLayout.setError(null);

        RxTextView.textChanges(email)
                .map(charSequence -> {
                    emailLayout.setError(null);
                    return charSequence;
                })
                .debounce(500, TimeUnit.MILLISECONDS)
                .filter(charSequence -> charSequence.length() > 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(charSequence -> validateEmail(charSequence.toString()));

        button.setOnClickListener(this);
    }

    @Override
    protected int getNavItem() {
        return NAVDRAWER_ITEM_ECO_POINTS;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_form_button:
                validateForm();
                break;
            case R.id.login_sign_in_text:
                startActivity(new Intent(this, RegisterActivity.class));
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String action = intent.getAction();
        verifyData = intent.getData();

        if (action != null && verifyData != null) {
            Timber.e("Got intent: " + action + " " + verifyData);

            verify = true;
            loginOrVerify();
        }
    }

    private void validateForm() {
        boolean error = !validateEmail(email.getText().toString());
        error |= !validatePassword(password.getText().toString());

        if (error) {
            return;
        }

        animateViews(true);

        new Handler().postDelayed(this::loginOrVerify, 500);
    }

    private void loginOrVerify() {
        if (!NetUtils.isOnline(this)) {
            Timber.e("No internet connection available");

            UIUtils.okDialog(this, R.string.login_no_internet_title,
                    R.string.login_no_internet_subtitle);

            return;
        }

        UserApi api = RestClient.ADAPTER.create(UserApi.class);

        if (verify) {
            verify = false;

            ProgressDialog progressDialog = new ProgressDialog(this, R.style.DialogStyle);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(getString(R.string.login_verification_progress_subtitle));
            progressDialog.setCancelable(false);
            progressDialog.show();

            List<String> segments = verifyData.getPathSegments();

            api.verify(segments.get(3), segments.get(4))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<LoginResponse>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Utils.logException(e);

                            progressDialog.dismiss();

                            UIUtils.okDialog(LoginActivity.this, R.string.login_verification_failed_title,
                                    R.string.login_verification_failed_subtitle);
                        }

                        @Override
                        public void onNext(LoginResponse response) {
                            Timber.e(response.toString());

                            progressDialog.dismiss();

                            if (response.success) {
                                loginSuccess(response);
                                return;
                            }

                            animateViews(false);

                            switch (response.error) {
                                case ERROR_ALREADY_VERIFIED:
                                    UIUtils.okDialog(LoginActivity.this, R.string.login_already_verified_title,
                                            R.string.login_already_verified_subtitle);
                                    break;
                                case ERROR_VERIFICATION_FAILED:
                                    UIUtils.okDialog(LoginActivity.this, R.string.login_verify_link_invalid_title,
                                            R.string.login_verify_link_invalid_subtitle);
                                    break;
                                default:
                                    Timber.e("Unknown error: " + response.error);
                            }
                        }
                    });
        } else {
            String email = this.email.getText().toString();
            String password = this.password.getText().toString();

            UserApi.LoginBody body = new UserApi.LoginBody(email, password,
                    FcmSettings.getGcmToken(this));

            Log.e(TAG, new Gson().toJson(body));

            api.login(body)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<LoginResponse>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Utils.logException(e);

                            loginFailed();
                        }

                        @Override
                        public void onNext(LoginResponse response) {
                            Timber.e(response.toString());

                            if (response.success) {
                                Timber.e("Login success, got token: " + response.token);

                                if (response.token == null) {
                                    Timber.e("Token is null");
                                    loginFailed();
                                    return;
                                }

                                loginSuccess(response);
                                return;
                            }

                            Timber.e("Login failure, got error: " + response.error);

                            TextInputLayout field = null;
                            switch (response.param) {
                                case "email":
                                    field = emailLayout;
                                    break;
                                case "password":
                                    field = passwordLayout;
                                    break;
                                default:
                                    Timber.e("Unknown field " + response.param);
                                    break;
                            }

                            if (field != null) {
                                field.setError(response.errorMessage);
                            }

                            AnswersHelper.logLoginError(response.param);

                            animateViews(false);
                        }
                    });
        }
    }

    private void animateViews(boolean showProgress) {
        if (showProgress) {
            button.animate().cancel();
            loading.animate().cancel();

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

            emailLayout.setError(null);
            passwordLayout.setError(null);
        } else {
            loading.animate().cancel();
            button.animate().cancel();

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

    private void loginFailed() {
        animateViews(false);

        AnswersHelper.logLoginError("Token error");

        UIUtils.okDialog(this, R.string.login_failed_dialog_title,
                R.string.login_failed_dialog_subtitle);
    }

    private void loginSuccess(LoginResponse response) {
        if (AuthHelper.setInitialToken(response.token)) {
            finish();
            startActivity(new Intent(this, EcoPointsActivity.class));

            AnswersHelper.logLoginSuccess();
        } else {
            Timber.e("Could not set token");
            loginFailed();
        }
    }


    // =================================== FORM VALIDATION =========================================

    private boolean validateEmail(CharSequence name) {
        if (!ReportHelper.isEmailValid(name)) {
            emailLayout.setError(getString(R.string.login_invalid_email));
            return false;
        }

        emailLayout.setError(null);

        return true;
    }

    private boolean validatePassword(CharSequence password) {
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError(getString(R.string.register_password_empty));
            return false;
        }

        if (password.length() < 6) {
            passwordLayout.setError(getString(R.string.register_password_too_short));
            return false;
        }

        passwordLayout.setError(null);

        return true;
    }
}
