package com.example.notificationutils;

import static android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class SPCBridge extends Activity {

    String TITLE = "";
    String DESCRIPTION = "";
    String imageURL, mStatus;

    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true);
            setShowWhenLocked(true);
        } else {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            |FLAG_DISMISS_KEYGUARD );
        }*/
        setContentView(R.layout.layout_main);
        Log.i("notification", "SPCBridge called: ");

        // NotificationUtil.handleMediaPlayer(getApplicationContext(),true);

        TITLE = getIntent().getStringExtra("PUSH_TITLE");
        DESCRIPTION = getIntent().getStringExtra("PUSH_DES");
        imageURL = getIntent().getStringExtra("PUSH_IMAGE_URL");
        mStatus = getIntent().getStringExtra("STATUS");
        Log.e("TAG", "onCreate: TITLE:" + TITLE + " ,DESCRIPTION:" + DESCRIPTION + " ,imageURL:" + imageURL);


        ImageView btnLaunch = findViewById(R.id.btn_launch_app);
        ImageView btnClose = findViewById(R.id.btn_close_app);
        ImageView promoImageView = findViewById(R.id.iv_promo_image);
        ImageView promoFullImageView = findViewById(R.id.iv_full_imageView);

        TextView tvTitle = findViewById(R.id.tv_title);
        TextView tvDescription = findViewById(R.id.tv_description);
        TextView tvFullDescription = findViewById(R.id.tv_full_description);

        if (TITLE != null) {
            tvTitle.setText(TITLE);
        }
        switch (mStatus) {
            case "0": {
                promoImageView.setVisibility(View.VISIBLE);
                tvDescription.setVisibility(View.VISIBLE);
                if (DESCRIPTION != null) {
                    tvDescription.setText(DESCRIPTION);
                }
                Bitmap bitmap = ImageCacheManager.getInstance().getBitmapFromCache(imageURL);
                if (bitmap != null) {
                    promoImageView.setImageBitmap(bitmap);
                } else {
                    promoImageView.setImageResource(R.mipmap.place_holder);
                }


                break;
            }
            case "1": {
                // only show Full image
                promoFullImageView.setVisibility(View.VISIBLE);
                Bitmap bitmap = ImageCacheManager.getInstance().getBitmapFromCache(imageURL);
                if (bitmap != null) {
                    promoFullImageView.setImageBitmap(bitmap);
                } else {
                    promoFullImageView.setImageResource(R.mipmap.place_holder);
                }

                break;
            }
            case "2":
                // Only show Full description show
                tvFullDescription.setVisibility(View.VISIBLE);
                if (DESCRIPTION != null) {
                    tvFullDescription.setText(DESCRIPTION);
                }
                break;
        }


        btnLaunch.setOnClickListener(view -> {
            startActivity(getPackageManager().getLaunchIntentForPackage(getPackageName()));
            NotificationUtil.handleMediaPlayer(getApplicationContext(), false, 1);
            NotificationUtil.getManager(getApplicationContext()).cancel(NotificationUtil.NOTIFICATION_ID);
            finish();
        });

        btnClose.setOnClickListener(view -> {
            NotificationUtil.getManager(getApplicationContext()).cancel(NotificationUtil.NOTIFICATION_ID);
            NotificationUtil.handleMediaPlayer(getApplicationContext(), false, 2);
            finishAndRemoveTask();
            System.exit(0);
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e("TAG", "onKeyDown: " + keyCode);
        NotificationUtil.handleMediaPlayer(getApplicationContext(), false, 4);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.e("notification", "onBackPressed: ");
        NotificationUtil.handleMediaPlayer(getApplicationContext(), false, 5);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        Log.e("notification", "onUserLeaveHint: ");
        NotificationUtil.handleMediaPlayer(getApplicationContext(), false, 6);
    }

    @Override
    protected void onPause() {
        super.onPause();
        KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (myKM.isKeyguardLocked()) {
            //it is locked
            Log.e("notification", "device is locked: ");
        } else {
            //it is not locked
            Log.e("notification", "device not locked: ");
            NotificationUtil.handleMediaPlayer(getApplicationContext(), false, 7);
        }
    }


}
