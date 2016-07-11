package it.sasabz.android.sasabus.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import it.sasabz.android.sasabus.BuildConfig;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.sync.SyncHelper;
import it.sasabz.android.sasabus.util.AnalyticsHelper;
import it.sasabz.android.sasabus.util.CustomTabsHelper;
import it.sasabz.android.sasabus.util.Utils;

/**
 * Shows the credits of this app, like the developers and used libraries.
 *
 * @author Alex Lardschneider
 */
public class CreditsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CreditsActivity";

    private CustomTabsHelper mTabsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.changeLanguage(this);

        setContentView(R.layout.activity_credits);

        AnalyticsHelper.sendScreenView(TAG);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView version = (TextView) findViewById(R.id.credits_version);
        if (version != null) {
            version.setText('v' + BuildConfig.VERSION_NAME);
        }

        ImageView icon = (ImageView) findViewById(R.id.credits_app_icon);

        icon.setOnLongClickListener(v -> {
            new SyncHelper(this).performSyncAsync();

            Toast.makeText(this, "Forced sync", Toast.LENGTH_SHORT).show();

            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(250);

            AnalyticsHelper.sendEvent(TAG, "Forced sync");

            return true;
        });

        LinearLayout alex = (LinearLayout) findViewById(R.id.credits_alex);
        LinearLayout david = (LinearLayout) findViewById(R.id.credits_david);
        TextView email = (TextView) findViewById(R.id.credits_email);
        CardView okhttp = (CardView) findViewById(R.id.credits_okhttp);
        CardView retrofit = (CardView) findViewById(R.id.credits_retrofit);
        CardView appcompat = (CardView) findViewById(R.id.credits_appcompat);
        CardView crashlytics = (CardView) findViewById(R.id.credits_crashlytics);
        CardView altbeacon = (CardView) findViewById(R.id.credits_altbeacon);

        alex.setOnClickListener(this);
        david.setOnClickListener(this);
        email.setOnClickListener(this);
        okhttp.setOnClickListener(this);
        retrofit.setOnClickListener(this);
        appcompat.setOnClickListener(this);
        crashlytics.setOnClickListener(this);
        altbeacon.setOnClickListener(this);

        mTabsHelper = new CustomTabsHelper(this);
        mTabsHelper.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mTabsHelper.stop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.credits_alex:
                mTabsHelper.launchUrl(Uri.parse("https://plus.google.com/u/1/+AlexLardschneider/posts"));
                break;
            case R.id.credits_david:
                mTabsHelper.launchUrl(Uri.parse("https://plus.google.com/102078461347682395386/posts"));
                break;
            case R.id.credits_email:
                startActivity(new Intent(this, AboutActivity.class).putExtra("dialog_report", true));
                break;
            case R.id.credits_okhttp:
                mTabsHelper.launchUrl(Uri.parse("http://square.github.io/okhttp/"));
                break;
            case R.id.credits_retrofit:
                mTabsHelper.launchUrl(Uri.parse("http://square.github.io/retrofit/"));
                break;
            case R.id.credits_appcompat:
                mTabsHelper.launchUrl(Uri.parse("https://developer.android.com/tools/support-library/features.html"));
                break;
            case R.id.credits_crashlytics:
                mTabsHelper.launchUrl(Uri.parse("https://crashlytics.com/"));
                break;
            case R.id.credits_altbeacon:
                mTabsHelper.launchUrl(Uri.parse("http://altbeacon.org/"));
                break;
        }
    }
}