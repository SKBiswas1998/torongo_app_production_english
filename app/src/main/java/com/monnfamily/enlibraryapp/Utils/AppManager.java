package com.monnfamily.enlibraryapp.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import android.util.Log;
import android.util.SparseIntArray;

import com.monnfamily.enlibraryapp.Adapter.CategoryAdapter;
import com.monnfamily.enlibraryapp.Contentful.Book;
import com.monnfamily.enlibraryapp.Contentful.Cast;
import com.monnfamily.enlibraryapp.Contentful.Category;
import com.monnfamily.enlibraryapp.Contentful.MakeAppFree;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AppManager {
    private boolean mMusicOn;
    private boolean mPageAudio;
    private List<Integer> mFavoriteBook = new ArrayList<>();
    private Book mCurrentBook;

    private List<Book> mOriginalBookList = new ArrayList<>();
    private List<Cast> mOriginalCastList = new ArrayList<>();
    private List<Category> mOriginalCategoryList = new ArrayList<>();
    private boolean isAppFree = false;

    private boolean mNeedsBGMusic = true;
    private boolean mNeedsPageAudio = true;

    private SparseIntArray mBookVersion = new SparseIntArray();
    private SparseIntArray mBookIconVersion = new SparseIntArray();
    private SparseIntArray mCastVersion = new SparseIntArray();
    private SparseIntArray mCategoryVersion = new SparseIntArray();

    private Integer mSelectedCategoy = 1;
    private Integer mSelectedCast = 1;
    private Integer mPositionIndexCast = 0;
    private Integer mPositionIndexCategory = 0;
    private boolean mIsBookDownloadInProgress = false;
    private boolean mNeedsUpdateFromCMS = false;

    public static final boolean mIsEnglishApp = true;
    public SharedPreferences mPreferences;  //SharedPreferences for User Value
    private long lastClickMillis = 0;
    private boolean isDownloading = false;
    private boolean needSubscription = false;
    private boolean isAutoPlay = true;
    private FragmentName mFragmentName = FragmentName.NONE;
    private FragmentName mPreviousFragment = FragmentName.NONE;

    private String mBookSearch = " ";

    private boolean isNetworkPopupVisible = false;

    private String mDeveiceCountryCode = "null";


    public Integer getmPositionIndexCast() {
        return mPositionIndexCast;
    }

    public Integer getmPositionIndexCategory() {
        return mPositionIndexCategory;
    }


    /**
     * If the network fail's just after installing the app
     */
    private boolean isNetworkPopupForZeroBook;
    /*
     * Static Singleton Function.
     */
    private static final AppManager mInstance = new AppManager();

    public static AppManager getInstance() {
        return mInstance;
    }

    private int mTotalRemaining =0;



    /*
     * Constructor Function.
     */
    private AppManager() {
        setContext();
    }

    private void setContext() {
        mPreferences = App.get().getApplicationContext().getSharedPreferences("AppDefaultValue", Context.MODE_PRIVATE);
        readDefaultValue();
    }


    /*
     * Public Set Function.
     */
    public void setMusicOn(boolean pMusicOn) {
        this.mMusicOn = pMusicOn;
        mPreferences.edit().putBoolean("DeviceMusic", this.mMusicOn).apply();
    }

    public void setmMusicOnforPage(boolean pMusicOn1) {
        this.mPageAudio = pMusicOn1;
        mPreferences.edit().putBoolean("DeviceMusicForPage", this.mPageAudio).apply();
    }


    public void setFavoriteBook(Integer pFavoriteBookID) {
        this.mFavoriteBook.add(pFavoriteBookID);
        writeFavoriteBook();
    }

    public void removeFavoriteBook(Integer pFavoriteBookID) {
        this.mFavoriteBook.remove(pFavoriteBookID);
        writeFavoriteBook();
    }

    public void setCurrentBook(Book pCurrentBook) {
        this.mCurrentBook = pCurrentBook;
    }

    public void setNeedsBGMusic(boolean pNeedsBGMusic) {
        this.mNeedsBGMusic = pNeedsBGMusic;
        mPreferences.edit().putBoolean("DeviceMusic", this.mNeedsBGMusic).apply();
    }

    public void setmNeedsPageAudio(boolean pNeedsPageSFX) {
        this.mNeedsPageAudio = pNeedsPageSFX;
        mPreferences.edit().putBoolean("DeviceMusicForPage", this.mNeedsPageAudio).apply();
    }

    public void setOrignalBookList(List<Book> pOrignalBookList) {
        this.mOriginalBookList = pOrignalBookList;
    }

    public void setBookVersion(Integer pBookID, Integer pBookVersion) {
        mBookVersion.put(pBookID, pBookVersion);
        updateVersions(this.mBookVersion, "BookVersions");
    }

    public void setBookIconVersion(Integer pBookID, Integer pBookVersion) {
        mBookIconVersion.put(pBookID, pBookVersion);
        updateVersions(this.mBookIconVersion, "BookIconVersions");
    }


    public void setOriginalCastList(List<Cast> pOriginalCastList) {
        int i = 0;

        for (Cast item : pOriginalCastList) {
            item.setPositionIndex(i);
            i++;
        }
        this.mOriginalCastList = pOriginalCastList;
    }

    public void setCastVersion(Integer pCastID, Integer pCastVersion) {
        mCastVersion.put(pCastID, pCastVersion);
        updateVersions(this.mCastVersion, "CastVersions");
    }

    public void setOriginalCategoryList(List<Category> pOriginalCategoryList) {

        int i = 0;
        for (Category item : pOriginalCategoryList) {
            item.setPositionIndex(i);
            i++;
        }
        this.mOriginalCategoryList = pOriginalCategoryList;
    }

    public void setCategoryVersion(Integer pCategoryID, Integer pCategoryVersion) {
        mCategoryVersion.put(pCategoryID, pCategoryVersion);
        updateVersions(this.mCategoryVersion, "CategoryVersions");
    }


    public void setSelectedCategoy(Integer pValue, Integer pIndex) {
        if (pValue < 0) return;
        mPositionIndexCategory = pIndex;
        mSelectedCategoy = pValue;
//        mPreferences.edit().putInt("SelectedCategory", this.mSelectedCategoy).apply();
    }

    public void setSelectedCast(Integer pValue, Integer pIndex) {
        try {
            if (pValue < 0) return;
            mPositionIndexCast = pIndex;
            mSelectedCast = pValue;
//            mPreferences.edit().putInt("SelectedCast", this.mSelectedCast).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setIsBookDownloadInProgress(boolean value) {
        mIsBookDownloadInProgress = value;
    }

    public void setNeedsUpdateFromCMS(boolean value) {
        mNeedsUpdateFromCMS = value;
    }

    public void setNeedSubscription(boolean needSubscription) {
        this.needSubscription = needSubscription;
    }

    public void setAutoPlay(boolean autoPlay) {
        isAutoPlay = autoPlay;
    }

    public void setNetworkPopupForZeroBook(boolean networkPopupForZeroBook) {
        isNetworkPopupForZeroBook = networkPopupForZeroBook;
    }

    public void setFragmentName(FragmentName mFragmentName) {
        this.mPreviousFragment = this.mFragmentName;
        this.mFragmentName = mFragmentName;
    }

    public void setBookSearch(String bookearch) {
        mBookSearch = bookearch;
    }

    public boolean isNetworkPopupVisible() {
        return isNetworkPopupVisible;
    }

    public void setTotalRemaining(int mTotalRemaining) {
        this.mTotalRemaining = mTotalRemaining;
    }

    public void setAppFree(List<MakeAppFree> appFree) {
        if (appFree.size()<=0)return;
        this.isAppFree = appFree.get(0).isAppFree();
        Log.i("AppManager", "setmCountryCodeList: "+this.isAppFree);
    }

    public void setmDeveiceCountryCode(String mDeveiceCountryCode) {
        this.mDeveiceCountryCode = mDeveiceCountryCode;
    }
    /*
     * Public Get Function.
     */
    public boolean getMusicOn() {
        return mMusicOn;
    }

    public boolean getPageAudio() {
        return mPageAudio;
    }

    public List<Integer> getFavoriteBook() {
        return mFavoriteBook;
    }

    public Book getCurrentBook() {
        return mCurrentBook;
    }

    public List<Book> getOrignalBookList() {
        List<Book> returnObject = new ArrayList<>();
        returnObject.addAll(mOriginalBookList);
        return returnObject;
    }

    public List<Cast> getOrignalCastList() {
        List<Cast> returnObject = new ArrayList<>();
        returnObject.addAll(mOriginalCastList);
        return returnObject;
    }

    public List<Category> getOrignalCategoryList() {
        List<Category> returnObject = new ArrayList<>();
        returnObject.addAll(mOriginalCategoryList);
        return returnObject;
    }

    public Book getBookForID(Integer pBookID) {
        List<Book> returnObject = getOrignalBookList();
        for (Book bookDetails : returnObject) {
            if (pBookID.equals(bookDetails.getBookId()))
                return bookDetails;
        }
        return null;
    }

    public boolean getNeedsBGMusic() {
        return mNeedsBGMusic;
    }

    public boolean getmNeedsPageAudio() {
        return mNeedsPageAudio;
    }

    public Integer getBookVersion(Integer pBookID) {
        return mBookVersion.get(pBookID, -1);
    }

    public boolean checkForBookVersion(Integer pBookID) {
        boolean istrue =(this.getBookVersion(pBookID) == this.getBookForID(pBookID).getVersion());
        return istrue;
    }

    public Integer getBookIconVersion(Integer pBookID) {
        return mBookIconVersion.get(pBookID, -1);
    }

    public Integer getCastVersion(Integer pCastID) {
        return mCastVersion.get(pCastID, -1);
    }

    public Integer getCategoryVersion(Integer pCategoryID) {
        return mCategoryVersion.get(pCategoryID, -1);
    }

    public List<Book> getStringFilterBook(String charText) {
        charText = charText.toLowerCase();
        List<Book> tData = new ArrayList<>();
        if (charText.length() > 0) {
            for (Book book : this.getOrignalBookList()) {
                if (book.getBookName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    tData.add(book);
                }
            }
        }
        return tData;
    }

    public List<Book> getCategoryFilterBook(Integer categoryID) {
        List<Book> tData = new ArrayList<>();

        if (categoryID >= 0) {
            for (Book book : this.getOrignalBookList()) {
                if (book.containsCategory(categoryID))
                    tData.add(book);
            }
        }
        return tData;
    }

    public List<Book> getCastFilterBook(Integer castID) {
        List<Book> tData = new ArrayList<>();

        if (castID >= 0) {
            for (Book book : this.mOriginalBookList) {
                if (book.containsCast(castID))
                    tData.add(book);
            }
        }
        return tData;
    }

    public List<Book> getCategoryDefaultFilterBook() {
        List<Book> tData = new ArrayList<>();
        if (mSelectedCategoy >= 0) {
            for (Book book : this.getOrignalBookList()) {
                if (book.containsCategory(mSelectedCategoy))
                    tData.add(book);
            }

        }
        return tData;
    }

    public List<Book> getCastDefaultFilterBook() {
        List<Book> tData = new ArrayList<>();
        if (mSelectedCast >= 0) {
            for (Book book : this.mOriginalBookList) {
                if (book.containsCast(mSelectedCast))
                    tData.add(book);
            }
        }
        return tData;
    }

    public List<Book> getFavuriteBook() {
        List<Book> tData = new ArrayList<>();
        List<Integer> tFavoriteItem = new ArrayList<>(mFavoriteBook);
        Collections.reverse(tFavoriteItem);
        for (Integer fbookID : tFavoriteItem) {
            tData.add(this.getBookForID(fbookID));
        }
        return tData;
    }
    public boolean isAppFree() {
        return isAppFree;
    }
    public Integer getSelectedCategoy() {
        return mSelectedCategoy;
    }

    public Integer getSelectedCast() {
        return mSelectedCast;
    }

    public boolean isBookDownloaded(Integer pBookID) {
        String tMainFolder = App.get().getBookDir() + "/BookCompleted" + pBookID;
        File directory = new File(tMainFolder);
        return directory.exists();
    }

    public boolean isBookDownloadInProgress() {
        return mIsBookDownloadInProgress;
    }

    public boolean isNeedsUpdateFromCMS() {
        return mNeedsUpdateFromCMS;
    }

    public boolean isForUpdate(Integer bookID) {
        return isBookDownloaded(bookID) && !checkForBookVersion(bookID);
    }

    public boolean isNeedSubscription() {
        return needSubscription;
    }

    public boolean isAutoPlay() {
        return isAutoPlay;
    }

    public boolean isNetworkPopupForZeroBook() {
        return isNetworkPopupForZeroBook;
    }

    public FragmentName getFragmentName() {
        return mFragmentName;
    }

    public FragmentName getPreviousFragmentName() {
        return mPreviousFragment;
    }

    public String getBookSearch() {
        return mBookSearch;
    }

    public int getTotalRemaining() {
        return mTotalRemaining;
    }




    public void checkCurrentBookData() {

        //Getting the Fresh list of book published on contentful and create a list of id's
        List<Integer> allBookId = new ArrayList<>();
        for (Book bookData : this.mOriginalBookList) {
            allBookId.add(bookData.getBookId());
        }

        //For Favourite
        List<Integer> tFavoriteItem = new ArrayList<>(mFavoriteBook);
        for (Integer bookID : tFavoriteItem) {

            if (!allBookId.contains(bookID))
                removeFavoriteBook(bookID);
        }

        //For Download book
        SparseIntArray tBookVersion = mBookVersion.clone();

        for (int i = 0; i < tBookVersion.size(); i++) {
            if (!allBookId.contains(tBookVersion.keyAt(i))) {
                mBookVersion.delete(tBookVersion.keyAt(i));
                deleteBook(tBookVersion.keyAt(i));
            }
        }
        updateVersions(this.mBookVersion, "BookVersions");


        //For Downlaoded Book Cover Image
        SparseIntArray tBookIconVersion = mBookIconVersion.clone();
        for (int i = 0; i < tBookIconVersion.size(); i++) {
            if (!allBookId.contains(tBookIconVersion.keyAt(i))) {
                mBookIconVersion.delete(tBookIconVersion.keyAt(i));
                File file = new File(App.get().getBookIconDir(), "BookIcon" + tBookIconVersion.keyAt(i) + ".png");
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
        updateVersions(this.mBookIconVersion, "BookIconVersions");
    }

    public void setNetworkPopupVisibility(boolean networkPopupVisible) {
        isNetworkPopupVisible = networkPopupVisible;
    }

    public String getmDeveiceCountryCode() {
        return mDeveiceCountryCode;
    }
    /*
     * Private Function.
     */


    private void deleteBook(int bookId) {
        String tMainFolder = App.get().getBookDir() + "/BookCompleted" + bookId;
        File directory = new File(tMainFolder);
        if (directory.exists()) {
            String[] entries = directory.list();
            for (String s : entries) {
                File currentFile = new File(directory.getPath(), s);
                currentFile.delete();
            }
            directory.delete();
            mPreferences.edit().remove("Book"+bookId).apply();
        }
    }

    private void writeFavoriteBook() {
        StringBuilder tWriteString = new StringBuilder();
        for (Integer bookID : this.mFavoriteBook) {
            tWriteString.append(bookID.toString()).append(",");
        }
        mPreferences.edit().putString("FavoriteBookID", tWriteString.toString()).apply();
    }

    private void readFavoriteBook(String pBookList) {
        String[] tStringList = pBookList.split(",");
        for (String bookID : tStringList) {
            if (bookID.length() > 0)
                this.mFavoriteBook.add(Integer.valueOf(bookID));
        }
    }

    private void updateVersions(SparseIntArray pArray, String pPrefernceString) {
        StringBuilder tWriteString = new StringBuilder();
        for (int pIndex = 0; pIndex < pArray.size(); pIndex++) {
            tWriteString.append(pArray.keyAt(pIndex)).append(";");
            tWriteString.append(pArray.valueAt(pIndex)).append("#");
        }
        mPreferences.edit().putString(pPrefernceString, tWriteString.toString()).apply();
    }

    private void readVersion(String pBookList, SparseIntArray pArray) {
        String[] tStringList = pBookList.split("#");
        for (String bookVersion : tStringList) {
            if (bookVersion.length() > 0) {
                String[] tseperateVersion = bookVersion.split(";");
                pArray.put(Integer.valueOf(tseperateVersion[0]), Integer.valueOf(tseperateVersion[1]));
            }
        }
    }



    /**
     * Function's Store and retrive mPageNumber  while Reading Book
     *
     * @return
     */
    public int getmPageNumber() {
        return mPreferences.getInt("mPageNumber", 1);
    }

    public void setmPageNumber(int mPageNumber) {
        mPreferences.edit().putInt("mPageNumber", mPageNumber).apply();
    }

    /**
     * Setting a boolean flag to get the info whether is subscribed or not to enable or disable the
     * subscribe button at the end of the page
     */
    public boolean getIsSubscribe() {
        return mPreferences.getBoolean("isSubscribe", false);
    }

    public void setIsSubscribe(boolean isSubscribe) {
        mPreferences.edit().putBoolean("isSubscribe", isSubscribe).apply();
    }

    /**
     * Setting lastClickMillis to make app listen one Click at a time
     *
     * @return
     */
    public long getLastClickMillis() {
        return lastClickMillis;
    }

    public void setLastClickMillis(long lastClickMillis) {
        this.lastClickMillis = lastClickMillis;
    }

    /**
     * If any Downloading or syncing process is on
     */


    public boolean isDownloading() {
        return isDownloading;
    }

    public void setDownloading(boolean downloading) {
        isDownloading = downloading;
    }

    public boolean safeToHidePopup() {
        if (!isBookDownloadInProgress() && CategoryAdapter.numbeLoaded > 2)
            return true;
        return false;
    }

    private void readDefaultValue() {
        boolean hasValue = mPreferences.getBoolean("HasDefaultValue", false);
        if (!hasValue)
            writeDefaultValue();
        mMusicOn = mPreferences.getBoolean("DeviceMusic", true);
        mPageAudio = mPreferences.getBoolean("DeviceMusicForPage", true);
        readFavoriteBook(mPreferences.getString("FavoriteBookID", ""));
        readVersion(mPreferences.getString("BookVersions", ""), this.mBookVersion);
        readVersion(mPreferences.getString("BookIconVersions", ""), this.mBookIconVersion);
        readVersion(mPreferences.getString("CastVersions", ""), this.mCastVersion);
        readVersion(mPreferences.getString("CategoryVersions", ""), this.mCategoryVersion);
        mSelectedCategoy = -1; //mPreferences.getInt("SelectedCategory", 1);
        mSelectedCast = -1; //mPreferences.getInt("SelectedCast", 1);

    }

    private void writeDefaultValue() {
        mPreferences.edit().putBoolean("HasDefaultValue", true).apply();
        mPreferences.edit().putBoolean("DeviceMusic", true).apply();
        mPreferences.edit().putBoolean("DeviceMusicForPage", true).apply();
        //------------------------------------------------------------ For FTUE
        mPreferences.edit().putBoolean("IsTutorialOver", false).apply();
        mPreferences.edit().putBoolean("IsLoadingCompleteForFTUE", false).apply();
        //------------------------------------------------------------
        mPreferences.edit().putString("CurrentLanguage", "English").apply();
        mPreferences.edit().putString("FavoriteBookID", "").apply();
        mPreferences.edit().putString("BookVersions", "").apply();
        mPreferences.edit().putString("BookIconVersions", "").apply();
        mPreferences.edit().putString("CastVersions", "").apply();
        mPreferences.edit().putString("CategoryVersions", "").apply();
        mPreferences.edit().putString("CountryCode", "NA").apply();

//        mPreferences.edit().putInt("SelectedCategory", 1).apply();
//        mPreferences.edit().putInt("SelectedCast", 1).apply();
    }
}
