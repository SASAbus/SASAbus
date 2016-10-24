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

package it.sasabz.android.sasabus.ui.ecopoints.event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.EncodeHintType;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import net.glxn.qrgen.android.QRCode;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.data.network.rest.model.Event;
import it.sasabz.android.sasabus.util.HashUtils;
import it.sasabz.android.sasabus.util.LogUtils;

public class QrCodeActivity extends RxAppCompatActivity {

    private static final String TAG = "QrCodeActivity";

    public static final String BROADCAST_QR_CODE_REDEEMED =
            "it.sasabz.android.sasabus.BROADCAST_QR_CODE_REDEEMED";

    @BindView(R.id.qr_code_image) ImageView qrCodeImage;
    @BindView(R.id.qr_code_success_animation) FrameLayout animationLayout;
    @BindView(R.id.qr_code_number_representation) TextView qrCodeNumber;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.e(TAG, "Got QR code broadcast");

            showDoneAnimation();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_qr_code);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        Event event = intent.getParcelableExtra(EventDetailsActivity.EXTRA_EVENT);

        if (event == null) {
            LogUtils.e(TAG, "Missing intent extra " + EventDetailsActivity.EXTRA_EVENT);
            finish();
            return;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.parseColor('#' + event.colorPrimary));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(view -> finish());

        int upColor = ContextCompat.getColor(this, event.lightStatusBar ?
                R.color.subtitle_on_primary : R.color.text_primary_light);

        Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material).mutate();
        upArrow.setColorFilter(upColor, PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        int titleColor = ContextCompat.getColor(this, event.lightStatusBar ?
                R.color.text_on_primary : R.color.text_primary_light);

        toolbar.setTitleTextColor(titleColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && event.lightStatusBar) {
            toolbar.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor('#' + event.colorPrimaryDark));
        }

        if (!event.redeemed) {
            generateQrCode(event.qrCode);

            animationLayout.setAlpha(0.5F);
            animationLayout.setScaleX(0);
            animationLayout.setScaleY(0);

            LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
                    new IntentFilter(BROADCAST_QR_CODE_REDEEMED));
        }

        generateNumber(event.qrCode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    private void generateQrCode(String code) {
        int px = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 164, getResources()
                .getDisplayMetrics()) + 0.5);

        Bitmap bitmap = QRCode.from(code)
                .withSize(px, px)
                .withHint(EncodeHintType.MARGIN, 0)
                .withCharset("UTF-8")
                .bitmap();

        qrCodeImage.setImageBitmap(bitmap);
    }

    private void generateNumber(String code) {
        StringBuilder stringBuilder = new StringBuilder();

        String hashed = HashUtils.sha256Hex(code);
        hashed = hashed.substring(0, 60);

        for (int i = 0; i < hashed.length(); i++) {
            char c = hashed.charAt(i);
            Random random = new Random(c);

            int number = random.nextInt(9);

            stringBuilder.append(number);

            if (i < 59 && (i + 1) % 20 == 0) {
                stringBuilder.append('\n');
            } else if (i < 59 && (i + 1) % 5 == 0) {
                stringBuilder.append(' ');
            }
        }

        qrCodeNumber.setText(stringBuilder.toString());
    }

    private void showDoneAnimation() {
        animationLayout.setAlpha(0.5F);
        animationLayout.setScaleX(0);
        animationLayout.setScaleY(0);

        animationLayout.animate()
                .scaleX(1)
                .scaleY(1)
                .alpha(1)
                .setStartDelay(1000)
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator(1.1F))
                .start();
    }
}
