package it.sasabz.android.sasabus.ui.bus;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import it.sasabz.android.sasabus.Config;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.Bus;
import it.sasabz.android.sasabus.model.Buses;
import it.sasabz.android.sasabus.model.Vehicle;
import it.sasabz.android.sasabus.network.rest.api.ReportApi;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.ReportHelper;
import it.sasabz.android.sasabus.util.Utils;

/**
 * Displays information about a bus/vehicle like license plate, color, fuel type ecc.
 * This activity consists of a {@link android.support.design.widget.AppBarLayout} with a picture of
 * the vehicle type as scrim, and a {@link CardView} below which holds the bus information.
 *
 * @author Alex Lardschneider
 */
public class BusDetailActivity extends AppCompatActivity {

    private static final String TAG = "BusDetailActivity";
    private static final String SCREEN_LABEL = "BusMarker details";

    private static final int REQUEST_CODE_EMAIL = 1001;
    private static final int SELECT_PHOTO = 1002;
    private static final int PERMISSIONS_ACCESS_STORAGE = 100;

    /**
     * Layout which holds the vehicle info.
     */
    private CardView mMainContent;
    private CardView mError;

    /**
     * Views holding the vehicle info.
     */
    private TextView mManufacturer;
    private TextView mModel;
    private TextView mFuel;
    private TextView mColor;

    /**
     * The vehicle id.
     */
    private int mVehicle;

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
     * The button to press when you want to add a photo to the report.
     */
    private Button mScreenshotButton;

    /**
     * The {@link Uri} of the selected report image to send.
     */
    private Uri mScreenshotUri;

    private ReportHelper reportHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.changeLanguage(this);

        setContentView(R.layout.activity_bus_details);

        Intent intent = getIntent();
        mVehicle = intent.getExtras().getInt(Config.EXTRA_VEHICLE);

        AnalyticsHelper.sendScreenView(TAG);
        AnalyticsHelper.sendEvent(SCREEN_LABEL, "Vehicle: " + mVehicle);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> finish());

        /*
        The collapsing toolbar layout which holds the vehicle image and handles the image
        scrim.
        */
        CollapsingToolbarLayout mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mCollapsingToolbar.setTitle(getString(R.string.bus_details));

        CoordinatorLayout mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_content);

        mError = (CardView) findViewById(R.id.bus_detail_error);
        mMainContent = (CardView) findViewById(R.id.bus_detail_main);

        mManufacturer = (TextView) findViewById(R.id.bus_details_manufacturer);
        mModel = (TextView) findViewById(R.id.bus_detail_model);
        mFuel = (TextView) findViewById(R.id.bus_detail_fuel);
        mColor = (TextView) findViewById(R.id.bus_detail_color);

        TextView report = (TextView) findViewById(R.id.bus_details_report);
        report.setOnClickListener(v -> {
            showReportDialog();
            AnalyticsHelper.sendEvent(SCREEN_LABEL, "Report show");
        });

        parseData(mVehicle);

        reportHelper = new ReportHelper(this, mCoordinatorLayout, ReportApi.TYPE_BUS);
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

    private void loadBackdrop(Vehicle vehicle) {
        ImageView imageView = (ImageView) findViewById(R.id.backdrop);

        if (imageView != null) {
            Glide.with(this).load(Uri.parse("file:///android_asset/images/" + vehicle.getCode() + ".jpg"))
                    .animate(R.anim.fade_in_short)
                    .centerCrop()
                    .crossFade()
                    .into(imageView);
        }
    }

    /**
     * Parses the vehicle data from the internet.
     *
     * @param vehicle the vehicle id.
     */
    private void parseData(int vehicle) {
        Bus b = Buses.getBus(vehicle);

        if (b != null) {
            Vehicle v = b.getVehicle();

            if (v != null) {
                mError.setVisibility(View.GONE);

                mManufacturer.setText(v.getManufacturer());
                mModel.setText(v.getModel());
                mFuel.setText(v.getFuelString(this));
                mColor.setText(v.getFuelString(this));

                loadBackdrop(v);
            } else {
                mMainContent.setVisibility(View.GONE);
                mError.setVisibility(View.VISIBLE);
            }
        }
    }


    /**
     * Shows the report dialog and sets up all the important listeners.
     */
    private void showReportDialog() {
        AnalyticsHelper.sendEvent(SCREEN_LABEL, "Report show");

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.dialog_report_bus, null);

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
                        mScreenshotUri, mVehicle);
                dialog.dismiss();
            }
        });
    }

    /**
     * Checks if a entered email is valid, also blacklists throw-away emails.
     *
     * @return {@code true} if it is valid, {@code false} otherwise.
     */
    private boolean validateEmail() {
        String emailString = mEmail.getText().toString().trim();

        if (emailString.isEmpty() || !ReportHelper.isEmailValid(emailString)) {
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