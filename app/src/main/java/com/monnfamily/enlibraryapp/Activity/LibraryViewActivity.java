package com.monnfamily.enlibraryapp.Activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.contentful.vault.SyncConfig;
import com.contentful.vault.Vault;
import com.monnfamily.enlibraryapp.Constants.Constant;
import com.monnfamily.enlibraryapp.Contentful.Book;
import com.monnfamily.enlibraryapp.Contentful.Cast;
import com.monnfamily.enlibraryapp.Contentful.Category;
import com.monnfamily.enlibraryapp.Contentful.ContentfulClient;
import com.monnfamily.enlibraryapp.Contentful.ContentfulSpaceLink;
import com.monnfamily.enlibraryapp.Contentful.MakeAppFree;
import com.monnfamily.enlibraryapp.Fragment.CastFragment;
import com.monnfamily.enlibraryapp.Fragment.FavoraiteFragment;
import com.monnfamily.enlibraryapp.Fragment.HomeFragment;
import com.monnfamily.enlibraryapp.Fragment.SearchFragment;
import com.monnfamily.enlibraryapp.Fragment.SubscribeFragment;
import com.monnfamily.enlibraryapp.R;
import com.monnfamily.enlibraryapp.Utils.App;
import com.monnfamily.enlibraryapp.Utils.AppManager;
import com.monnfamily.enlibraryapp.Utils.DownloadContentManager;
import com.monnfamily.enlibraryapp.Utils.FragmentName;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class LibraryViewActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener,
        SearchFragment.OnFragmentInteractionListener,
        FavoraiteFragment.OnFragmentInteractionListener,
        SubscribeFragment.OnFragmentInteractionListener,
        CastFragment.OnFragmentInteractionListener,
        DownloadContentManager.DownloadCompletedListner,
        View.OnClickListener {
    private static final String TAG = "LibraryViewActivity";
    @BindView(R.id.toolBar_activity)
    Toolbar toolbar;

//    @BindView(R.id.relative_Search)
//    RelativeLayout svToolbar;
//
//    @BindView(R.id.img_toolbarTile)
//    ImageView imgToolbar;

    @BindView(R.id.img_shadowBottom)
    ImageView img_shadowTop;

    @BindView(R.id.tabShadow)
    ImageView img_shadowBottom;


    static ImageView mTempImageReference;

    @BindView(R.id.tab_home)
    ImageView tab_home;
    @BindView(R.id.tab_cast)
    ImageView tab_cast;
    @BindView(R.id.tab_search)
    ImageView tab_search;
    @BindView(R.id.tab_subscribe)
    public ImageView tab_subscribe;
    @BindView(R.id.tab_favorites)
    ImageView tab_favorites;

    View view;
    FragmentManager fragmentManager;
    static Fragment fragment;
    FragmentTransaction fragmentTransaction;

    static App app = App.get();
    private ProgressDialog mInitialDialog;

    static boolean loadfragment = false;

    private static final Vault vault = Vault.with(app, ContentfulSpaceLink.class);
    Context context = LibraryViewActivity.this;

    public static boolean isAllIconDone = false;


    LinearLayout tabLayout;

    Long time = 0L;


    private static LibraryViewActivity mInstance = new LibraryViewActivity();

    // variable for handling multiple clicks
    private static final long THRESHOLD_MILLIS = 400L;
    private long lastClickMillis;

    /**
     * Check For the first Network popup
     */
    public static boolean isNetworkPopupAppear = false;
    private boolean mIsSettingClicked = false;

    public static LibraryViewActivity get() {
        return mInstance;
    }

    int[] mWaitingImage = {
            R.drawable.pleasewait,
            R.drawable.bitmore,
            R.drawable.almost
    };
    int mWaitingImageIndex = 0;
    ConstraintLayout popupImage;
    ProgressBar bar1, bar2;
    CountDownTimer countDownTimer;
    long timeLeftInMille = 0;
    AlertDialog alertDialog1, alertDialog;
    static AppManager manager = AppManager.getInstance();

    public static boolean isSyncSuccess = true;
    public static boolean isSyncProgress = false;
    public boolean isLaterButtonCLicked = false;

    /*
     * Protected Functions
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_view);
        mInstance = this;
        this.view = findViewById(R.id.relative_LibraryView);
        tabLayout = findViewById(R.id.lin_customTab);
//        manager.setSelectedCategoy(1);
//        manager.setSelectedCast(1);
        ButterKnife.bind(this);

        //setting tool bar
        toolbar.inflateMenu(R.menu.action_bar_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            if (mIsSettingClicked) return false;
            mIsSettingClicked = true;
            switch (item.getItemId()) {
                case R.id.actionbar_setup:
                    Intent intent;
                    intent = new Intent(context, SettingActivity.class);
                    HomeFragment.mSwipeRefreshLayout.setRefreshing(false);
                    startActivity(intent);
                    break;
            }
            return true;
        });

        //On the start of this Acivity Setting Home Fragement As default

        fragmentManager = getSupportFragmentManager();
        if (manager.getFragmentName() == FragmentName.NONE) {
            fragment = HomeFragment.get();
            setVisibilityInToolBar(0);
            tab_home.setBackgroundDrawable(getResources().getDrawable(R.drawable.homebuttonbg));
            tab_home.setImageDrawable(getResources().getDrawable(R.drawable.home_selected));
            mTempImageReference = tab_home;
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.view_fragmentContatiner, fragment).commit();
        }
//        if (shouldAskPermissions())
//            askPermissions();
//        App.get().volleyGet();
        if (!isAllIconDone) {
            subscribeForSyncResults();
            requestSync();
            if (manager.mPreferences.getBoolean("IsTutorialOver", true))
                showInitialLoadingView();
//            isAllIconDone = true;
        }
        //setting Custom Tab Layout clicke function
        tab_home.setOnClickListener(this);
        tab_cast.setOnClickListener(this);
        tab_search.setOnClickListener(this);
        tab_subscribe.setOnClickListener(this);
        tab_favorites.setOnClickListener(this);


        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (fragment != SearchFragment.get()) {
                    try {
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    } catch (Exception e) {

                    }
                }
                Rect r = new Rect();
                view.getWindowVisibleDisplayFrame(r);
                int screenHeight = view.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;
                if (keypadHeight > screenHeight * 0.15) {
                    tabLayout.setVisibility(View.GONE);
                    findViewById(R.id.tabShadow).setVisibility(View.GONE);
                } else {
                    tabLayout.setVisibility(View.VISIBLE);
                    findViewById(R.id.tabShadow).setVisibility(View.VISIBLE);


                }
            }
        });
    }


    /*
     * Public Functions
     */
    //Back Pressed to exit the app
    @Override
    public void onBackPressed() {

        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.exit_dialog_view, viewGroup, false);
        ImageView mOkFinish = dialogView.findViewById(R.id.Okfinish);
        ImageView mNoBtn = dialogView.findViewById(R.id.Nobutton);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        alertDialog.setCancelable(false); //Function Performed When "YES" pressed
        long now = SystemClock.elapsedRealtime();
        if (now - lastClickMillis > THRESHOLD_MILLIS) {
            lastClickMillis = now;

            mOkFinish.setOnClickListener(view -> {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
                finish();
                System.exit(0);
                alertDialog.dismiss();
            });
            //Function Performed When "NO" pressed
            mNoBtn.setOnClickListener(view -> alertDialog.dismiss());
        }

    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void isFragmentLoaded() {
        loadfragment = true;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public void mLoadHomeFragment() {
        fragmentTransaction = null;
        fragment = HomeFragment.get();
        setVisibilityInToolBar(0);

        tabUnselected();
        mTempImageReference = tab_home;
        mInstance.isLaterButtonCLicked = true;
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.view_fragmentContatiner, fragment).commitAllowingStateLoss();
        tab_home.setBackgroundDrawable(getResources().getDrawable(R.drawable.homebuttonbg));
        tab_home.setImageDrawable(getResources().getDrawable(R.drawable.home_selected));
    }

    public void setVisibilityInToolBar(int position) {
        //    @BindView(R.id.relative_Search)
        RelativeLayout svToolbar = findViewById(R.id.relative_Search);
//
//    @BindView(R.id.img_toolbarTile)
        ImageView imgToolbar = findViewById(R.id.img_toolbarTile);
        if (svToolbar == null || imgToolbar == null) return;
        svToolbar.setVisibility(View.GONE);
        imgToolbar.setVisibility(View.GONE);
        switch (position) {
            case 0:
                imgToolbar.setVisibility(View.VISIBLE);
                break;
            case 1:
                svToolbar.setVisibility(View.VISIBLE);
                break;


        }
    }


    public static void requestSync() {
        if (manager.isNeedsUpdateFromCMS()) return;
        manager.setNeedsUpdateFromCMS(true);
        vault.requestSync(SyncConfig.builder().setClient(ContentfulClient.get()).build());
    }

    @Override
    public void downloadCompleted() {
        manager.setDownloading(false);
        if (!manager.isBookDownloadInProgress() && getApplication() != null) {
            HomeFragment.get().updateData();
            manager.mPreferences.edit().putBoolean("IsLoadingCompleteForFTUE", true).apply();
//            hideInitialLoadingView();
//            HomeFragment.get().hideInitialLoadingView();
            manager.setNeedsUpdateFromCMS(false);
        }

        HomeFragment.get().stopRefresh();

    }

    @Override
    public void downloadNetworkError(androidx.appcompat.app.AlertDialog alertDialog) {

    }

    @Override
    protected void onResume() {
        if (isAllIconDone) {
            if (!manager.isBookDownloadInProgress()) {
                HomeFragment.get().forceRefresh();
            }
        }
        loadfragment = true;
        mIsSettingClicked = false;
        tab_subscribe.setOnClickListener(manager.isAppFree() ? null : this);
        tab_subscribe.setAlpha(manager.isAppFree() ? 0.5f : 1.0f);
        if (!manager.mPreferences.getBoolean("IsTutorialOver", true)) {
            try {
                String[] list = getAssets().list("free_book");

                startActivity(new Intent(LibraryViewActivity.this, PageActivity.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (!manager.mPreferences.getBoolean("IsLoadingCompleteForFTUE", false) && alertDialog1 == null) {
                if (!isSyncSuccess) {
//                    HomeFragment.get().twoParameterDialog(R.layout.popup_network_error, R.id.img_popRetry, R.id.img_popCancel, view, -1);
                    twoParameterDialog(R.layout.popup_network_error, R.id.img_popRetry, R.id.img_popCancel);

                } else {
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    showInitialLoadingView();

                }
            }
        }

        super.onResume();

    }

    @Override
    protected void onStart() {

        switch (manager.getFragmentName()) {
            case FAVOURITE:
                fragment = null;
                fragmentTransaction = null;
                fragment = FavoraiteFragment.get();
                setVisibilityInToolBar(0);
                tabUnselected();
                tab_favorites.setBackgroundDrawable(getResources().getDrawable(R.drawable.favoriteiconbg));
                tab_favorites.setImageDrawable(getResources().getDrawable(R.drawable.favorite_selected));
                mTempImageReference = tab_favorites;
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.view_fragmentContatiner, fragment).commitAllowingStateLoss();

                break;
            case SEARCH:
                fragment = null;
                fragmentTransaction = null;
                fragment = SearchFragment.get();
                setVisibilityInToolBar(1);
                tabUnselected();
                tab_search.setBackgroundDrawable(getResources().getDrawable(R.drawable.searchiconbg));
                tab_search.setImageDrawable(getResources().getDrawable(R.drawable.search_selected));

                mTempImageReference = tab_search;
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.view_fragmentContatiner, fragment).commitAllowingStateLoss();
                manager.setFragmentName(FragmentName.NONE);
                break;
            case CAST:

                fragment = null;
                fragmentTransaction = null;
                fragment = CastFragment.get();
                setVisibilityInToolBar(0);
                tabUnselected();
                tab_cast.setBackgroundDrawable(getResources().getDrawable(R.drawable.myaccounticonbg));
                tab_cast.setImageDrawable(getResources().getDrawable(R.drawable.casts_selected));
                mTempImageReference = tab_cast;
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.view_fragmentContatiner, fragment).commitAllowingStateLoss();
                manager.setFragmentName(FragmentName.NONE);
                break;
            case HOME:
                fragment = null;
                fragmentTransaction = null;
                fragment = HomeFragment.get();
                setVisibilityInToolBar(0);
                tabUnselected();
                tab_home.setBackgroundDrawable(getResources().getDrawable(R.drawable.homebuttonbg));
                tab_home.setImageDrawable(getResources().getDrawable(R.drawable.home_selected));
                mTempImageReference = tab_home;
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.view_fragmentContatiner, fragment).commitAllowingStateLoss();
                manager.setFragmentName(FragmentName.NONE);
                break;
            case SUBSCRIBE:
                fragment = null;
                fragmentTransaction = null;
                fragment = SubscribeFragment.get();
                setVisibilityInToolBar(0);
                tabUnselected();
                tab_subscribe.setBackgroundDrawable(getResources().getDrawable(R.drawable.subscribeiconbg));
                tab_subscribe.setImageDrawable(getResources().getDrawable(R.drawable.subscribe_selected));
                mTempImageReference = tab_subscribe;
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.view_fragmentContatiner, fragment).commitAllowingStateLoss();
                break;

        }
        super.onStart();

    }


    /*
     * Private Functions
     */
    private boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(23)
    private void askPermissions() {
        String[] permissions = {"android.permission.READ_EXTERNAL_STORAGE" +
                "" +
                "E", "android.permission.WRITE_EXTERNAL_STORAGE"};
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }

    @SuppressLint("CheckResult")
    public void subscribeForSyncResults() {
        time = SystemClock.elapsedRealtime();
        vault.releaseAll();
        Log.i(TAG, "subscribeForSyncResults: ");
        Vault.observeSyncResults().observeOn(AndroidSchedulers.mainThread()).subscribe(this::onSyncResult);
    }

    private void onSyncResult(com.contentful.vault.SyncResult syncResult) {
        isSyncSuccess = true;
        if (syncResult.error() != null) {

            isSyncSuccess = false;
            vault.releaseAll();
            DownloadContentManager.getInstance().terminateDownload();
            hideInitialLoadingView();

            return;
        }
//        if (isNetworkPopupAppear)
//            HomeFragment.get().twoParameterDialog(R.layout.popup_network_error, R.id.img_popRetry, R.id.img_popCancel, view, -1);

        isSyncSuccess = true;
        if (isSyncProgress) return;
        Log.i(TAG, "onSyncResult: ");
        reloadVersions();
        isSyncProgress = true;

    }

    @SuppressLint("CheckResult")
    private void reloadVersions() {
        Log.i(TAG, "reloadVersions: Fetching Country Code");
        time = SystemClock.elapsedRealtime();
        vault.observe(MakeAppFree.class).order(Constant.APP_FREE + " DESC").all().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).toList().subscribe(this::readAllCountryCode);

    }

    @SuppressLint("CheckResult")
    private void readAllCountryCode(List<MakeAppFree> countryCodesList) {
        Log.i(TAG, "reloadVersions: Fetching Category");

        manager.setAppFree(countryCodesList);


        vault.observe(Category.class).order(Constant.CATEGORY_ID).all().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).toList().subscribe(this::readAllCategory);
    }

    @SuppressLint("CheckResult")
    private void readAllCategory(List<Category> categoryList) {
        Log.i(TAG, "readAllCategory: " + manager.isAppFree());
        if (categoryList.size() <= 0) {
            //Network Error popup
            vault.releaseAll();
//            HomeFragment.get().twoParameterDialog(R.layout.popup_network_error, R.id.img_popRetry, R.id.img_popCancel,HomeFragment.get().view,-1);
            Vault.observeSyncResults().observeOn(AndroidSchedulers.mainThread()).onTerminateDetach();
            DownloadContentManager.getInstance().terminateDownload();
            hideInitialLoadingView();
            HomeFragment.get().hideInitialLoadingView();
        } else {
            manager.setOriginalCategoryList(categoryList);
            time = SystemClock.elapsedRealtime();
            vault.observe(Cast.class).order(Constant.CAST_ID).all().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).toList().subscribe(this::readAllCast);
        }
    }

    @SuppressLint("CheckResult")
    private void readAllCast(List<Cast> castList) {
        if (castList.size() <= 0) {
            //Network Error popup
            vault.releaseAll();
//            HomeFragment.get().twoParameterDialog(R.layout.popup_network_error, R.id.img_popRetry, R.id.img_popCancel,HomeFragment.get().view,-1);
            Vault.observeSyncResults().observeOn(AndroidSchedulers.mainThread()).onTerminateDetach();
            DownloadContentManager.getInstance().terminateDownload();
            hideInitialLoadingView();
            HomeFragment.get().hideInitialLoadingView();
        } else {
            manager.setOriginalCastList(castList);
            time = SystemClock.elapsedRealtime();
            vault.observe(Book.class).order(Constant.BOOK_ID + " DESC").all().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).toList().subscribe(this::readAllBooks);
        }
    }

    private void readAllBooks(List<Book> bookList) {
        if (bookList.size() <= 0) {
            vault.releaseAll();
//            HomeFragment.get().twoParameterDialog(R.layout.popup_network_error, R.id.img_popRetry, R.id.img_popCancel,HomeFragment.get().view,-1);
            Vault.observeSyncResults().observeOn(AndroidSchedulers.mainThread()).onTerminateDetach();
            DownloadContentManager.getInstance().terminateDownload();
            hideInitialLoadingView();
            HomeFragment.get().hideInitialLoadingView();
        } else {
            if (manager.getSelectedCast() == -1) {
                manager.setSelectedCast(manager.getOrignalCastList().get(0).getCastId(), 0);
                manager.setSelectedCategoy(manager.getOrignalCategoryList().get(0).getCategoryId(), 0);
            }
            manager.setOrignalBookList(bookList);
            manager.checkCurrentBookData();
            DownloadContentManager.getInstance().setListnerForIcon(this);
            DownloadContentManager.getInstance().downloadDefaultIcons(this, HomeFragment.get().getView());
            // Swipe action and make listener null

        }
        isSyncProgress = false;
    }

    private void showInitialLoadingView() {
        mInitialDialog = new ProgressDialog(LibraryViewActivity.this);
        mInitialDialog.setMessage(getResources().getString(R.string.wait));
        mInitialDialog.setCancelable(false);
        mInitialDialog.show();
        timeLeftInMille = 240000;
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.popup_please_wait, viewGroup, false);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        popupImage = dialogView.findViewById(R.id.img_popup_wait);
        bar1 = dialogView.findViewById(R.id.initialLoading);
        bar2 = dialogView.findViewById(R.id.initialLoading2);
        bar1.setVisibility(View.GONE);
        bar2.setVisibility(View.GONE);
//        popupImage.setBackground(context.getResources().getDrawable(mWaitingImage[mWaitingImageIndex++]));

        builder.setView(dialogView);
        alertDialog1 = builder.create();
        Objects.requireNonNull(alertDialog1.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        alertDialog1.setCancelable(false);
        final int[] times = {0};
        try {
            this.countDownTimer = new CountDownTimer(timeLeftInMille, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeLeftInMille = millisUntilFinished;
                    /**
                     * Check the  internet connection while Syncing to contentFul
                     */
                    ConnectivityManager connectivityManager
                            = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo == null) {
                        if (mInitialDialog != null) {
                            mInitialDialog.dismiss();
                            mInitialDialog = null;
                        }
                        if (!manager.isNetworkPopupVisible()) {
                            try {
                                if (manager.mPreferences.getBoolean("IsTutorialOver", true)) {
                                    twoParameterDialog(R.layout.popup_network_error, R.id.img_popRetry, R.id.img_popCancel);
                                }
                                DownloadContentManager.getInstance().terminateDownload();
                                hideInitialLoadingView();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        return;
                    }
                    if (timeLeftInMille < 220000 && timeLeftInMille > 200000 && times[0] == 0) {
                        bar1.setVisibility(View.GONE);
                        bar2.setVisibility(View.VISIBLE);
                        alertDialog1.show();

                        if (mInitialDialog.isShowing())
                            mInitialDialog.dismiss();
                        if (mWaitingImageIndex >= 2) mWaitingImageIndex = 0;
                        popupImage.setBackground(context.getResources().getDrawable(mWaitingImage[mWaitingImageIndex++]));
                        times[0] = 1;
                    } else if (timeLeftInMille < 200000 && timeLeftInMille > 180000 && times[0] == 1) {
                        bar1.setVisibility(View.GONE);
                        bar2.setVisibility(View.VISIBLE);
                        popupImage.setBackground(context.getResources().getDrawable(mWaitingImage[mWaitingImageIndex++]));
                        times[0] = 2;
                    } else if (timeLeftInMille < 180000 && times[0] == 2) {
                        bar1.setVisibility(View.VISIBLE);
                        bar2.setVisibility(View.GONE);
                        popupImage.setBackground(context.getResources().getDrawable(mWaitingImage[mWaitingImageIndex++]));
                        times[0] = 3;

                    }
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }

                @Override
                public void onFinish() {
                    if (alertDialog1 != null) {
                        Vault.observeSyncResults().observeOn(AndroidSchedulers.mainThread()).onTerminateDetach();
                        DownloadContentManager.getInstance().terminateDownload();
                        hideInitialLoadingView();
                        HomeFragment.get().hideInitialLoadingView();
                        twoParameterDialog(R.layout.popup_network_error, R.id.img_popRetry, R.id.img_popCancel);
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideInitialLoadingView() {
        try {
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
                alertDialog1.dismiss();
                alertDialog1 = null;

            }
            if (mInitialDialog != null) {
                mInitialDialog.dismiss();
                mInitialDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        mInitialDialog = null;
    }

    /**
     * Function below this minicing the Functionality of Tablayout
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        toolbar.setVisibility(View.VISIBLE);
        if (v.getId() != mTempImageReference.getId() && loadfragment) {
            if (!manager.isDownloading())
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            long now = SystemClock.elapsedRealtime();
            if (now - manager.getLastClickMillis() < Constant.THRESHOLD_MILLIS) {
                manager.setLastClickMillis(now);
                return;
            }

            manager.setBookSearch("");
            manager.setNeedSubscription(false);
            manager.setFragmentName(FragmentName.NONE);
//            App.get().volleyGet();
            switch (v.getId()) {
                /**
                 * Click of Custom tabs
                 */
                case R.id.tab_home:
                    setVisibilityInToolBar(0);
                    fragment = HomeFragment.get();
                    tab_home.setBackgroundDrawable(getResources().getDrawable(R.drawable.homebuttonbg));
                    tab_home.setImageDrawable(getResources().getDrawable(R.drawable.home_selected));
                    tabUnselected();
                    img_shadowTop.setBackgroundDrawable(getResources().getDrawable(R.drawable.yellow_shadow1));
                    img_shadowBottom.setBackgroundDrawable(getResources().getDrawable(R.drawable.yellow_shadow2));
                    mTempImageReference = tab_home;
                    break;
                case R.id.tab_cast:
                    setVisibilityInToolBar(0);
                    fragment = CastFragment.get();

                    tab_cast.setBackgroundDrawable(getResources().getDrawable(R.drawable.myaccounticonbg));
                    tab_cast.setImageDrawable(getResources().getDrawable(R.drawable.casts_selected));
                    tabUnselected();
                    img_shadowTop.setBackgroundDrawable(getResources().getDrawable(R.drawable.green_shadow1));
                    img_shadowBottom.setBackgroundDrawable(getResources().getDrawable(R.drawable.green_shadow2));
                    mTempImageReference = tab_cast;
                    break;
                case R.id.tab_search:
                    setVisibilityInToolBar(1);
                    fragment = SearchFragment.get();
                    tab_search.setBackgroundDrawable(getResources().getDrawable(R.drawable.searchiconbg));
                    tab_search.setImageDrawable(getResources().getDrawable(R.drawable.search_selected));

                    tabUnselected();
                    img_shadowTop.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_shadow1));
                    img_shadowBottom.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_shadow2));
                    mTempImageReference = tab_search;
                    break;
                case R.id.tab_subscribe:
                    setVisibilityInToolBar(0);
                    fragment = SubscribeFragment.get();
                    tab_subscribe.setBackgroundDrawable(getResources().getDrawable(R.drawable.subscribeiconbg));
                    tab_subscribe.setImageDrawable(getResources().getDrawable(R.drawable.subscribe_selected));
                    tabUnselected();
                    img_shadowTop.setBackground(getResources().getDrawable(R.drawable.yellow_shadow1));
                    img_shadowBottom.setBackground(getResources().getDrawable(R.drawable.yellow_shadow2));
                    mTempImageReference = tab_subscribe;

                    break;
                case R.id.tab_favorites:
                    setVisibilityInToolBar(0);
                    fragment = FavoraiteFragment.get();
                    tab_favorites.setBackgroundDrawable(getResources().getDrawable(R.drawable.favoriteiconbg));
                    tab_favorites.setImageDrawable(getResources().getDrawable(R.drawable.favorite_selected));
                    tabUnselected();
                    img_shadowTop.setBackgroundDrawable(getResources().getDrawable(R.drawable.fav_shadow1));
                    img_shadowBottom.setBackgroundDrawable(getResources().getDrawable(R.drawable.fav_shadow2));
                    mTempImageReference = tab_favorites;
                    break;
                //*************************************************************************************
            }
            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.view_fragmentContatiner, fragment).commit();
            loadfragment = false;
        }
    }

    public void tabUnselected() {
        switch (mTempImageReference.getId()) {
            case R.id.tab_home:
                tab_home.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_unselected_bg));
                tab_home.setImageDrawable(getResources().getDrawable(R.drawable.home_unselected));

                break;
            case R.id.tab_cast:
                tab_cast.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_unselected_bg));
                tab_cast.setImageDrawable(getResources().getDrawable(R.drawable.casts_unselected));

                break;
            case R.id.tab_search:
                tab_search.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_unselected_bg));
                tab_search.setImageDrawable(getResources().getDrawable(R.drawable.search_unselected));

                break;
            case R.id.tab_subscribe:
                tab_subscribe.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_unselected_bg));
                tab_subscribe.setImageDrawable(getResources().getDrawable(R.drawable.subscribe_unselected));

                break;
            case R.id.tab_favorites:
                tab_favorites.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_unselected_bg));
                tab_favorites.setImageDrawable(getResources().getDrawable(R.drawable.favorite_unselected));

                break;
        }
    }

    public void forceUpdateContainer() {
        downloadCompleted();
    }

    /**
     * Error message popup when the user freshly install the app and just after the they loose's the
     * internet connection
     *
     * @param dialogContainer layoutID of the error dialog
     * @param yesView         LayoutID of the retry Button
     * @param noView          LayoutID of the cancel Button
     */
    public void twoParameterDialog(int dialogContainer, int yesView, int noView) {

        if (!manager.mPreferences.getBoolean("IsTutorialOver", true))
            return;
        DownloadContentManager.getInstance().terminateDownload();
        hideInitialLoadingView();
        manager.setNetworkPopupVisibility(true);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(context).inflate(dialogContainer, viewGroup, false);
        ImageView mOkFinish = dialogView.findViewById(yesView);
        ImageView mNoBtn = dialogView.findViewById(noView);
//        AlertDialog alertDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        alertDialog.setCancelable(false);
        //Function Performed When "YES" pressed
        boolean isclicked = false;
        mOkFinish.setOnClickListener(view -> {
            // Todo: Making the needUpdateFromCMS to 'false' as we are checking trying to download the content's inside the app
            long now = SystemClock.elapsedRealtime();
            if (now - manager.getLastClickMillis() < Constant.THRESHOLD_MILLIS) {
                manager.setLastClickMillis(now);

                return;
            }

            manager.setNetworkPopupVisibility(false);
            this.mWaitingImageIndex = 0;
            manager.setNeedsUpdateFromCMS(false);
            subscribeForSyncResults();
            requestSync();
            showInitialLoadingView();
//            isAllIconDone = true;
            alertDialog.dismiss();
        });
        //Function Performed When "NO" pressed
        mNoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = SystemClock.elapsedRealtime();
                if (now - manager.getLastClickMillis() < Constant.THRESHOLD_MILLIS) {
                    manager.setLastClickMillis(now);

                    return;
                }

                manager.setNetworkPopupVisibility(false);
                alertDialog.dismiss();
            }
        });
        isNetworkPopupAppear = true;
    }

    @Override
    protected void onStop() {
        if ((mInitialDialog != null) && mInitialDialog.isShowing())
            mInitialDialog.dismiss();
        super.onStop();
    }

    /**
     * This function is call from the home fragment when the is no Data downloaded
     */

    public void callNetworkPopup() {

        manager.setNeedsUpdateFromCMS(false);
        subscribeForSyncResults();
        requestSync();
//        isAllIconDone = true;
    }

    public void trueAllIconDone() {
        isAllIconDone = true;
    }
}
