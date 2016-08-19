package it.sasabz.android.sasabus.ui.ecopoints;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.fcm.FcmSettings;
import it.sasabz.android.sasabus.network.NetUtils;
import it.sasabz.android.sasabus.network.auth.AuthHelper;
import it.sasabz.android.sasabus.network.rest.Endpoint;
import it.sasabz.android.sasabus.network.rest.RestClient;
import it.sasabz.android.sasabus.network.rest.api.EcoPointsApi;
import it.sasabz.android.sasabus.network.rest.api.UserApi;
import it.sasabz.android.sasabus.network.rest.model.Profile;
import it.sasabz.android.sasabus.network.rest.response.PasswordResponse;
import it.sasabz.android.sasabus.network.rest.response.ProfilePictureResponse;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.LogUtils;
import it.sasabz.android.sasabus.util.ReportHelper;
import it.sasabz.android.sasabus.util.UIUtils;
import it.sasabz.android.sasabus.util.Utils;
import it.sasabz.android.sasabus.util.recycler.CircleImageAdapter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author David Dejori
 */
public class EcoPointsProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int CAMERA_REQUEST = 101;
    private static final int GALLERY_REQUEST = 102;

    private static final int PERMISSIONS_ACCESS_CAMERA_STORAGE = 100;
    private static final int PERMISSIONS_ACCESS_GALLERY_STORAGE = 101;

    public static final String EXTRA_PROFILE_URL = "com.davale.sasabus.EXTRA_PROFILE_URL";
    public static final String EXTRA_PROFILE_FILE = "com.davale.sasabus.EXTRA_PROFILE_FILE";

    private static final String TAG = "EcoPointsActivity";

    static final String EXTRA_PROFILE = "com.davale.sasabus.EXTRA_PROFILE";

    @BindView(R.id.main_content) FrameLayout mainContent;

    @BindView(R.id.eco_points_profile_picture) ImageView profilePicture;
    @BindView(R.id.eco_points_profile_name) TextView profileName;
    @BindView(R.id.eco_points_profile_card_21) CardView cardView1;

    private File imageFile;

    private boolean profileChanged;
    private String newProfileUrl;
    private File newProfileFile;

    private BroadcastReceiver logoutReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthHelper.isLoggedIn()) {
            LogUtils.e(TAG, "Token is null, showing login activity");
            finish();
            startActivity(new Intent(this, LoginActivity.class));

            return;
        }

        logoutReceiver = AuthHelper.registerLogoutReceiver(this);

        setContentView(R.layout.activity_eco_points_profile);
        ButterKnife.bind(this);

        AnalyticsHelper.sendScreenView(TAG);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        Profile profile = intent.getParcelableExtra(EXTRA_PROFILE);
        if (profile == null) {
            LogUtils.e(TAG, "Missing intent extra " + EXTRA_PROFILE);
            finish();
            return;
        }

        Glide.with(this)
                .load(Endpoint.API + Endpoint.ECO_POINTS_PROFILE_PICTURE_USER + profile.profile)
                .into(profilePicture);

        profileName.setText(profile.username);

        RelativeLayout password = (RelativeLayout) findViewById(R.id.eco_points_profile_password);
        RelativeLayout picture = (RelativeLayout) findViewById(R.id.eco_points_profile_change_picture);
        RelativeLayout logout = (RelativeLayout) findViewById(R.id.eco_points_profile_logout);
        RelativeLayout logoutAll = (RelativeLayout) findViewById(R.id.eco_points_profile_logout_all);
        RelativeLayout delete = (RelativeLayout) findViewById(R.id.eco_points_profile_delete_account);

        password.setOnClickListener(this);
        picture.setOnClickListener(this);
        logout.setOnClickListener(this);
        logoutAll.setOnClickListener(this);
        delete.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.eco_points_profile_password:
                showPasswordDialog();
                break;
            case R.id.eco_points_profile_change_picture:
                showPictureDialog();
                break;
            case R.id.eco_points_profile_logout:
                logout();
                break;
            case R.id.eco_points_profile_logout_all:
                logoutAll();
                break;
            case R.id.eco_points_profile_delete_account:
                deleteAccount();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_ACCESS_CAMERA_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    ReportHelper.showPermissionRationale(this);
                }
                break;
            case PERMISSIONS_ACCESS_GALLERY_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromGallery();
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    ReportHelper.showPermissionRationale(this);
                }
                break;
        }
    }

    @Override
    public void finish() {
        if (profileChanged) {
            Intent data = new Intent();
            data.putExtra(EXTRA_PROFILE_URL, newProfileUrl);
            data.putExtra(EXTRA_PROFILE_FILE, newProfileFile);

            setResult(RESULT_OK, data);
        }

        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        AuthHelper.unregisterLogoutReceiver(this, logoutReceiver);
    }


    // ===================================== ACCOUNT ===============================================

    private void logout() {
        if (!NetUtils.isOnline(this)) {
            Snackbar.make(mainContent, R.string.error_wifi, Snackbar.LENGTH_LONG);
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this, R.style.DialogStyle);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.dialog_logging_out));
        progressDialog.show();

        UserApi api = RestClient.ADAPTER.create(UserApi.class);
        api.logout(FcmSettings.getGcmToken(this))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.handleException(e);

                        progressDialog.dismiss();

                        UIUtils.okDialog(EcoPointsProfileActivity.this,
                                R.string.eco_points_logout_error_dialog_title,
                                R.string.eco_points_logout_error_dialog_message,
                                (dialogInterface, i) -> dialogInterface.dismiss());
                    }

                    @Override
                    public void onNext(Void response) {
                        progressDialog.dismiss();

                        AuthHelper.logout(EcoPointsProfileActivity.this);
                    }
                });
    }

    private void logoutAll() {
        if (!NetUtils.isOnline(this)) {
            Snackbar.make(mainContent, R.string.error_wifi, Snackbar.LENGTH_LONG);
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this, R.style.DialogStyle);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.dialog_logging_out));
        progressDialog.show();

        UserApi api = RestClient.ADAPTER.create(UserApi.class);
        api.logoutAll()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.handleException(e);

                        progressDialog.dismiss();

                        UIUtils.okDialog(EcoPointsProfileActivity.this,
                                R.string.eco_points_logout_error_dialog_title,
                                R.string.eco_points_logout_error_dialog_message,
                                (dialogInterface, i) -> dialogInterface.dismiss());
                    }

                    @Override
                    public void onNext(Void response) {
                        progressDialog.dismiss();

                        AuthHelper.logout(EcoPointsProfileActivity.this);
                    }
                });
    }

    private void deleteAccount() {
        if (!NetUtils.isOnline(this)) {
            Snackbar.make(mainContent, R.string.error_wifi, Snackbar.LENGTH_LONG);
            return;
        }

        new AlertDialog.Builder(this, R.style.DialogStyle)
                .setTitle(R.string.eco_points_delete_account_confirmation_title)
                .setMessage(R.string.eco_points_delete_account_confirmation_message)
                .setPositiveButton(R.string.eco_points_delete_account_confirmation_positive, (dialogInterface, i) -> {
                    dialogInterface.dismiss();

                    ProgressDialog progressDialog = new ProgressDialog(this, R.style.DialogStyle);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage(getString(R.string.dialog_deleting_account));
                    progressDialog.show();

                    UserApi api = RestClient.ADAPTER.create(UserApi.class);
                    api.delete()
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<Void>() {
                                @Override
                                public void onCompleted() {

                                }

                                @Override
                                public void onError(Throwable e) {
                                    Utils.handleException(e);

                                    progressDialog.dismiss();

                                    UIUtils.okDialog(EcoPointsProfileActivity.this,
                                            R.string.eco_points_delete_account_error_title,
                                            R.string.eco_points_delete_account_error_message,
                                            (dialogInterface, i) -> dialogInterface.dismiss());
                                }

                                @Override
                                public void onNext(Void response) {
                                    progressDialog.dismiss();

                                    AuthHelper.logout(EcoPointsProfileActivity.this);
                                }
                            });
                })
                .setNegativeButton(R.string.dialog_button_cancel, (dialogInterface, i) ->
                        dialogInterface.dismiss())
                .create()
                .show();
    }


    // ===================================== PASSWORD ==============================================

    private void showPasswordDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_change_password, null, false);

        TextInputLayout oldLayout = (TextInputLayout)
                view.findViewById(R.id.dialog_change_password_old_layout);
        TextInputLayout newLayout1 = (TextInputLayout)
                view.findViewById(R.id.dialog_change_password_new_1_layout);
        TextInputLayout newLayout2 = (TextInputLayout)
                view.findViewById(R.id.dialog_change_password_new_2_layout);

        TextView old = (TextView) view.findViewById(R.id.dialog_change_password_old);
        TextView new1 = (TextView) view.findViewById(R.id.dialog_change_password_new_1);
        TextView new2 = (TextView) view.findViewById(R.id.dialog_change_password_new_2);

        oldLayout.setError(getString(R.string.dialog_change_password_new_no_match));
        oldLayout.setError(null);

        newLayout1.setError(getString(R.string.dialog_change_password_new_no_match));
        newLayout1.setError(null);

        newLayout2.setError(getString(R.string.dialog_change_password_new_no_match));
        newLayout2.setError(null);

        RxTextView.textChanges(old)
                .map(charSequence -> {
                    oldLayout.setError(null);
                    return charSequence;
                })
                .debounce(500, TimeUnit.MILLISECONDS)
                .filter(charSequence -> charSequence.length() > 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(charSequence -> ReportHelper.validatePassword(this,
                        oldLayout, charSequence.toString()));

        RxTextView.textChanges(new1)
                .map(charSequence -> {
                    newLayout1.setError(null);
                    return charSequence;
                })
                .debounce(500, TimeUnit.MILLISECONDS)
                .filter(charSequence -> charSequence.length() > 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(charSequence -> ReportHelper.validatePassword(this,
                        newLayout1, charSequence.toString()));

        RxTextView.textChanges(new2)
                .map(charSequence -> {
                    newLayout2.setError(null);
                    return charSequence;
                })
                .debounce(500, TimeUnit.MILLISECONDS)
                .filter(charSequence -> charSequence.length() > 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(charSequence -> ReportHelper.validatePassword(this,
                        newLayout2, charSequence.toString()));

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.DialogStyle)
                .setTitle(R.string.dialog_change_password_title)
                .setView(view)
                .setPositiveButton(R.string.dialog_change_password_positive, (dialogInterface, i) -> {
                })
                .setNegativeButton(R.string.dialog_button_cancel, (dialogInterface, i) -> {
                })
                .create();

        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String oldPassword = old.getText().toString();
            String newPassword1 = new1.getText().toString();
            String newPassword2 = new2.getText().toString();

            boolean error = !ReportHelper.validatePassword(this, oldLayout, oldPassword);

            error |= !ReportHelper.validatePassword(this, newLayout1, newPassword1);
            error |= !ReportHelper.validatePassword(this, newLayout2, newPassword2);

            error |= !checkForPasswordMatch(newLayout1, newLayout2, newPassword1, newPassword2);

            if (error) {
                return;
            }

            ProgressDialog progressDialog = new ProgressDialog(this, R.style.DialogStyle);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getString(R.string.dialog_change_password_changing));
            progressDialog.show();

            UserApi api = RestClient.ADAPTER.create(UserApi.class);
            api.changePassword(oldPassword, newPassword1, FcmSettings.getGcmToken(this))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<PasswordResponse>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Utils.handleException(e);

                            progressDialog.dismiss();

                            UIUtils.okDialog(EcoPointsProfileActivity.this,
                                    R.string.dialog_change_password_error_title,
                                    R.string.dialog_change_password_error_message,
                                    (dialogInterface, i) -> {

                                        dialogInterface.dismiss();
                                        dialog.dismiss();
                                    });
                        }

                        @Override
                        public void onNext(PasswordResponse response) {
                            if (response.success) {
                                String token = response.token;

                                if (!AuthHelper.setInitialToken(token)) {
                                    AuthHelper.logout(EcoPointsProfileActivity.this);
                                    return;
                                }

                                LogUtils.e(TAG, "Password change successful");

                                progressDialog.dismiss();

                                UIUtils.okDialog(EcoPointsProfileActivity.this,
                                        R.string.dialog_change_password_success_title,
                                        R.string.dialog_change_password_success_message,
                                        (dialogInterface, i) -> {

                                            dialogInterface.dismiss();
                                            dialog.dismiss();
                                        });

                            } else {
                                LogUtils.e(TAG, "Password change failure, got error: " + response.error);

                                progressDialog.dismiss();

                                switch (response.param) {
                                    case "old_pwd":
                                        oldLayout.setError(response.errorMessage);
                                        break;
                                    case "new_pwd":
                                        newLayout1.setError(response.errorMessage);
                                        newLayout2.setError(response.errorMessage);
                                        break;
                                    default:
                                        LogUtils.e(TAG, "Invalid field " + response.param);
                                        break;
                                }
                            }
                        }
                    });
        });
    }

    private boolean checkForPasswordMatch(TextInputLayout layout1, TextInputLayout layout2,
                                          String password1, String password2) {
        if (!password1.equals(password2)) {
            layout1.setError(getString(R.string.dialog_change_password_new_no_match));
            layout2.setError(getString(R.string.dialog_change_password_new_no_match));
            return false;
        }

        layout1.setError(null);
        layout2.setError(null);

        return true;
    }


    // ================================= PROFILE PICTURE ===========================================

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
                uploadPicture(imageFile);
            } else if (requestCode == GALLERY_REQUEST) {
                Uri imageUri = data.getData();
                uploadPicture(getFileFromUri(imageUri));
            }
        }
    }

    private void showPictureDialog() {
        CharSequence[] options = {
                "Select from defaults",
                "Take picture",
                "Pick from gallery"
        };

        new AlertDialog.Builder(this, R.style.DialogStyle)
                .setItems(options, (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            selectFromDefaults();
                            break;
                        case 1:
                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    ReportHelper.showPermissionRationale(this);
                                } else {
                                    ActivityCompat.requestPermissions(this,
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_ACCESS_CAMERA_STORAGE);
                                }
                            } else {
                                takePicture();
                            }
                            break;
                        case 2:
                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    ReportHelper.showPermissionRationale(this);
                                } else {
                                    ActivityCompat.requestPermissions(this,
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_ACCESS_GALLERY_STORAGE);
                                }
                            } else {
                                pickFromGallery();
                            }
                            break;
                    }
                })
                .create()
                .show();
    }

    private void selectFromDefaults() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_change_profile_picture, null, false);

        List<String> mItems = new ArrayList<>();

        ProgressBar progressBar = (ProgressBar)
                view.findViewById(R.id.dialog_change_profile_picture_progress);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.DialogStyle)
                .setView(view)
                .create();

        CircleImageAdapter adapter = new CircleImageAdapter(this, mItems, position -> {
            ProgressDialog progressDialog = new ProgressDialog(this, R.style.DialogStyle);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getString(R.string.dialog_change_profile_picture_changing));
            progressDialog.show();

            String imageUrl = mItems.get(position);

            EcoPointsApi api = RestClient.ADAPTER.create(EcoPointsApi.class);
            api.upload(imageUrl)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Void>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Utils.handleException(e);

                            progressDialog.dismiss();

                            UIUtils.okDialog(EcoPointsProfileActivity.this,
                                    R.string.dialog_change_profile_picture_error_title,
                                    R.string.dialog_change_profile_picture_error_message,
                                    (dialogInterface, i) -> {

                                        dialogInterface.dismiss();
                                        dialog.dismiss();
                                    });
                        }

                        @Override
                        public void onNext(Void response) {
                            progressDialog.dismiss();
                            dialog.dismiss();

                            Glide.with(EcoPointsProfileActivity.this)
                                    .load(imageUrl)
                                    .into(profilePicture);

                            profileChanged = true;
                            newProfileUrl = imageUrl;
                        }
                    });
        });

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        dialog.show();

        EcoPointsApi api = RestClient.ADAPTER.create(EcoPointsApi.class);
        api.getProfilePictures()
                .subscribeOn(Schedulers.newThread())
                .map(response -> {
                    List<String> strings = new ArrayList<>();

                    for (String url : response.pictures) {
                        strings.add(response.directory + url);
                    }

                    response.pictures = strings;

                    return response;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ProfilePictureResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.handleException(e);
                    }

                    @Override
                    public void onNext(ProfilePictureResponse response) {
                        mItems.clear();
                        mItems.addAll(response.pictures);

                        adapter.notifyDataSetChanged();

                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void takePicture() {
        imageFile = new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg");

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    private void uploadPicture(File file) {
        RequestBody image = RequestBody.create(MediaType.parse("image/*"), file);

        ProgressDialog progressDialog = new ProgressDialog(this, R.style.DialogStyle);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.dialog_change_profile_picture_changing));
        progressDialog.show();

        EcoPointsApi api = RestClient.ADAPTER.create(EcoPointsApi.class);
        api.upload(image)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.handleException(e);

                        progressDialog.dismiss();

                        UIUtils.okDialog(EcoPointsProfileActivity.this,
                                R.string.dialog_change_profile_picture_error_title,
                                R.string.dialog_change_profile_picture_error_message,
                                (dialogInterface, i) -> dialogInterface.dismiss());
                    }

                    @Override
                    public void onNext(Void response) {
                        progressDialog.dismiss();

                        Glide.with(EcoPointsProfileActivity.this)
                                .load(file)
                                .into(profilePicture);

                        profileChanged = true;
                        newProfileFile = file;
                    }
                });
    }

    private void pickFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
    }

    private File getFileFromUri(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);

        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }

        return new File(result);
    }
}
