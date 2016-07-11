package it.sasabz.android.sasabus.ui;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import it.sasabz.android.sasabus.BuildConfig;
import it.sasabz.android.sasabus.R;

import it.sasabz.android.sasabus.network.rest.api.ReportApi;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.CustomTabsHelper;
import it.sasabz.android.sasabus.util.ReportHelper;
import it.sasabz.android.sasabus.util.Utils;

/**
 * Activity which allows the user to view the changelog, credits ecc.
 * This activity provides a way to send reports about app bugs or crashes by using
 * {@link #showReportDialog()} to show the report dialog and {@link ReportHelper} to send the report.
 *
 * @author Alex Lardschneider
 * @author David Dejori
 */
public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AboutActivity";
    private static final String SCREEN_LABEL = "About";

    private static final int REQUEST_CODE_EMAIL = 1001;
    private static final int SELECT_PHOTO = 1002;
    private static final int PERMISSIONS_ACCESS_STORAGE = 100;

    private static final String URL_PRIVACY = "http://www.sasabz.it/fileadmin/user_upload/PDFs/POLICY_SITO_WEB_SASA_2015.pdf";

    /**
     * Report dialog input fields.
     */
    private EditText mEmail;
    private EditText mContent;

    /**
     * Report dialog input layouts to get the material animations.
     */
    private TextInputLayout mEmailLayout;
    private TextInputLayout mContentLayout;

    /**
     * The {@link ImageView} which holds the screenshot image.
     */
    private ImageView mScreenshotImage;

    /**
     * The button to press when you want to add a screenshot to the report.
     */
    private Button mScreenshotButton;

    /**
     * The {@link Uri} of the selected report image to send.
     */
    private Uri mScreenshotUri;

    private CustomTabsHelper mCustomTabsHelper;

    private ReportHelper reportHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.changeLanguage(this);

        setContentView(R.layout.activity_about);

        AnalyticsHelper.sendScreenView(TAG);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RelativeLayout relativeLayout1 = (RelativeLayout) findViewById(R.id.about_first);
        RelativeLayout relativeLayout2 = (RelativeLayout) findViewById(R.id.about_second);
        RelativeLayout relativeLayout3 = (RelativeLayout) findViewById(R.id.about_third);
        RelativeLayout relativeLayout4 = (RelativeLayout) findViewById(R.id.about_fourth);

        if (relativeLayout1 != null) {
            relativeLayout1.setOnClickListener(this);
        }
        if (relativeLayout2 != null) {
            relativeLayout2.setOnClickListener(this);
        }
        if (relativeLayout3 != null) {
            relativeLayout3.setOnClickListener(this);
        }
        if (relativeLayout4 != null) {
            relativeLayout4.setOnClickListener(this);
        }

        TextView textView = (TextView) findViewById(R.id.about_first_sub);
        if (textView != null) {
            textView.setText(getString(R.string.about_changelog_subtitle) + ' ' + BuildConfig.VERSION_NAME);
        }

        if (getIntent().getBooleanExtra("dialog_report", false)) {
            showReportDialog();
        }

        mCustomTabsHelper = new CustomTabsHelper(this);
        mCustomTabsHelper.start();

        reportHelper = new ReportHelper(this, findViewById(android.R.id.content),
                ReportApi.TYPE_DEFAULT);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.about_first:
                startActivity(new Intent(this, ChangelogActivity.class));
                break;
            case R.id.about_second:
                showReportDialog();
                break;
            case R.id.about_third:
                startActivity(new Intent(this, CreditsActivity.class));
                break;
            case R.id.about_fourth:
                AnalyticsHelper.sendEvent(SCREEN_LABEL, "Privacy");
                mCustomTabsHelper.launchUrl(Uri.parse(URL_PRIVACY));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mCustomTabsHelper.stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EMAIL && resultCode == RESULT_OK) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

            mEmail.setText(accountName);
        } else if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data != null) {
            mScreenshotUri = data.getData();

            if (mScreenshotUri != null) {
                Glide.with(this).load(mScreenshotUri).centerCrop().into(mScreenshotImage);
                mScreenshotImage.setVisibility(View.VISIBLE);
                mScreenshotButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_ACCESS_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    reportHelper.pickImage();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        reportHelper.showPermissionRationale();
                    }
                }
                break;
        }
    }


    /**
     * Shows the report dialog and sets up all the important listeners.
     */
    private void showReportDialog() {
        AnalyticsHelper.sendEvent(SCREEN_LABEL, "Report show");

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.dialog_report, null);

        mEmailLayout = (TextInputLayout) promptView.findViewById(R.id.dialog_report_email_layout);
        mContentLayout = (TextInputLayout) promptView.findViewById(R.id.dialog_report_content_layout);
        mEmail = (EditText) promptView.findViewById(R.id.dialog_report_email);
        mContent = (EditText) promptView.findViewById(R.id.dialog_report_content);

        mEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mEmailLayout.setErrorEnabled(false);
                mEmailLayout.setErrorEnabled(false);
            }
        });

        mContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mContentLayout.setErrorEnabled(false);
                mContentLayout.setErrorEnabled(false);
            }
        });

        mScreenshotButton = (Button) promptView.findViewById(R.id.dialog_report_screenshot_button);
        mScreenshotImage = (ImageView) promptView.findViewById(R.id.dialog_report_screenshot);

        mScreenshotButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    reportHelper.showPermissionRationale();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_ACCESS_STORAGE);
                }
            } else {
                reportHelper.pickImage();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogStyle)
                .setTitle(R.string.dialog_report_title)
                .setView(promptView)
                .setPositiveButton(R.string.dialog_report_send, (dialog, which) -> {

                })
                .setNegativeButton(getResources().getString(android.R.string.cancel), (dialog, which) -> {
                    mScreenshotUri = null;
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (validateEmail() && validateContent()) {
                AnalyticsHelper.sendEvent(SCREEN_LABEL, "Report send");

                View focus = getCurrentFocus();
                if (focus != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
                }

                reportHelper.send(mEmail.getText().toString(), mContent.getText().toString(),
                        mScreenshotUri);

                dialog.dismiss();
            }
        });
    }

    /**
     * Checks if a entered email is valid.
     *
     * @return {@code true} if it is valid, {@code false} otherwise.
     */
    private boolean validateEmail() {
        String emailString = mEmail.getText().toString().trim();

        if (emailString.isEmpty() || !ReportHelper.isValidEmail(emailString)) {
            mEmailLayout.setError(getString(R.string.dialog_report_email_invalid));
            return false;
        } else {
            mEmailLayout.setErrorEnabled(false);
        }

        return true;
    }

    /**
     * Checks if a entered content is valid.
     *
     * @return {@code true} if it is valid, {@code false} otherwise.
     */
    private boolean validateContent() {
        String contentString = mContent.getText().toString().trim();

        if (contentString.isEmpty() || contentString.length() > 2000) {
            mContentLayout.setError(getString(R.string.dialog_report_content_invalid));
            return false;
        } else {
            mContentLayout.setErrorEnabled(false);
        }

        return true;
    }
}