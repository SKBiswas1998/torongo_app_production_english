package com.monnfamily.enlibraryapp.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.monnfamily.enlibraryapp.Activity.LibraryViewActivity;
import com.monnfamily.enlibraryapp.Activity.OpenBookActivity;
import com.monnfamily.enlibraryapp.Adapter.BookAdapter;
import com.monnfamily.enlibraryapp.Adapter.CategoryAdapter;
import com.monnfamily.enlibraryapp.Constants.Constant;
import com.monnfamily.enlibraryapp.Contentful.Book;
import com.monnfamily.enlibraryapp.R;
import com.monnfamily.enlibraryapp.Utils.App;
import com.monnfamily.enlibraryapp.Utils.AppManager;
import com.monnfamily.enlibraryapp.Utils.DownloadContentManager;
import com.monnfamily.enlibraryapp.Utils.FragmentName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.monnfamily.enlibraryapp.Activity.LibraryViewActivity.isNetworkPopupAppear;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements DownloadContentManager.DownloadCompletedListner {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "HomeFragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private BookAdapter mBookAdapter;
    private CategoryAdapter mCategoryAdapter;
    private OnFragmentInteractionListener mListener;
    public static HomeFragment mInstance;
    public static SwipeRefreshLayout mSwipeRefreshLayout;
    public View view, mCardView;
    List<Book> mBookList;
    static boolean isDownloading = false;

    static int bookID = 0;
    ProgressBar mProgressBar;
    RecyclerView recycler_books;
    boolean isErrorPopupVisible = false;
    private static boolean isRefresh = false;

    private int mWaitingImageIndex = 0;
    ConstraintLayout popupImage;
    ProgressBar bar1, bar2;
    private CountDownTimer countDownTimer;
    private long timeLeftInMille = 0;
    private AlertDialog alertDialog1;
    private int[] mWaitingImage = {
            R.drawable.pleasewait,
            R.drawable.bitmore,
            R.drawable.almost
    };
    private ProgressDialog mInitialDialog;
    AppManager manager = AppManager.getInstance();

    public HomeFragment() {
        // Required empty private constructor
        if (mInstance != null)
            mInstance = null;
        mInstance = this;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment get() {
        if (mInstance == null) {
            new HomeFragment();
        }
        return mInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);
        setupRecyclercategory(view);
        setupRecyclerBooks(view);
        //Add Functionality using swip here
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_BookHomeFragment);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefresh = true;
                long now = SystemClock.elapsedRealtime();
                if (now - manager.getLastClickMillis() > Constant.REFRESH_THRESHOLD_MILLIS) {
                    manager.setLastClickMillis(now);
                     if (isNetworkConnected() == null) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        isRefresh = false;
                        if (!isErrorPopupVisible)
                            twoParameterDialog(R.layout.popup_network_error, R.id.img_popRetry, R.id.img_popCancel, view, -1);
                        return;
                    }
                    if (getActivity() != null)
                        Objects.requireNonNull(getActivity()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    manager.setNeedsUpdateFromCMS(false);
                    manager.setDownloading(true);
                    LibraryViewActivity.get().subscribeForSyncResults();
                    LibraryViewActivity.requestSync();
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                    isRefresh = false;
                }

            }
        });
        /**
         * This listener freeze the app from use when the user try's to refersh or if try's to have a fake refersh
         */
        mSwipeRefreshLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != 2) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (getActivity() != null)
                                Objects.requireNonNull(HomeFragment.get().getActivity()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        }
                    }, Constant.THRESHOLD_MILLIS);

                }
                return false;
            }
        });
        return view;
    }

    private void setupRecyclercategory(View view) {
        mCategoryAdapter = new CategoryAdapter(getActivity(), (v, categoryID) -> {
            long now = SystemClock.elapsedRealtime();
            if (now - manager.getLastClickMillis() < Constant.THRESHOLD_MILLIS) {
                manager.setLastClickMillis(now);
                return;
            }
            mBookList.clear();
            mBookList.addAll(manager.getCategoryDefaultFilterBook());
            mBookAdapter.updateData(mBookList);
            recycler_books.smoothScrollToPosition(0);
        });

        RecyclerView recycler_categoty = view.findViewById(R.id.recyc_Category);
        recycler_categoty.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recycler_categoty.smoothScrollToPosition(manager.getmPositionIndexCategory());
        recycler_categoty.setAdapter(mCategoryAdapter);
    }

    private void setupRecyclerBooks(View view) {
        manager.setNetworkPopupForZeroBook((manager.getOrignalBookList().size() < 0));
        mBookList = new ArrayList<>();
        mListener.isFragmentLoaded();
        mBookAdapter = new BookAdapter(getActivity(), new BookAdapter.ClickListener() {
            @Override
            public void onItemClicked(View v, int bookId, View mBookItemView) {
                bookID = bookId;
                long now = SystemClock.elapsedRealtime();
                if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {
                    manager.setLastClickMillis(now);
                    if ((!manager.isBookDownloaded(bookID)) || !manager.checkForBookVersion(bookID)) {
                        if (mSwipeRefreshLayout.isRefreshing()) return;

                        if (manager.isForUpdate(bookID)) {
                            isDownloading = true;
                            mProgressBar = mBookItemView.findViewById(R.id.progress_DownloadBook);
                            mProgressBar.setVisibility(View.VISIBLE);
                            mProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.fake_progress_bar));
                            mProgressBar.setProgress(5);
                            DownloadContentManager.getInstance().setListnerForBook(HomeFragment.get());
                            DownloadContentManager.getInstance().downloadBookData(bookID, HomeFragment.get().getActivity(), mProgressBar, view);
                            mCardView = mBookItemView;
                            return;
                        }
                        twoParameterDialog(R.layout.popup_newbook_download, R.id.img_YesDonloadNewBook, R.id.img_NoDonloadNewBook, mBookItemView, bookID);
                        mCardView = mBookItemView;

                    } else {
                        manager.setFragmentName(FragmentName.HOME);
//                        App.get().volleyGet();
                        Intent intent = new Intent(getContext(), OpenBookActivity.class);
                        intent.putExtra("bookId", bookID);
                        getActivity().startActivity(intent);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                            getActivity().overridePendingTransition(R.anim.side_in_right, R.anim.side_out_left);
                        }
                    }
                }

            }

            @Override
            public void onItemFavFrgment(View v, int position, int mBookId, ImageView mFavIcon) {
                //This will set image when the click the favoraite icon
                long now = SystemClock.elapsedRealtime();
                if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {

                    manager.setLastClickMillis(now);
                    if (manager.getFavoriteBook().contains(mBookId)) {
                        removeFromFav(R.layout.popup_remove_from_fav, R.id.ok_button, R.id.no_button, mBookId, mFavIcon, v); //Remove From fav
                    } else {
                        manager.setFavoriteBook(mBookId);
                        mFavIcon.setImageResource(R.drawable.homefavoriteen);
                    }
                }


            }

        });
        if (manager.getOrignalCategoryList() != null) {
            updateData();
        }
        recycler_books = view.findViewById(R.id.recyc_BookList);
        recycler_books.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recycler_books.scrollToPosition(1);
        recycler_books.setNestedScrollingEnabled(false);
        recycler_books.setAdapter(mBookAdapter);

    }


    @Override
    public void onResume() {
        manager.setFragmentName(FragmentName.HOME);
        if (LibraryViewActivity.get().isLaterButtonCLicked){
            forceRefresh();
            LibraryViewActivity.get().isLaterButtonCLicked = false;
        }
        super.onResume();
    }


    public void updateData() {
        mCategoryAdapter.itemNotifiedChanged(manager.getOrignalCategoryList());

        mBookAdapter.updateData(manager.getCategoryDefaultFilterBook());
        try {
            if (!manager.isNetworkPopupForZeroBook() && isNetworkPopupAppear) {
                if (!manager.isNetworkPopupVisible())
                    twoParameterDialog(R.layout.popup_network_error, R.id.img_popRetry, R.id.img_popCancel, view, -1);
                manager.setNetworkPopupForZeroBook(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        if (manager.isBookDownloadInProgress())
//            stopRefresh();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;


    }

    @Override
    public void downloadCompleted() {
        //Download completed popup....
        if (isDownloading && mCardView != null) {
            isDownloading = false;
            ImageView v = null;
            if (getActivity() == null)
                return;
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            TextView textView = mCardView.findViewById(R.id.txtStatus);
            String text = textView.getText().toString().toLowerCase().trim();
            String tempText = getResources().getString(R.string.update);
            textView.setText(R.string.open);
            textView.setTextColor(getResources().getColor(R.color.white));
            textView.setBackgroundDrawable(getResources().getDrawable(R.drawable.bct_open_bg));
            mCardView.findViewById(R.id.progress_DownloadBook).setVisibility(View.GONE);
            if (!text.trim().equals(tempText.toLowerCase()))
                oneParameterDialog(R.layout.popup_bookdownload_complete, R.id.relative_OkBookDownCompButton, 3, 0, v, view);//here tBook and Image Book are null or fault
            manager.setIsBookDownloadInProgress(false);
            if (manager.isNeedsUpdateFromCMS())
                LibraryViewActivity.get().forceUpdateContainer();
            mCardView = null;
            DownloadContentManager.getInstance().terminateDownload();
        }
    }

    @Override
    public void downloadNetworkError(AlertDialog alertDialog) {
        DownloadContentManager.getInstance().terminateDownload();
        DownloadContentManager.getInstance().setListnerForBook(HomeFragment.get());
        DownloadContentManager.getInstance().downloadBookData(bookID, HomeFragment.get().getActivity(), mProgressBar, this.view);
    }


    public void stopRefresh() {
        updateData();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);

                if (isRefresh && mInstance.getActivity() != null) {

                    Objects.requireNonNull(mInstance.getActivity()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    isRefresh = false;
                }
            }
        }, 1000);
        LibraryViewActivity.get().tab_subscribe.setOnClickListener(manager.isAppFree() ? null : LibraryViewActivity.get());
        LibraryViewActivity.get().tab_subscribe.setAlpha(manager.isAppFree() ? 0.5f : 1.0f);
    }

    public void forceRefresh() {
        if (mSwipeRefreshLayout.isRefreshing()) return;
        mSwipeRefreshLayout.post(() -> {
            mSwipeRefreshLayout.setRefreshing(true);
            isRefresh = true;
            LibraryViewActivity.get().subscribeForSyncResults();
            LibraryViewActivity.get().requestSync();
        });
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void isFragmentLoaded();

    }

    public void oneParameterDialog(int dialogContainer, int yesView, int isAdd, Integer tBook, ImageView mImgFavIcon, View v) {
        ViewGroup viewGroup = v.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(getActivity()).inflate(dialogContainer, viewGroup, false);
        RelativeLayout mOkFinish = dialogView.findViewById(yesView);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        alertDialog.setCancelable(false);
        //Function Performed When "YES" pressed
        try {
            mOkFinish.setOnClickListener(view -> {
                long now = SystemClock.elapsedRealtime();
                if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {
                    manager.setLastClickMillis(now);
                    alertDialog.dismiss();
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void removeFromFav(int dialogContainer, int yesView, int noView, Integer tBook, ImageView mImgFavIcon, View v) {
        ViewGroup viewGroup = v.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(getActivity()).inflate(dialogContainer, viewGroup, false);
        ImageView mOkFinish = dialogView.findViewById(yesView);
        ImageView mNoFinish = dialogView.findViewById(noView);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        alertDialog.setCancelable(false);
        //Function Performed When "YES" pressed
        try {
            mOkFinish.setOnClickListener(view -> {
                //if want to remove book from favoraite
                long now = SystemClock.elapsedRealtime();
                if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {
                    manager.setLastClickMillis(now);
                    manager.removeFavoriteBook(tBook);
                    mImgFavIcon.setImageResource(R.drawable.favorite_unselected);
                    alertDialog.dismiss();
                }

            });
            mNoFinish.setOnClickListener(view -> {
                //if want to remove book from favoraite
                long now = SystemClock.elapsedRealtime();
                if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {
                    manager.setLastClickMillis(now);
                    alertDialog.dismiss();
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void twoParameterDialog(int dialogContainer, int yesView, int noView, View v, int bookID) {
        manager.setNetworkPopupVisibility(true);
        ViewGroup viewGroup = v.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(getActivity()).inflate(dialogContainer, viewGroup, false);
        ImageView mOkFinish = dialogView.findViewById(yesView);
        ImageView mNoBtn = dialogView.findViewById(noView);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        alertDialog.show();
        alertDialog.setCancelable(false);

        //Function Performed When "YES" pressed
        try {
            mOkFinish.setOnClickListener(view -> {
                // Showing Progress When Book is Downloaded
                long now = SystemClock.elapsedRealtime();
                if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {

                    manager.setLastClickMillis(now);
                    manager.setNetworkPopupVisibility(false);

                    if (bookID == -1) {

                        if (alertDialog1 != null)
                            alertDialog1.dismiss();
                        manager.setNetworkPopupForZeroBook(true);
                        if (manager.getFragmentName() == FragmentName.HOME) {
                            LibraryViewActivity.get().callNetworkPopup();
                            showInitialLoadingView();
                        }
                    } else {
                        isDownloading = true;
                        mProgressBar = v.findViewById(R.id.progress_DownloadBook);
                        mProgressBar.setVisibility(View.VISIBLE);
                        mProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.fake_progress_bar));
                        mProgressBar.setProgress(5);
                        DownloadContentManager.getInstance().setListnerForBook(HomeFragment.get());
                        DownloadContentManager.getInstance().downloadBookData(bookID, HomeFragment.get().getActivity(), mProgressBar, this.view);
                    }
                    alertDialog.dismiss();
                }
            });
            //Function Performed When "NO" pressed
            mNoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long now = SystemClock.elapsedRealtime();
                    if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {
                        if (alertDialog1 != null)
                            alertDialog1.dismiss();
                        manager.setLastClickMillis(now);
                        alertDialog.dismiss();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showInitialLoadingView() {
        mInitialDialog = new ProgressDialog(getActivity());
        mInitialDialog.setMessage(getResources().getString(R.string.wait));
        mInitialDialog.setCancelable(false);
        mInitialDialog.show();

        timeLeftInMille = 240000;
        ViewGroup viewGroup = view.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.popup_please_wait, viewGroup, false);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
        popupImage = dialogView.findViewById(R.id.img_popup_wait);
        bar1 = dialogView.findViewById(R.id.initialLoading);
        bar2 = dialogView.findViewById(R.id.initialLoading2);
        bar1.setVisibility(View.GONE);
        bar2.setVisibility(View.GONE);
        builder.setView(dialogView);
        alertDialog1 = builder.create();
        Objects.requireNonNull(alertDialog1.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        alertDialog1.setCancelable(false);
        final int[] times = {0};
        try {
            countDownTimer = new CountDownTimer(timeLeftInMille, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeLeftInMille = millisUntilFinished;
                    /**
                     * Check the  internet connection while Syncing to contentFul
                     */
                    if (isNetworkConnected() == null || !LibraryViewActivity.isSyncSuccess) {
                        if (mInitialDialog != null) {
                            mInitialDialog.dismiss();
                            mInitialDialog = null;
                        }
                        if (!manager.isNetworkPopupVisible()) {
                            twoParameterDialog(R.layout.popup_network_error, R.id.img_popRetry, R.id.img_popCancel, view, -1);
                            DownloadContentManager.getInstance().terminateDownload();
                            hideInitialLoadingView();
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
                        popupImage.setBackground(getActivity().getResources().getDrawable(mWaitingImage[mWaitingImageIndex++]));
                        times[0] = 1;
                    } else if (timeLeftInMille < 200000 && timeLeftInMille > 180000 && times[0] == 1) {
                        bar1.setVisibility(View.GONE);
                        bar2.setVisibility(View.VISIBLE);
                        popupImage.setBackground(getActivity().getResources().getDrawable(mWaitingImage[mWaitingImageIndex++]));
                        times[0] = 2;
                    } else if (timeLeftInMille < 180000 && times[0] == 2) {
                        bar1.setVisibility(View.VISIBLE);
                        bar2.setVisibility(View.GONE);
                        popupImage.setBackground(getActivity().getResources().getDrawable(mWaitingImage[mWaitingImageIndex++]));
                        times[0] = 3;
                    }
                }

                @Override
                public void onFinish() {
                    if (alertDialog1 != null) {
                        DownloadContentManager.getInstance().terminateDownload();
                        hideInitialLoadingView();
                        LibraryViewActivity.get().hideInitialLoadingView();
                        twoParameterDialog(R.layout.popup_network_error, R.id.img_popRetry, R.id.img_popCancel, view, -1);

                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideInitialLoadingView() {
        try {
            manager.setLastClickMillis(SystemClock.elapsedRealtime());
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
    }

    private NetworkInfo isNetworkConnected() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }
}
