package com.monnfamily.enlibraryapp.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import androidx.core.content.FileProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.monnfamily.enlibraryapp.Constants.Constant;
import com.monnfamily.enlibraryapp.Contentful.Book;
import com.monnfamily.enlibraryapp.Contentful.Page;
import com.monnfamily.enlibraryapp.R;
import com.monnfamily.enlibraryapp.Utils.App;
import com.monnfamily.enlibraryapp.Utils.AppManager;
import com.monnfamily.enlibraryapp.Utils.FragmentName;
import com.monnfamily.enlibraryapp.Utils.LinkUtilities;
import com.monnfamily.enlibraryapp.Utils.ReadOutTextAnimation;
import com.monnfamily.enlibraryapp.Utils.ScreenshotType;
import com.monnfamily.enlibraryapp.Utils.ScreenshotUtils;
import com.monnfamily.enlibraryapp.Utils.SoundManager;
import com.romainpiel.shimmer.BuildConfig;
import com.romainpiel.shimmer.ShimmerTextView;

import java.io.File;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PageActivity extends AppCompatActivity implements View.OnClickListener,
        ReadOutTextAnimation.ReadingPageCompleted,
        GestureDetector.OnGestureListener {

    public static final String TAG = "PAGE_VIEW";
    @BindView(R.id.page_view_backbtn)
    Button btn_back;
    @BindView(R.id.page_view_palybtn)
    Button btn_play;
    @BindView(R.id.page_view_nextbtn)
    Button btn_next;
    @BindView(R.id.page_view_share)
    Button btn_share;
    @BindView(R.id.home_page)
    Button btn_home;
    @BindView(R.id.page_view_startover)
    Button btn_startover;
    @BindView(R.id.img_pageImage)
    ImageView img_pageImage;

    @BindView(R.id.page_view_textview)
    ShimmerTextView txt_pageContent;
    @BindView(R.id.rootcontent)
    RelativeLayout rootContent;
    boolean isAutoPlay;
    @BindView(R.id.layoutanimation)
    View myView;

    private Handler handler = new Handler();
    private Handler tapHandler = new Handler();
    private Runnable mTimerCallback;
    private GestureDetector gestureDetector;
    private boolean isUp;
    private boolean isRunningSlideAnimation;
    private static Integer mPageNumber = 1;
    private String mMainFolder;
    private Integer mBookID;
    private boolean mIsPaused;
    static private int STATIC_RESULT = 1002;
    boolean isFtueNetworkPopup = false;
    Book mCurrentBookRead;

    // variable for handling multiple clicks
    private static boolean isNetworkPopupVisible = false;

    AppManager manager = AppManager.getInstance();

    public static PageActivity mInstance;

    public static PageActivity getInstance() {
        return mInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mInstance = this;
        setContentView(R.layout.activity_page);
        ButterKnife.bind(this);


        Intent intent = getIntent();
//        isAutoPlay = intent.getBooleanExtra("fromButton", true);

        isAutoPlay = manager.isAutoPlay();
        mBookID = intent.getIntExtra("mBookID", 0);

        //Getting Address of Book and Setting it to currentBook
        mMainFolder = App.get().getBookDir() + "/BookCompleted" + mBookID;

        if (!manager.mPreferences.getBoolean("IsTutorialOver", true)) {
            mCurrentBookRead = manager.getCurrentBook();
        } else {
            mCurrentBookRead = manager.getBookForID(mBookID);
            manager.setCurrentBook(manager.getBookForID(mBookID));
            myView.setVisibility(View.INVISIBLE);
        }
        // TODO  For Ftue Book get the book setted in app manager to mCurrentBook Read


        mPageNumber = manager.getmPageNumber();                 //Initial Setting counter to page 1

        //Setting OnClickListener
        if (isAutoPlay) {
            btn_play.setOnClickListener(this);
        } else {
            btn_play.setBackgroundResource(R.drawable.playnew);
            btn_play.setOnClickListener(null);
        }
        if (manager.mPreferences.getBoolean("IsTutorialOver", true)) {
            btn_back.setOnClickListener(this);
            btn_next.setOnClickListener(this);
            btn_share.setOnClickListener(this);
            btn_home.setOnClickListener(this);
            btn_startover.setOnClickListener(this);
        } else {
            btn_share.setAlpha(0.5f);
            btn_home.setAlpha(0.5f);
            btn_startover.setAlpha(0.5f);
            btn_next.setAlpha(0.5f);
            btn_back.setAlpha(0.5f);
            btn_share.setVisibility(View.GONE);
            btn_startover.setVisibility(View.GONE);
            btn_next.setVisibility(View.GONE);
            btn_back.setVisibility(View.GONE);
//            btn_play.setVisibility(View.GONE);
            btn_home.setVisibility(View.GONE);
//            manager.setmPageNumber(1);
        }
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        gestureDetector = new GestureDetector(this, this);
//        myView = findViewById(R.id.layoutanimation);


        Typeface face = Typeface.createFromAsset(getAssets(), "font/englisharial.ttf");
        txt_pageContent.setTypeface(face);
        ReadOutTextAnimation.getInstance().setShimmerTextView(txt_pageContent);
        //Setting Control Panal Visible To user at First
        isUp = false;
        isRunningSlideAnimation = false;
        slideUp(myView);
        tapHandler.postDelayed(() -> {
            slideDown(myView);
            btn_share.setClickable(false);
            btn_startover.setClickable(false);
            btn_next.setClickable(false);
            btn_back.setClickable(false);
            btn_play.setClickable(false);
            btn_home.setClickable(false);
            isUp = !isUp;
        }, 8000);

        mMainFolder = App.get().getBookDir() + "/BookCompleted" + mBookID;

        ReadOutTextAnimation.getInstance().setListner(this);
        rootContent = findViewById(R.id.rootcontent);
        //Timer 700 ms.
        mTimerCallback = new Runnable() {
            @Override
            public void run() {
                checkToLoadNextPage();
            }
        };
        checkNetworkAvailability();
        loadPageDetails();
        enableBtn();
//        App.get().volleyGet();
    }


    /**
     * @param v defines the view clicked
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.page_view_backbtn && mPageNumber == 1) {
            enableBtn();
            return;
        }

        long now = SystemClock.elapsedRealtime();

        if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {
            manager.setLastClickMillis(now);
            switch (v.getId()) {
                //Define Function on click of back Button
                case R.id.page_view_backbtn:
                    loadPreviousPage();
                    break;
                //Define Function on click of play Button
                case R.id.page_view_palybtn:

                    if (isAutoPlay) {
                        if (mIsPaused) {
                            resumeApp();
                        } else {
                            pauseApp();
                        }
                    }
                    break;
                //Define Function on click of next Button
                case R.id.page_view_nextbtn:
                    checkToLoadNextPage();
                    break;
                //Define Function on click of startOver Button
                case R.id.page_view_startover:

                    mPageNumber = 1;
                    manager.setmPageNumber(mPageNumber);
                    enableBtn();
                    loadPageDetails();
                    break;
                //Define Function on click of Share Button
                case R.id.page_view_share:
                    if (isAutoPlay)
                        pauseApp();
                    fourParameterDialog(R.layout.popup_share_book_page, R.id.img_popSharePage, R.id.img_popShareBook, R.id.img_popShareApp, R.id.img_popShareCancel);
                    break;
                //Define Function on click of Home Button
                case R.id.home_page:
                    onBackPressed();
//                    LibraryViewActivity.get().loadLastFragment();
                    break;

            }
        }


    }

    // transition is applied when back button is pressed
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.side_in_left, R.anim.side_out_right);

    }

    //Popup Dialogs
    private void fourParameterDialog(int dialogContainer, int oneView, int twoView, int threeView, int fourView) {
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(dialogContainer, viewGroup, false);
        ImageView firstView = dialogView.findViewById(oneView);
        ImageView secondView = dialogView.findViewById(twoView);
        ImageView thirdView = dialogView.findViewById(threeView);
        TextView fourthView = dialogView.findViewById(fourView);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        alertDialog.setCancelable(false);
        //Function Performed When "Share this Page" pressed

        firstView.setOnClickListener(view -> {

            long now = SystemClock.elapsedRealtime();

            if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {
                manager.setLastClickMillis(now);
                takeScreenshot(ScreenshotType.CUSTOM);
                alertDialog.dismiss();
            }
        });
        //Function Performed When "Share this Book" pressed
        secondView.setOnClickListener(v -> {
            long now = SystemClock.elapsedRealtime();

            if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {
                manager.setLastClickMillis(now);
                Book mCurrentBook = manager.getCurrentBook();
                String mMainFolder = String.valueOf(App.get().getAppContext().getDir("MonnFamily", Context.MODE_PRIVATE));
                mMainFolder += "/DownloadedBooks/BookCompleted" + mCurrentBook.getBookId().toString();

                File imgFile = new File(mMainFolder, Constant.BOOK_MAIN_IMAGE + ".png");

                if (imgFile.isFile()) {
                    Bitmap bookImage = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    File saveFile = ScreenshotUtils.getMainDirectoryName(PageActivity.this);
//                    File file = ScreenshotUtils.store(bookImage, "screenshot_Book_Image.jpg", saveFile);
                    File file = ScreenshotUtils.store(bookImage, "The_Lovelands_Books.jpg", saveFile);
                    nativeShare(FileProvider.getUriForFile(PageActivity.this, getApplicationContext().getPackageName() + ".fileprovider", file));
                } else
                    Toast.makeText(PageActivity.this, R.string.screenshot_take_failed, Toast.LENGTH_SHORT).show();

                alertDialog.dismiss();
            }
        });
        //Function Performed When "Share this App" pressed
        thirdView.setOnClickListener(v -> {
            long now = SystemClock.elapsedRealtime();

            if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {
                manager.setLastClickMillis(now);
                alertDialog.dismiss();
                LinkUtilities.shareThisApp(PageActivity.this);
            }
        });
        //Function Performed When "Cancel" pressed
        fourthView.setOnClickListener(v -> {
            long now = SystemClock.elapsedRealtime();

            if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {
                manager.setLastClickMillis(now);

                alertDialog.dismiss();
            }
        });


    }

    private void nativeShare(Uri uri) {
        final String appPackageName = BuildConfig.APPLICATION_ID;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String shareBodyText = getString(R.string.share_body) + "\n\n https://play.google.com/store/apps/details?id=" + appPackageName;
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText);
        if (uri != null) {
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);//pass uri here
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            shareIntent.setType("text/plain");
        }
        startActivityForResult(Intent.createChooser(shareIntent, getString(R.string.share_subject)), STATIC_RESULT);
    }

    private void threeParameterDialog(int dialogContainer, int oneView, int twoView, int threeView) {
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(dialogContainer, viewGroup, false);

        TextView firstView = dialogView.findViewById(oneView);
        TextView secondView = dialogView.findViewById(twoView);
        TextView thirdView = dialogView.findViewById(threeView);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        alertDialog.setCancelable(false);//Function Performed When "YES" pressed
        App.get().getPurchaseHelper().appResumed();
        if (App.get().getPurchaseHelper().isAlreadySubscribed()||manager.isAppFree()) {
            secondView.setEnabled(false);
            secondView.getBackground().setAlpha(100);
        }
        firstView.setOnClickListener(view -> {
            long now = SystemClock.elapsedRealtime();

            if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {
                manager.setLastClickMillis(now);
                LinkUtilities.rateThisApp(PageActivity.this);
                alertDialog.dismiss();
                finish();
            }
        });
        //Function Performed When "NO" pressed
        secondView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = SystemClock.elapsedRealtime();

                if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {
                    manager.setLastClickMillis(now);
                    alertDialog.dismiss();
                    manager.setFragmentName(FragmentName.SUBSCRIBE);
                    onBackPressed();
                }
            }
        });
        thirdView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = SystemClock.elapsedRealtime();

                if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {
                    manager.setLastClickMillis(now);
                    alertDialog.dismiss();
                    onBackPressed();
                }

            }
        });


    }


    private void twoParameterDialog(int dialogContainer, int yesView, int noView, boolean isForFTUE) {
        pauseApp();
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(dialogContainer, viewGroup, false);
        ImageView mOkFinish = dialogView.findViewById(yesView);
        ImageView mNoBtn = dialogView.findViewById(noView);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        alertDialog.setCancelable(false);
        if (isForFTUE) {
            isFtueNetworkPopup = true;
            mNoBtn.setVisibility(View.GONE);
            mOkFinish.setVisibility(View.GONE);
            dialogView.findViewById(R.id.img_popRetryForFTUE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long now = SystemClock.elapsedRealtime() + 500;
                    if (now - manager.getLastClickMillis() < Constant.THRESHOLD_MILLIS) {
                        manager.setLastClickMillis(now);
                        return;
                    }
                    alertDialog.dismiss();
                    manager.setNeedsUpdateFromCMS(false);
                    LibraryViewActivity.get().subscribeForSyncResults();
                    LibraryViewActivity.requestSync();
                    resumeApp();

                    checkNetworkAvailability();

                }
            });
            dialogView.findViewById(R.id.img_popRetryForFTUE).setVisibility(View.VISIBLE);
        }
        //Function Performed When "YES" pressed
        mOkFinish.setOnClickListener(view -> {
            long now = SystemClock.elapsedRealtime();

            if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {
                manager.setLastClickMillis(now);

                alertDialog.dismiss();
                manager.setFragmentName(FragmentName.SUBSCRIBE);
                manager.setNeedSubscription(true);
                onBackPressed();
                finish();
            }
        });
        //Function Performed When "NO" pressed
        mNoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = SystemClock.elapsedRealtime();

                if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {
                    manager.setLastClickMillis(now);
                    alertDialog.dismiss();
                    onBackPressed();

                }
            }
        });
    }

    private void takeScreenshot(ScreenshotType screenshotType) {
        Bitmap b = null;
        switch (screenshotType) {
            case FULL:
                b = ScreenshotUtils.getScreenShot(rootContent);
                break;
            case CUSTOM:
                b = ScreenshotUtils.getScreenShot(rootContent);
                break;
        }


        if (b != null) {
            File saveFile = ScreenshotUtils.getMainDirectoryName(this);
            File file = ScreenshotUtils.store(b, "The_Lovelands_Books.jpg", saveFile);
            shareScreenshot(FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", file));
        } else

            Toast.makeText(this, R.string.screenshot_take_failed, Toast.LENGTH_SHORT).show();

    }

    private void shareScreenshot(Uri uri) {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_page_subject));
        intent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.share_body));
        if (uri != null) {
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, uri);//pass uri here
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.setType("text/plain");
        }
        startActivityForResult(Intent.createChooser(intent, getString(R.string.share_title)), 1003);
    }


    //Listener for the reading Current page is Completed
    @Override
    public void readingPageCompleted() {
        handler.postDelayed(mTimerCallback, 700);
    }

    //Function Slide Up the Control Panel
    public void slideUp(View view) {
        isRunningSlideAnimation = true;
        view.setVisibility(View.VISIBLE);
        btn_play.setVisibility(View.VISIBLE);
        if (manager.mPreferences.getBoolean("IsTutorialOver", true)) {
            btn_share.setVisibility(View.VISIBLE);
            btn_startover.setVisibility(View.VISIBLE);
            btn_next.setVisibility(View.VISIBLE);
            btn_back.setVisibility(View.VISIBLE);
            btn_home.setVisibility(View.VISIBLE);
        }
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isRunningSlideAnimation = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animate);
    }

    // slide the view from its current position to below itself
    public void slideDown(View view) {
        isRunningSlideAnimation = true;
        view.setVisibility(View.INVISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isRunningSlideAnimation = false;
                btn_play.setVisibility(View.GONE);
                btn_share.setVisibility(View.GONE);
                btn_startover.setVisibility(View.GONE);
                btn_next.setVisibility(View.GONE);
                btn_back.setVisibility(View.GONE);
                btn_home.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animate);
    }


    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    //This Function Execute When user Click On the Screen Other then Control section
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (isRunningSlideAnimation) return false;

        if (isUp) {
            tapHandler.removeCallbacksAndMessages(null);
            slideUp(myView);
            btn_share.setClickable(true);
            btn_startover.setClickable(true);
            btn_next.setClickable(true);
            btn_back.setClickable(true);
            btn_play.setClickable(true);
            btn_home.setClickable(true);

            tapHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    slideDown(myView);
                    btn_share.setClickable(false);
                    btn_startover.setClickable(false);
                    btn_next.setClickable(false);
                    btn_back.setClickable(false);
                    btn_play.setClickable(false);
                    btn_home.setClickable(false);

                    isUp = !isUp;
                }
            }, 8000);
        } else {
            tapHandler.removeCallbacksAndMessages(null);
            slideDown(myView);
            btn_share.setClickable(false);
            btn_startover.setClickable(false);
            btn_next.setClickable(false);
            btn_back.setClickable(false);
            btn_play.setClickable(false);
            btn_home.setClickable(false);
        }
        isUp = !isUp;
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float velocityX, float velocityY) {
        if (!manager.mPreferences.getBoolean("IsTutorialOver", true))
            return false;
        long now = SystemClock.elapsedRealtime();

        if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {
            manager.setLastClickMillis(now);
            if (motionEvent.getX() - motionEvent1.getX() > 50) {
                checkToLoadNextPage();
                return true;
            }

            if (motionEvent1.getX() - motionEvent.getX() > 50 && mPageNumber != 1) {
                loadPreviousPage();
                return true;
            }

        }
        return false;
    }

    private void checkToLoadNextPage() {
        handler.removeCallbacks(mTimerCallback);
        mPageNumber++;

        manager.setmPageNumber(mPageNumber);
//        if (!App.get().getPurchaseHelper().isAlreadySubscribed() && mPageNumber > Constant.UNSUBSCRIBE_PAGE_COUNT) { //Previous Code

        //Whether the app is free(move to next page) or not (charge)
        if (!manager.isAppFree()) {
            if ((!mCurrentBookRead.getBookForFree()
                    && !App.get().getPurchaseHelper().isAlreadySubscribed())
                    && mPageNumber == Constant.UNSUBSCRIBE_PAGE_COUNT) {                   //Code For Free Books
                closeAllResources();
                //Add Popup here Of Book Complete here
                twoParameterDialog(R.layout.popup_subscribe_alert, R.id.img_yesbutton, R.id.img_cancelbutton, false);
                return;
            }
        }
            loadNextPage();

    }

    private void loadNextPage() {
        if (mPageNumber > manager.getCurrentBook().getAllPage().size()) {
            mPageNumber = manager.getCurrentBook().getAllPage().size();

            closeAllResources();
            //Add Popup here Of Book Complete here
            if (!manager.mPreferences.getBoolean("IsTutorialOver", true)) {
                manager.mPreferences.edit().putBoolean("IsTutorialOver", true).apply();
                onBackPressed();
                return;
            }
            threeParameterDialog(R.layout.popup_book_complete,
                    R.id.img_popRateTheApp,
                    R.id.img_popSubscribe,
                    R.id.img_popDone);
        } else {
            checkNetworkAvailability();
            loadPageDetails();
            enableBtn();
        }
    }

    private void loadPreviousPage() {
        mPageNumber--;
        manager.setmPageNumber(mPageNumber);
        handler.removeCallbacks(mTimerCallback);
        if (mPageNumber < 1) {
            mPageNumber = 1;
        } else {
            loadPageDetails();
        }
        enableBtn();
    }

    private void loadPageDetails() {
        try {
            ReadOutTextAnimation.getInstance().stopReadOut();
            Page mCurrentPage = manager.getCurrentBook().getPageDetailsForNumber(mPageNumber);
            // Check if audio duration data is available for text animation
            boolean hasAudioDuration = mCurrentPage.getPageAudioDurationJSON() != null;
            File imgFile = new File(App.get().getBookPath(mBookID + ""), Constant.BOOK_PAGE_IMAGE + mPageNumber + ".png");
            if (imgFile.exists()) {
                Bitmap bookImage = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                Bitmap.createScaledBitmap(bookImage, 50, 50, false);
                img_pageImage.setImageBitmap(bookImage);
            }

            File soundFile = new File(mMainFolder, Constant.BOOK_PAGE_AUDIO + mPageNumber.toString() + ".mp3");
            if (soundFile.exists()) {
                if (isAutoPlay) {
                    SoundManager.getInstance().playPageAudio(soundFile.getPath());
                } else {
                    SoundManager.getInstance().pausePageAudio();
                }
            }
            SoundManager.getInstance().playPageAudio(soundFile.getPath());

            ReadOutTextAnimation.getInstance().startReadOut(mPageNumber);
            if (soundFile.exists()) {
                if (isAutoPlay) {
                    SoundManager.getInstance().playPageAudio(soundFile.getPath());

                } else {
                    SoundManager.getInstance().pausePageAudio();
                    ReadOutTextAnimation.getInstance().setStringForIndexMute(mPageNumber);
                    ReadOutTextAnimation.getInstance().stopReadOut();
                }
            }
            if (!mIsPaused) {
                Integer timeSkip = ReadOutTextAnimation.getInstance().getElaspeTime();
                SoundManager.getInstance().resumePageAudio(timeSkip);
                ReadOutTextAnimation.getInstance().resumeReadOut();
                enableBtn();
                if (isAutoPlay)
                    btn_play.setBackgroundResource(R.drawable.pause);
                if (soundFile.exists()) {
                    if (isAutoPlay) {
                        SoundManager.getInstance().playPageAudio(soundFile.getPath());

                    } else {
                        btn_play.setAlpha(.5f);
                        btn_back.setEnabled(false);
                        SoundManager.getInstance().pausePageAudio();
                        ReadOutTextAnimation.getInstance().setStringForIndexMute(mPageNumber);
                        ReadOutTextAnimation.getInstance().stopReadOut();
                    }
                }
            } else {
                handler.removeCallbacks(mTimerCallback);
                SoundManager.getInstance().pausePageAudio();
                SoundManager.getInstance().pauseBackgroundMusic();
                ReadOutTextAnimation.getInstance().pauseReadOut();
                if (isAutoPlay)
                    btn_play.setBackgroundResource(R.drawable.playnew);
            }


        } catch (Exception e) {
            // Log error but don't crash - allow page to display even with partial content
            Log.e("PageActivity", "Error loading page details: " + e.getMessage());
            e.printStackTrace();
            // Keep buttons visible so user can navigate
            enableBtn();
        }
    }

    @Override
    public void onBackPressed() {
        if (manager.mPreferences.getBoolean("IsTutorialOver", true)) {
            closeAllResources();
            super.onBackPressed();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        pauseApp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!manager.mPreferences.getBoolean("IsTutorialOver", true))
            resumeApp();
    }

    private void pauseApp() {
        handler.removeCallbacks(mTimerCallback);
        SoundManager.getInstance().pausePageAudio();
        SoundManager.getInstance().pauseBackgroundMusic();
        ReadOutTextAnimation.getInstance().pauseReadOut();
        mIsPaused = true;
        if (isAutoPlay)
            btn_play.setBackgroundResource(R.drawable.playnew);
    }

    private void resumeApp() {
        Integer timeSkip = ReadOutTextAnimation.getInstance().getElaspeTime();
        SoundManager.getInstance().resumePageAudio(timeSkip);
        ReadOutTextAnimation.getInstance().resumeReadOut();
        enableBtn();
        mIsPaused = false;
        if (isAutoPlay)
            btn_play.setBackgroundResource(R.drawable.pause);
    }

    private void disableBtn() {
        btn_next.setEnabled(false);
        btn_back.setEnabled(false);
    }

    private void enableBtn() {
        if (mPageNumber > 1) {
            btn_back.setEnabled(true);
            btn_next.setEnabled(true);
            if (manager.mPreferences.getBoolean("IsTutorialOver", true))
                btn_back.setAlpha(.9f);
        } else {
            btn_back.setAlpha(.5f);
            btn_next.setEnabled(true);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // TODO Auto-generated method stub
        gestureDetector.onTouchEvent(motionEvent);
        return false;
    }

    public void closeAllResources() {
        ReadOutTextAnimation.getInstance().setListner(null);
        handler.removeCallbacks(mTimerCallback);
        ReadOutTextAnimation.getInstance().stopReadOut();
        SoundManager.getInstance().stopALL();
    }

    protected void checkNetworkAvailability() {
        try {
            boolean isTrue = manager.mPreferences.getBoolean("IsTutorialOver", true);
            if (!SplashActivity.getInstance().isNetworkAvailable() &&
                    !isTrue) {

                twoParameterDialog(R.layout.popup_network_error, R.id.img_popRetry, R.id.img_popCancel, true);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
