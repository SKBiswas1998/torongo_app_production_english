package com.monnfamily.enlibraryapp.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.monnfamily.enlibraryapp.Activity.LibraryViewActivity;
import com.monnfamily.enlibraryapp.Constants.Constant;
import com.monnfamily.enlibraryapp.Contentful.Book;
import com.monnfamily.enlibraryapp.Contentful.Cast;
import com.monnfamily.enlibraryapp.Contentful.Category;
import com.monnfamily.enlibraryapp.Contentful.Page;
import com.monnfamily.enlibraryapp.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Objects;

public class MyDownloaderAsyncTask extends AsyncTask<String, Integer, Boolean> {

    private static final String TAG = "MyDownloaderAsyncTask";

    private DownloadContentManager.DownloadCompletedListner mListnerForBook;
    private DownloadContentManager.DownloadCompletedListner mListnerForIcon;
    private Integer mBookID;
    private Integer mBookVersion;
    private AlertDialog alertDialog, alertDialog1;
    private ProgressDialog progressDialog;
    private boolean isForBooks;
    private static boolean isNetworkPoped;
    @SuppressLint("StaticFieldLeak")
    private Activity context;
    @SuppressLint("StaticFieldLeak")
    private ProgressBar mProgressStat;
    @SuppressLint("StaticFieldLeak")
    private View mFragmentView;
    private int mTotalDownload = 0, mTotalDownloaded = 0;
    AppManager manager = AppManager.getInstance();

    Long time = 0L;
    boolean isFirst = true;


    /*
     * Init Functions
     */

    public MyDownloaderAsyncTask() {
    }

    public MyDownloaderAsyncTask(DownloadContentManager.DownloadCompletedListner listener, Integer bookID, Activity context, ProgressBar mProgressBar, View view) {
        mProgressStat = null;
        mListnerForBook = listener;
        mBookID = bookID;
        this.context = context;
        isForBooks = true;
        mFragmentView = view;
        mProgressStat = mProgressBar;
        isNetworkPoped = false;
        isFirst = true;
    }

    public MyDownloaderAsyncTask(DownloadContentManager.DownloadCompletedListner listener, Activity context, View view) {
        mListnerForIcon = listener;
        this.context = context;
        isForBooks = false;
        mFragmentView = view;
        isFirst = true;

        isNetworkPoped = false;
    }


    /*
     * Download Functions
     */
    private void downloadBookImages() {
        try {
            Book tBook = manager.getBookForID(mBookID);
            mBookVersion = tBook.getVersion();
            String tMainFolder = App.get().getBookDir() + "/DownloadProgress";
            downloadBookDetails(tMainFolder, Constant.BOOK_MAIN_IMAGE + ".png", tBook.getBookMainImageURL());
            if (tBook.getBookSoundURL() != null) {
                downloadBookDetails(tMainFolder, Constant.BOOK_SOUND + ".mp3", tBook.getBookSoundURL());
                mTotalDownload = 1;
            }
            mTotalDownload += 1 + (tBook.getAllPage().size() * 3);
            mProgressStat.setProgress(0);

            for (Page tPage : tBook.getAllPage()) {
                String tPageNumber = tPage.getPageNumber().toString();

                downloadBookDetails(tMainFolder, Constant.BOOK_PAGE_IMAGE + tPageNumber + ".png", tPage.getPageImage());
                downloadBookDetails(tMainFolder, Constant.BOOK_PAGE_AUDIO + tPageNumber + ".mp3", tPage.getPageAudio());
                downloadBookDetails(tMainFolder, Constant.BOOK_PAGE_TEXT + tPageNumber + ".txt", tPage.getPageText());
            }
        } catch (Exception e) {
            checkHalfDownloadBooks();

            e.getMessage();
        }
    }

    private void dowloadAppImageIcon() {
        try {
            //For Category
            time = SystemClock.elapsedRealtime();
            if (manager.getOrignalCategoryList() != null) {

                mTotalDownload = manager.getOrignalCategoryList().size();
                for (Category tCategory : manager.getOrignalCategoryList()) {
                    File file = new File(App.get().getCategoryIconDir(), "CategoryIcon" + tCategory.getCategoryId() + ".png");


                    if (file.exists()) {
                        if (manager.getCategoryVersion(tCategory.getCategoryId()) == tCategory.getVersion())
                            continue;
                        file.delete();
                    }
                    if (AppManager.mIsEnglishApp)
                        downloadBookDetails(App.get().getCategoryIconDir(), "CategoryIcon" + tCategory.getCategoryId() + ".png", tCategory.getCategoryImageURL());
                    else
                        downloadBookDetails(App.get().getCategoryIconDir(), "CategoryIcon" + tCategory.getCategoryId() + ".png", tCategory.getBanglaCategoryImageURL());
                    manager.setCategoryVersion(tCategory.getCategoryId(), tCategory.getVersion());

                }
            }

            time = SystemClock.elapsedRealtime();

            //For Cast
            if (manager.getOrignalCastList() != null) {
                mTotalDownload += manager.getOrignalCastList().size();
                for (Cast tCast : manager.getOrignalCastList()) {
                    File file = new File(App.get().getCastIconDir(), "CastIcon" + tCast.getCastId() + ".png");

                    if (file.exists()) {
                        if (manager.getCastVersion(tCast.getCastId()) == tCast.getVersion())
                            continue;
                        file.delete();
                    }

                    downloadBookDetails(App.get().getCastIconDir(), "CastIcon" + tCast.getCastId() + ".png", tCast.getCastImageURL());
                    manager.setCastVersion(tCast.getCastId(), tCast.getVersion());
                }
            }

            time = SystemClock.elapsedRealtime();
            //For Book Icon
            if (manager.getOrignalBookList() != null) {
                mTotalDownload += manager.getOrignalBookList().size();
                for (Book tBook : manager.getOrignalBookList()) {
                    File file = new File(App.get().getBookIconDir(), "BookIcon" + tBook.getBookId() + ".png");

                    if (file.exists()) {
                        if (manager.getBookIconVersion(tBook.getBookId()) == tBook.getVersion())
                            continue;
                        file.delete();
                    }

                    downloadBookDetails(App.get().getBookIconDir(), "BookIcon" + tBook.getBookId() + ".png", tBook.getBookCoverURL());
                    manager.setBookIconVersion(tBook.getBookId(), tBook.getVersion());
                }
            }

        } catch (Exception e) {

            e.getMessage();
        }
    }

    private void downloadBookDetails(String pMainFolder, String pFileName, String pDownloadURL) {
        //Check for network.
        if (!isNetworkAvailable() && !isNetworkPoped) {
            twoParameterDialog(R.layout.popup_network_error, R.id.img_popRetry, R.id.img_popCancel, mFragmentView);
            if (mProgressStat != null) {
                mProgressStat.setVisibility(View.GONE);
            }
            isNetworkPoped = true;
        }
        int len;
        try {
            int streamSize = 8 * 1024;
            URL url = new URL(pDownloadURL);
            URLConnection ucon = url.openConnection();
            ucon.setReadTimeout(5000);
            ucon.setConnectTimeout(10000);
            ucon.connect();
            InputStream is = ucon.getInputStream();
            BufferedInputStream inStream = new BufferedInputStream(is, streamSize);
            File directory = new File(pMainFolder, pFileName);
            FileOutputStream outStream = new FileOutputStream(directory);

            byte[] buff = new byte[streamSize];


            while ((len = inStream.read(buff)) != -1) {
                outStream.write(buff, 0, len);

            }
            if (isForBooks) {
                this.mTotalDownloaded++;
                int pro = (this.mTotalDownloaded * 100) / Math.max(1, mTotalDownload);
                mProgressStat.setProgress(Math.max(1, pro));
            }
            if (mProgressStat != null)
                publishProgress(mProgressStat.getProgress());
            outStream.flush();
            outStream.close();
            inStream.close();

        } catch (Exception e) {
            //Add Network Error.
            if (!isNetworkAvailable() && !isNetworkPoped) {
                twoParameterDialog(R.layout.popup_network_error, R.id.img_popRetry, R.id.img_popCancel, mFragmentView);
                if (mProgressStat != null) {
                    mProgressStat.setVisibility(View.GONE);
                }
                isNetworkPoped = true;
            }
            e.printStackTrace();
        }
    }

    /*
     * AsyncTask Override Functions
     */


    @Override
    protected void onPreExecute() {
        if (isForBooks && isNetworkAvailable()) {
            context.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

    }


    @SuppressLint("WrongThread")
    @Override
    protected Boolean doInBackground(String... strings) {
        //Check for network
        if (isForBooks) {
            //Clear Previous Data...
            checkHalfDownloadBooks();
            String tMainFolder = App.get().getBookDir() + "/DownloadProgress";
            File directory = new File(tMainFolder);
            directory.mkdir();

            if (manager.mPreferences.contains("Book" + mBookID)) {
                downloadUpdateForBook();
            } else
                downloadBookImages();

        } else {
            dowloadAppImageIcon();
        }
        return true;
    }

    private void downloadUpdateForBook() {
        String string = manager.mPreferences.getString("Book" + mBookID, "");
        SparseIntArray array = new SparseIntArray();
        String[] tStringList = string.split("#");
        ArrayList<Integer> listToDownload = new ArrayList<>();
        Book tbook = manager.getBookForID(mBookID);
        mBookVersion = tbook.getVersion();
        for (String bookVersion : tStringList) {
            if (bookVersion.length() > 0) {
                String[] tseperateVersion = bookVersion.split(";");
                array.put(Integer.valueOf(tseperateVersion[0]), Integer.valueOf(tseperateVersion[1]));
            }
        }

        for (Page m : tbook.getAllPage()) {
            //if page doesn't exist
            if ((array.get(m.getPageNumber(), -1) == -1) ||// If page doesn't exist
                    (array.get(m.getPageNumber(), -1) != m.getPageVersion())) // If Existing page gets updated
            {
                listToDownload.add(m.getPageNumber());
            }
        }
        int noOfPagesRemoved = array.size() - tbook.getAllPage().size();
        int i = 0;

        /**
         * remove Last waste pages
         */
        while (noOfPagesRemoved > 0) {
            deleteFile(App.get().getBookPath(mBookID + "") + File.separator + Constant.BOOK_PAGE_IMAGE + (array.size() - i) + ".png");
            deleteFile(App.get().getBookPath(mBookID + "") + File.separator + Constant.BOOK_PAGE_AUDIO + (array.size() - i) + ".mp3");
            deleteFile(App.get().getBookPath(mBookID + "") + File.separator + Constant.BOOK_PAGE_TEXT + (array.size() - i) + ".txt");
            noOfPagesRemoved--;
            i++;
        }
/**
 * Downloading newly updated pages
 */
        downloadBookDetails(App.get().getBookDir() + "/DownloadProgress", Constant.BOOK_MAIN_IMAGE + ".png", tbook.getBookMainImageURL());
        if (tbook.getBookSoundURL() != null) {
            downloadBookDetails(App.get().getBookDir() + "/DownloadProgress", Constant.BOOK_SOUND + ".mp3", tbook.getBookSoundURL());
            mTotalDownload = 1;
        }
        mTotalDownload += 1 + (listToDownload.size() * 3);
        mProgressStat.setProgress(0);
        for (Integer m : listToDownload) {
            downloadBookDetails(App.get().getBookDir() + "/DownloadProgress", Constant.BOOK_PAGE_IMAGE + m + ".png", tbook.getAllPage().get(m - 1).getPageImage());
            downloadBookDetails(App.get().getBookDir() + "/DownloadProgress", Constant.BOOK_PAGE_AUDIO + m + ".mp3", tbook.getAllPage().get(m - 1).getPageAudio());
            downloadBookDetails(App.get().getBookDir() + "/DownloadProgress", Constant.BOOK_PAGE_TEXT + m + ".txt", tbook.getAllPage().get(m - 1).getPageText());
        }

    }

    public void deleteFile(String mFileName) {
        File file = new File(mFileName);
        if (file.exists())
            file.delete();
    }

    protected void onPostExecute(Boolean result) {
        if (result) {
            if (isForBooks) { // Check for Network
                context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                String tMainFolder = App.get().getBookDir() + "/BookCompleted" + mBookID;
                String tDownloadedFolder = App.get().getBookDir() + "/DownloadProgress";

                File mainDirectory = new File(tMainFolder);
                File downDirectory = new File(tDownloadedFolder);
                //This Condition Follows on update
                if (mainDirectory.exists()) {


                    /**
                     * new Update code
                     * Here, we will get the list of files that are downloaded
                     * Which just require changing file name
                     */
                    for (String mfile : downDirectory.list()) {
                        String[] strings = mfile.split("/");
                        String mainFile = strings[strings.length - 1];
                        File fileToDelete = new File(tMainFolder, mainFile);
                        File fileToSave = new File(downDirectory, mainFile);
                        deleteFile(tMainFolder + File.separator + mainFile);
                        fileToSave.renameTo(fileToDelete);

                    }
                    if (mListnerForBook != null)
                        mListnerForBook.downloadCompleted();
                    mListnerForBook = null;
                    manager.setBookVersion(mBookID, mBookVersion);
                    //TODO: Creating logic when the book is loaded for the first time
                    /**
                     * When book loaded/downloaded for the first time then it will create key for
                     * the book and pages version inside the preferences
                     */
                    addingBookForFirstTime();
                    if (downDirectory.list().length == 0)
                        deleteFile(tDownloadedFolder);
                } else if (downDirectory.exists()) {
                    downDirectory.renameTo(mainDirectory);


                    if (mListnerForBook != null)
                        mListnerForBook.downloadCompleted();
                    mListnerForBook = null;
                    manager.setBookVersion(mBookID, mBookVersion);
                    //TODO: Creating logic when the book is loaded for the first time
                    /**
                     * When book loaded/downloaded for the first time then it will create key for
                     * the book and pages version inside the preferences
                     */
                    addingBookForFirstTime();
                } else {
                    checkHalfDownloadBooks();
                    mListnerForBook = null;
                    manager.setIsBookDownloadInProgress(false);
                    if (manager.isNeedsUpdateFromCMS())
                        LibraryViewActivity.get().forceUpdateContainer();


                    //Error Popup..
                    if (!isNetworkAvailable() && !isNetworkPoped) {
                        twoParameterDialog(R.layout.popup_network_error, R.id.img_popRetry, R.id.img_popCancel, mFragmentView);
                        isNetworkPoped = true;
                        mProgressStat.setVisibility(View.GONE);
                        return;
                    }
                    Toast.makeText(App.get().getAppContext(), " Content not available for this book. please try again later", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (mListnerForIcon != null) {
                    mListnerForIcon.downloadCompleted();
                }
                mListnerForIcon = null;

            }

        } else {
            if (this.progressDialog != null)
                this.progressDialog.dismiss();
            this.progressDialog = null;
        }

    }

    private void addingBookForFirstTime() {
        Book tBook = manager.getBookForID(mBookID);
        StringBuffer string = new StringBuffer();
        int totalNoOfPages = tBook.getAllPage().size();
        for (int i = 0; i < totalNoOfPages; i++) {
            Page page = tBook.getAllPage().get(i);
            string.append(page.getPageNumber() + ";");
            string.append(page.getPageVersion());
            if (i < totalNoOfPages - 1) {
                string.append("#");
            }
        }
        manager.mPreferences.edit().putString("Book" + mBookID, string + "").apply();
    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (isFirst) {
            if (values[0] < 80) {
                isFirst = false;
                mProgressStat.setProgress(5);
                mProgressStat.setProgressDrawable(context.getResources().getDrawable(R.drawable.progressbar_background));
            }
            return;
        }
        mProgressStat.setVisibility(View.VISIBLE);
        mProgressStat.setProgress(values[0]);

    }

    public static void checkHalfDownloadBooks() {
       try{

           String tMainFolder = App.get().getBookDir() + "/DownloadProgress";
           File directory = new File(tMainFolder);
           if (directory.exists()) {
               String[] entries = directory.list();
               Log.i(TAG, "checkHalfDownloadBooks: Ucon => "+ entries.toString());
               for (String s : entries) {
                   File currentFile = new File(directory.getPath(), s);
                   currentFile.delete();
               }
               directory.delete();
           }
       }catch (Exception e){
           e.printStackTrace();
       }
    }


    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) App.get().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT < 23) {
                final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null) {
                    return (activeNetworkInfo.isConnected() && (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI || activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE));
                }
            } else {
                final Network network = connectivityManager.getActiveNetwork();

                if (network != null) {
                    final NetworkCapabilities nc = connectivityManager.getNetworkCapabilities(network);
                    return (Objects.requireNonNull(nc).hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));
                }
            }
        }
        return false;
    }

    private void twoParameterDialog(int dialogContainer, int yesView, int noView, View
            mFragmentView) {
        manager.setNetworkPopupVisibility(true);
        ViewGroup viewGroup = mFragmentView.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(context).inflate(dialogContainer, viewGroup, false);
        ImageView mOkFinish = dialogView.findViewById(yesView);
        ImageView mNoBtn = dialogView.findViewById(noView);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setView(dialogView);
        alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        alertDialog.setCancelable(false);
        //Function Performed When "YES" pressed
        mOkFinish.setOnClickListener(view -> {
            manager.setNetworkPopupVisibility(false);

            if (isForBooks) {
                alertDialog.dismiss();
                if (mListnerForBook != null)
                    mListnerForBook.downloadNetworkError(alertDialog);
            } else
                alertDialog.dismiss();
        });
        //Function Performed When "NO" pressed
        mNoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.setNetworkPopupVisibility(false);

                alertDialog.dismiss();
            }
        });
    }


}

