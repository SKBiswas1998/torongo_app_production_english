package com.monnfamily.enlibraryapp.Utils;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;

import com.monnfamily.enlibraryapp.Constants.Constant;
import com.monnfamily.enlibraryapp.Contentful.Book;
import com.monnfamily.enlibraryapp.Contentful.Page;
import com.monnfamily.enlibraryapp.Model.DownloadModel;
import com.monnfamily.enlibraryapp.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.security.auth.login.LoginException;

public class DownloadContentManager {
    /*
     * Interface Class for Download complete
     */
    AppManager manager = AppManager.getInstance();
    MyDownloaderAsyncTask downloadingTask = null;

    Map<String, DownloadBookPages> mAsyncDownloadList;

    public interface DownloadCompletedListner {
        // you can define any parameter as per your requirement
        void downloadCompleted();

        void downloadNetworkError(AlertDialog alertDialog);
    }

    private DownloadCompletedListner mListnerForBook;
    private DownloadCompletedListner mListnerForIcon;

    static float mRemainingAssets = 0;
    ProgressBar mProgressBar;
    static int book_Id = 0;
    static boolean isNeworkPopupOpen = false;
    Activity context;
    private AlertDialog alertDialog;
    private View mFragmentView;
    /*
     * Singleton Instance
     */

    private static final DownloadContentManager mInstance = new DownloadContentManager();

    public static DownloadContentManager getInstance() {
        return mInstance;
    }

    private DownloadContentManager() {
    }


    public void setListnerForBook(DownloadContentManager.DownloadCompletedListner pListner) {
        this.mListnerForBook = pListner;
    }

    public void setListnerForIcon(DownloadContentManager.DownloadCompletedListner pListner) {
        this.mListnerForIcon = pListner;
    }

    public void downloadDefaultIcons(Activity context, View view) {

        //Default Folder for All Icons
        String tMainFolder = App.get().getDefaultIconDir();
        File directory = new File(tMainFolder);
        if (!directory.exists()) {
            directory.mkdir();
        }

        //Category Folder
        tMainFolder = App.get().getCategoryIconDir();
        directory = new File(tMainFolder);
        if (!directory.exists()) {
            directory.mkdir();
        }

        //Cast Folder
        tMainFolder = App.get().getCastIconDir();
        directory = new File(tMainFolder);
        if (!directory.exists()) {
            directory.mkdir();
        }

        //Book Icon Folder
        tMainFolder = App.get().getBookIconDir();
        directory = new File(tMainFolder);
        if (!directory.exists()) {
            directory.mkdir();
        }

        downloadingTask = (MyDownloaderAsyncTask) new MyDownloaderAsyncTask(mListnerForIcon, context, view).execute();
    }

    public boolean terminateDownload() {
        if (downloadingTask != null) {
            return downloadingTask.cancel(true);
        }
        return false;
    }

    public void downloadBookData(int bookID, Activity context, ProgressBar mProgressBar, View view) {
        //Check for Base folder
        String tBaseFolder = App.get().getBookDir();
        File directory = new File(tBaseFolder);
        if (!directory.exists())
            directory.mkdir();
        manager.setIsBookDownloadInProgress(true);

//        downloadingTask = (MyDownloaderAsyncTask) new MyDownloaderAsyncTask(mListnerForBook, bookID, context, mProgressBar, view).execute();


        //----------------------------------------New Download flow for book
        context.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        boolean isNetworkAvail = new MyDownloaderAsyncTask().isNetworkAvailable();
        this.context = context;
        if (!isNetworkAvail && !isNeworkPopupOpen) {
            context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            twoParameterDialog(R.layout.popup_network_error, R.id.img_popRetry, R.id.img_popCancel, view);
            isNeworkPopupOpen = true;
            mProgressBar.setVisibility(View.GONE);
            return;
        }
        isNeworkPopupOpen = false;
        try {
            mRemainingAssets = 0;
            manager.setTotalRemaining(0);
            DownloadBookPages.isInterrupt = false;
            book_Id = 0;
            this.mProgressBar = null;
            book_Id = bookID;
            this.mProgressBar = mProgressBar;
            this.mProgressBar.setProgress(0);
            this.mProgressBar.setMax(100);
            this.mFragmentView = view;
            Book tBook = manager.getBookForID(bookID);
            String tMainFolder = App.get().getBookDir() + "/DownloadProgress";
            File mFile = new File(tMainFolder);
            if (!mFile.isDirectory())
                mFile.mkdir();
            Map<String, String> mAssetsList = new LinkedHashMap<>();
            mAssetsList.put(Constant.BOOK_MAIN_IMAGE + ".png", tBook.getBookMainImageURL());
            if (tBook.getBookSoundURL() != null)
                mAssetsList.put(Constant.BOOK_SOUND + ".mp3", tBook.getBookSoundURL());
            if (manager.mPreferences.contains("Book" + book_Id))
                downloadUpdateForBook(mAssetsList);
            else
                for (Page page : tBook.getAllPage()) {
                    mAssetsList.put(Constant.BOOK_PAGE_IMAGE + page.getPageNumber() + ".png", page.getPageImage());
                    mAssetsList.put(Constant.BOOK_PAGE_AUDIO + page.getPageNumber() + ".mp3", page.getPageAudio());
                    mAssetsList.put(Constant.BOOK_PAGE_TEXT + page.getPageNumber() + ".txt", page.getPageText());
                }

            manager.setTotalRemaining((int) mAssetsList.size());
            mAsyncDownloadList = new HashMap<String, DownloadBookPages>();
            for (Map.Entry asset : mAssetsList.entrySet()) {
                DownloadModel model = new DownloadModel(tMainFolder + File.separator + asset.getKey(), (String) asset.getValue());
                //-----------------------------Multiple download
                DownloadBookPages task = (DownloadBookPages) new DownloadBookPages();
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, model);

                mAsyncDownloadList.put(tMainFolder + File.separator + asset.getKey(), task);

            }
        } catch (Exception e) {
            MyDownloaderAsyncTask.checkHalfDownloadBooks();

            e.getMessage();
        }
    }

    private void downloadUpdateForBook(Map mAssetsList) {
        String string = manager.mPreferences.getString("Book" + book_Id, "");
        SparseIntArray array = new SparseIntArray();
        String[] tStringList = string.split("#");
        ArrayList<Integer> listToDownload = new ArrayList<>();
        Book tbook = manager.getBookForID(book_Id);
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
            deleteFile(App.get().getBookPath(book_Id + "") + File.separator + Constant.BOOK_PAGE_IMAGE + (array.size() - i) + ".png");
            deleteFile(App.get().getBookPath(book_Id + "") + File.separator + Constant.BOOK_PAGE_AUDIO + (array.size() - i) + ".mp3");
            deleteFile(App.get().getBookPath(book_Id + "") + File.separator + Constant.BOOK_PAGE_TEXT + (array.size() - i) + ".txt");
            noOfPagesRemoved--;
            i++;
        }
/**
 * Downloading newly updated pages
 */
        for (Integer m : listToDownload) {
            mAssetsList.put(Constant.BOOK_PAGE_IMAGE + m + ".png", tbook.getAllPage().get(m - 1).getPageImage());
            mAssetsList.put(Constant.BOOK_PAGE_AUDIO + m + ".mp3", tbook.getAllPage().get(m - 1).getPageAudio());
            mAssetsList.put(Constant.BOOK_PAGE_TEXT + m + ".txt", tbook.getAllPage().get(m - 1).getPageText());
        }

    }

    public void deleteFile(String mFileName) {
        File file = new File(mFileName);
        if (file.exists())
            file.delete();
    }

    public void updateProgress(boolean isInterrupted, String fileName) {
        int mTotalAssets = manager.getTotalRemaining();
        mRemainingAssets += 1;
        int percentage = (int) ((mRemainingAssets / AppManager.getInstance().getTotalRemaining()) * 100);
        mProgressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.progressbar_background));
        mProgressBar.setProgress(percentage);
        DownloadBookPages termiantingTask = mAsyncDownloadList.get(fileName);
        if (termiantingTask != null) {
            termiantingTask.cancel(true);
        }
        Log.i("updateProgress", "updateProgress: " + percentage);
        try {
            this.mProgressBar.setProgress(percentage);
            if (isInterrupted) {
                if (!isNeworkPopupOpen) {
                    if (mAsyncDownloadList.size()>0){
                        for(Map.Entry m:mAsyncDownloadList.entrySet()){
                            if (!((DownloadBookPages)m.getValue()).isCancelled()){
                                ((DownloadBookPages)m.getValue()).cancel(true);
                            }
                        }
                        mAsyncDownloadList.clear();
                        mAsyncDownloadList=null;
                    }
                    context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    twoParameterDialog(R.layout.popup_network_error, R.id.img_popRetry, R.id.img_popCancel, mFragmentView);
                    isNeworkPopupOpen = true;
                    mProgressBar.setVisibility(View.GONE);
                }
                return;
            }
            if (percentage == 100) {
                String tempFolderPath = App.get().getBookDir() + "/DownloadProgress";
                String bookFolderPath = App.get().getBookPath(String.valueOf(book_Id));
                File tempFolder = new File(tempFolderPath);
                File bookFolder = new File(bookFolderPath);
                /**
                 * First Time Download
                 */
                context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                if (bookFolder.exists()) {
                    for (String mfile : tempFolder.list()) {
                        String[] strings = mfile.split("/");
                        String mainFile = strings[strings.length - 1];
                        File fileToDelete = new File(bookFolderPath, mainFile);
                        File fileToSave = new File(tempFolderPath, mainFile);
                        deleteFile(bookFolderPath + File.separator + mainFile);
                        fileToSave.renameTo(fileToDelete);

                    }
                    if (mListnerForBook != null)
                        mListnerForBook.downloadCompleted();
                    mListnerForBook = null;
                    Book tBook = manager.getBookForID(book_Id);
                    manager.setBookVersion(book_Id, tBook.getVersion());
                    //TODO: Creating logic when the book is loaded for the first time
                    /**
                     * When book loaded/downloaded for the first time then it will create key for
                     * the book and pages version inside the preferences
                     */
                    addingBookForFirstTime();

                } else if (tempFolder.exists()) {
                    tempFolder.renameTo(bookFolder);
                    if (mListnerForBook != null)
                        mListnerForBook.downloadCompleted();
                    mListnerForBook = null;
                    Book tBook = manager.getBookForID(book_Id);
                    manager.setBookVersion(book_Id, tBook.getVersion());
                    addingBookForFirstTime();
                }

                Log.i("TAG", "updateProgress: Download Complete");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addingBookForFirstTime() {
        Book tBook = manager.getBookForID(book_Id);
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
        manager.mPreferences.edit().putString("Book" + book_Id, string + "").apply();
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

            alertDialog.dismiss();
            if (mListnerForBook != null)
                mListnerForBook.downloadNetworkError(alertDialog);

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
