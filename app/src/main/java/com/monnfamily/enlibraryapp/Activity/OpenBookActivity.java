package com.monnfamily.enlibraryapp.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.monnfamily.enlibraryapp.Constants.Constant;
import com.monnfamily.enlibraryapp.R;
import com.monnfamily.enlibraryapp.Utils.App;
import com.monnfamily.enlibraryapp.Utils.AppManager;
import com.monnfamily.enlibraryapp.Utils.SoundManager;

import java.io.File;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OpenBookActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "OpenBookActivity";
    @BindView(R.id.imgAutoPlay)
    ImageView img_AutoPlay;
    @BindView(R.id.imgReadMyself)
    ImageView img_RaedMyself;
    @BindView(R.id.imgBookMain)
    ImageView img_Bookmain;
    @BindView(R.id.lay_button)
    LinearLayout layBottom;
    @BindView(R.id.FTUEautoplay)
    ImageView ftueAutoplay;

    AppManager manager = AppManager.getInstance();

    Integer mBookID;
    Handler handler = new Handler();


    private Runnable mTimerCallback;

    private boolean mAppEnteredBG = false;
    // variable for handling multiple clicks

    private boolean mIsButtonClicked = false;
    public static final OpenBookActivity mInstance = new OpenBookActivity();

    public static OpenBookActivity getInstance() {
        return mInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_book);

        ButterKnife.bind(this);

        img_AutoPlay.setOnClickListener(this);
        img_RaedMyself.setOnClickListener(this);
        layBottom.setVisibility(View.VISIBLE);

        ftueAutoplay.setVisibility(View.GONE);

        Intent intent = getIntent();

        mBookID = intent.getIntExtra("bookId", 0);
        String tMainFolder = App.get().getBookDir() + "/BookCompleted" + mBookID;

        File imgFile = new File(tMainFolder, Constant.BOOK_MAIN_IMAGE + ".png");
        if (imgFile.exists()) {
            Bitmap bookImage = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            img_Bookmain.setImageBitmap(bookImage);
        }


        File soundFile = new File(tMainFolder, Constant.BOOK_SOUND + ".mp3");
        if (soundFile.exists()) {
            SoundManager.getInstance().playBackgroundMusic(soundFile.getPath());
        }
        //Timer. // 700ms
        mTimerCallback = new Runnable() {
            @Override
            public void run() {
                manager.setNeedsBGMusic(true);
                onClick(img_AutoPlay);
                finish();
            }
        };
//        App.get().volleyGet();
    }


    @Override
    public void onClick(View v) {
        //Stop timer.
        handler.removeCallbacks(mTimerCallback);
        if (mIsButtonClicked) return;
        mIsButtonClicked = true;
        Intent intent;
        manager.setmPageNumber(1);
        switch (v.getId()) {
            case R.id.imgAutoPlay:
            case R.id.FTUEautoplay:
                intent = new Intent(OpenBookActivity.this, PageActivity.class);
//                    intent.putExtra("fromButton", true);
                manager.setAutoPlay(true);
                intent.putExtra("mBookID", mBookID);
                this.finish();
                startActivity(intent);
                overridePendingTransition(R.anim.side_in_right, R.anim.side_out_left);
                break;
            case R.id.imgReadMyself:
                intent = new Intent(OpenBookActivity.this, PageActivity.class);
//                    intent.putExtra("fromButton", false);
                manager.setAutoPlay(false);
                intent.putExtra("mBookID", mBookID);
                this.finish();
                startActivity(intent);
                overridePendingTransition(R.anim.side_in_right, R.anim.side_out_left);
                break;
        }
    }

    // transition is applied when back button is pressed
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.side_in_left, R.anim.side_out_right);

    }

    @Override
    protected void onResume() {
        mIsButtonClicked = false;
        super.onResume();

    }

    @Override
    protected void onPause() {
        SoundManager.getInstance().pauseBackgroundMusic();
        mAppEnteredBG = true;
        handler.removeCallbacks(mTimerCallback);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (manager.mPreferences.getBoolean("IsTutorialOver", true)) {
            handler.removeCallbacks(mTimerCallback);
            SoundManager.getInstance().stopBackgroundMusic();
            super.onBackPressed();
        }
    }

}
