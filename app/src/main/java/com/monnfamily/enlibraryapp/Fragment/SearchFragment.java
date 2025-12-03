package com.monnfamily.enlibraryapp.Fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.monnfamily.enlibraryapp.Activity.LibraryViewActivity;
import com.monnfamily.enlibraryapp.Activity.OpenBookActivity;
import com.monnfamily.enlibraryapp.Adapter.BookAdapter;
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

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment implements DownloadContentManager.DownloadCompletedListner {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    @BindView(R.id.recyc_search)
    RecyclerView recyc_search;
    RecyclerView recycler_books;
    Toolbar toolbar;
    List<Book> mBookList;
    BookAdapter mBookAdapter;
    boolean isSearching = false;
    public static ProgressDialog mInitialDialog;
    List<Book> mTempBookList;
    private OnFragmentInteractionListener mListener;
    static SearchFragment mInstance;
    View view, mCardView;
    static boolean isDownloading = false;
    EditText etSearch;
    static int bookID = 0;
    ProgressBar mProgressBar;
    TextView txtEmptyList;

    AppManager manager = AppManager.getInstance();

    // variable for handling multiple clicks

//    private long lastClickMillis;

    public SearchFragment() {
        // Required empty private constructor
        if (mInstance != null)
            mInstance = null;
        mInstance = this;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment get() {
        if (mInstance == null) {
            new SearchFragment();
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
        view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(getActivity());
        toolbar = getActivity().findViewById(R.id.toolBar_activity);
        etSearch = toolbar.findViewById(R.id.et_toolbarTile);
        txtEmptyList = view.findViewById(R.id.txtEmptyList);
        setupRecycler(view);

        ImageView img_textEmpty = toolbar.findViewById(R.id.img_textEmpty);

        img_textEmpty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearch.setText("");
                mBookList.clear();
                mBookList.addAll(manager.getStringFilterBook(etSearch.getText().toString().trim()));
                if (mBookList.size() == 0) {
                    txtEmptyList.setVisibility(View.VISIBLE);
                } else {
                    txtEmptyList.setVisibility(View.GONE);
                }
                mBookAdapter.updateData(mBookList);
                img_textEmpty.setVisibility(View.GONE);
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isDownloading && isNetworkConnected() != null) return;
                img_textEmpty.setVisibility(View.VISIBLE);
                if (s.toString().trim().equals(""))
                    img_textEmpty.setVisibility(View.GONE);
                mBookList.clear();
                mBookList.addAll(manager.getStringFilterBook(s.toString().trim()));
                if (mBookList.size() == 0) {
                    txtEmptyList.setVisibility(View.VISIBLE);
                } else {
                    txtEmptyList.setVisibility(View.GONE);

                }

                mBookAdapter.updateData(mBookList);

            }
        });
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (mBookList.size() == 0)
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        oneParameterDialog(R.layout.popup_noresult_found, R.id.img_okButton, 3, 0, null, view);//here tBook and Image Book are null or fault
                        break;
                }
            return false;
        });
        return view;
    }


    private void setupRecycler(View view) {
        mBookList = new ArrayList<>();
        mBookList.addAll(manager.getStringFilterBook(""));
        etSearch.setText("");
        if (mBookList.size() == 0) {
            txtEmptyList.setVisibility(View.VISIBLE);
        } else {
            txtEmptyList.setVisibility(View.GONE);

        }
        mListener.isFragmentLoaded();
        mBookAdapter = new BookAdapter(getActivity(), new BookAdapter.ClickListener() {
            @Override
            public void onItemClicked(View v, int bookId, View mBookItemView) {
                bookID = bookId;
                //Hiding Keyboard when user verify to download
                hideKeyboard();

                long now = SystemClock.elapsedRealtime();
                if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {
                    manager.setLastClickMillis(now);
                    if (!manager.isBookDownloaded(bookID) || !manager.checkForBookVersion(bookID)) {
                        if (manager.isForUpdate(bookID)) {
                            isDownloading = true;
                            mProgressBar = mBookItemView.findViewById(R.id.progress_DownloadBook);
                            mProgressBar.setVisibility(View.VISIBLE);
                            mProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.fake_progress_bar));
                            mProgressBar.setProgress(5);
                            DownloadContentManager.getInstance().setListnerForBook(SearchFragment.get());
                            DownloadContentManager.getInstance().downloadBookData(bookID, SearchFragment.get().getActivity(), mProgressBar, view);
                            mCardView = mBookItemView;
                            return;
                        }

                        twoParameterDialog(R.layout.popup_newbook_download, R.id.img_YesDonloadNewBook, R.id.img_NoDonloadNewBook, mBookItemView, bookID);
                        mCardView = mBookItemView;

                    } else {
                        manager.setBookSearch(etSearch.getText().toString().trim());
                        manager.setFragmentName(FragmentName.SEARCH);
//                        App.get().volleyGet();
                        Intent intent = new Intent(getContext(), OpenBookActivity.class);
                        intent.putExtra("bookId", bookID);
                        getActivity().startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.side_in_right, R.anim.side_out_left);
                    }
                }

            }

            @Override
            public void onItemFavFrgment(View v, int position, int mBookId, ImageView mFavIcon) {
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
        mBookAdapter.updateData(mBookList);
        recycler_books = view.findViewById(R.id.recyc_search);
        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(getActivity()) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };
        smoothScroller.setTargetPosition(0);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recycler_books.setLayoutManager(layoutManager);
        recycler_books.setNestedScrollingEnabled(false);
        layoutManager.startSmoothScroll(smoothScroller);
        recycler_books.setAdapter(mBookAdapter);
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
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


    public void downloadCompleted() {
        //Download completed popup....
        if (isDownloading) {
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
        DownloadContentManager.getInstance().setListnerForBook(SearchFragment.get());
        DownloadContentManager.getInstance().downloadBookData(bookID, SearchFragment.get().getActivity(), mProgressBar, this.view);

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

        mOkFinish.setOnClickListener(view -> {
            long now = SystemClock.elapsedRealtime();
            if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {
                manager.setLastClickMillis(now);
                alertDialog.dismiss();
            }

        });


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


    }


    private void twoParameterDialog(int dialogContainer, int yesView, int noView, View v, int bookID) {
        ViewGroup viewGroup = v.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(getActivity()).inflate(dialogContainer, viewGroup, false);
        ImageView mOkFinish = dialogView.findViewById(yesView);
        ImageView mNoBtn = dialogView.findViewById(noView);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        alertDialog.setCancelable(false);
        //Function Performed When "YES" pressed

        mOkFinish.setOnClickListener(view -> {
            // Showing Progress When Book is Downloaded
            long now = SystemClock.elapsedRealtime();
            if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {
                manager.setLastClickMillis(now);
                isDownloading = true;

                mProgressBar = v.findViewById(R.id.progress_DownloadBook);
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.fake_progress_bar));
                mProgressBar.setProgress(5);
                DownloadContentManager.getInstance().setListnerForBook(SearchFragment.get());
                DownloadContentManager.getInstance().downloadBookData(bookID, SearchFragment.get().getActivity(), mProgressBar, this.view);
                alertDialog.dismiss();
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
                }
            }
        });


    }

    @Override
    public void onResume() {
        Log.i("SearchTextResume", "bjsdgjsag " + manager.getBookSearch());

        super.onResume();
        etSearch.setText(manager.getBookSearch());
        etSearch.setSelection(etSearch.getText().length());
    }

    @Override
    public void onPause() {
        manager.setBookSearch(etSearch.getText().toString().trim());
        hideKeyboard();
        etSearch.clearFocus();
        super.onPause();
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getActivity().getCurrentFocus();
        if (view == null) {
            view = new View(getActivity());
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }

    private void setCustomTabVisible(boolean isVisible) {
        if (isVisible) {
        }
    }

    private NetworkInfo isNetworkConnected() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }
}
