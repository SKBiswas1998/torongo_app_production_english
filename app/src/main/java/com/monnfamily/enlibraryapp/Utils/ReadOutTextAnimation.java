package com.monnfamily.enlibraryapp.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.monnfamily.enlibraryapp.Activity.PageActivity;
import com.monnfamily.enlibraryapp.Constants.Constant;
import com.monnfamily.enlibraryapp.Contentful.Page;
import com.romainpiel.shimmer.ShimmerTextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.monnfamily.enlibraryapp.Activity.PageActivity.TAG;

public class ReadOutTextAnimation {
    public interface ReadingPageCompleted {
        // you can define any parameter as per your requirement
        public void readingPageCompleted();
    }

    private ReadingPageCompleted mListner = null;
    private ShimmerTextView mShimmerTextView;
    private Handler mHandler = new Handler();
    private int mCurrentReadoutIndex;
    private Runnable mTimerCallback;
    private Page mCurrentPage;
    private JSONArray mAudioDuration;
    /*
     * Static Singleton Function.
     */
    private static final ReadOutTextAnimation mInstance = new ReadOutTextAnimation();


    public static ReadOutTextAnimation getInstance() {
        return mInstance;
    }

    AppManager manager = AppManager.getInstance();

    /*
     * Constructor Function.
     */

    private ReadOutTextAnimation() {
        //Timer Function Callback
        mTimerCallback = new Runnable() {
            @Override
            public void run() {
                runnableCallback();
            }
        };
    }

    /*
     * Set Function.
     */
    public void setShimmerTextView(ShimmerTextView shimmerTextView) {
        this.mShimmerTextView = shimmerTextView;
    }

    /*
     * Get Function.
     */

    public int getCurrentReadoutIndex() {
        return this.mCurrentReadoutIndex;
    }

    /*
     * ReadOut Function.
     */

    public void startReadOut(Integer pPageNumber) {
        stopReadOut();
        mCurrentPage = manager.getCurrentBook().getPageDetailsForNumber(pPageNumber);
        if (mCurrentPage.getDisplayStringArray() == null) {
            String tMainFolder = App.get().getBookDir() + "/BookCompleted" + manager.getCurrentBook().getBookId().toString();
            readPageText(tMainFolder, Constant.BOOK_PAGE_TEXT + pPageNumber.toString() + ".txt", pPageNumber);
        }

        mAudioDuration = mCurrentPage.getPageAudioDurationJSON();
        mCurrentReadoutIndex = 0;
        setStringForIndex(mCurrentReadoutIndex);
        try {

            mHandler.postDelayed(mTimerCallback, (int) (mAudioDuration.getDouble(mCurrentReadoutIndex) * 1000));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void stopReadOut() {
        mHandler.removeCallbacks(mTimerCallback);
    }

    public void pauseReadOut() {
        stopReadOut();
    }

    public void resumeReadOut() {
        stopReadOut();
        mHandler.removeCallbacks(mTimerCallback);
        setStringForIndex(mCurrentReadoutIndex);
        try {

            mHandler.postDelayed(mTimerCallback, (int) (mAudioDuration.getDouble(mCurrentReadoutIndex) * 1000));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setListner(ReadingPageCompleted pListner) {
        this.mListner = pListner;
    }

    /*
     * Private Function.
     */

    private void setStringForIndex(int pIndex) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        try {
            int jsonCount = mCurrentPage.getPageAudioDurationJSON().length();
            int textCount = mCurrentPage.getDisplayStringArray().size();
            if (jsonCount!=textCount)
                throw new IndexOutOfBoundsException("Couldn't load the page, data incomplete.");
            for (int stringIndex = 0; stringIndex < mCurrentPage.getDisplayStringArray().size(); stringIndex++) {
                String word = mCurrentPage.getDisplayStringArray().get(stringIndex);
                String space = word.contains("\n")?"":" ";
                SpannableString str1 = new SpannableString(mCurrentPage.getDisplayStringArray().get(stringIndex) +space);
                if (pIndex == stringIndex)
                    str1.setSpan(new ForegroundColorSpan(Color.RED), 0, str1.length(), 0);
                else
                    str1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, str1.length(), 0);
                builder.append(str1);
            }
            mShimmerTextView.setText(builder, TextView.BufferType.SPANNABLE);

        } catch (Exception e) {
            Intent intent = new Intent(App.get().getAppContext(), PageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            App.get().getAppContext().startActivity(intent);
            Toast.makeText(App.get(), "Content not available for this book, please try again later", Toast.LENGTH_LONG).show();

            e.getMessage();
        }
    }

    public void setStringForIndexMute(int pIndex) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        try {
            for (int stringIndex = 0; stringIndex < mCurrentPage.getDisplayStringArray().size(); stringIndex++) {
                SpannableString str1 = new SpannableString(mCurrentPage.getDisplayStringArray().get(stringIndex) + " ");
                if (pIndex == stringIndex)
                    str1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, str1.length(), 0);
                else
                    str1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, str1.length(), 0);
                builder.append(str1);
            }
            mShimmerTextView.setText(builder, TextView.BufferType.SPANNABLE);

        } catch (Exception e) {
            Intent intent = new Intent(App.get().getAppContext(), PageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            App.get().getAppContext().startActivity(intent);
            Toast.makeText(App.get(), "Content not available for this book, please try again later", Toast.LENGTH_LONG).show();

            e.getMessage();
        }
    }


    private void runnableCallback() {
        try {


            if (mCurrentReadoutIndex >= mCurrentPage.getDisplayStringArray().size() - 1) {
                if (mListner != null)
                    mListner.readingPageCompleted();
                return;
            }
            //do something
            mCurrentReadoutIndex++;
            setStringForIndex(mCurrentReadoutIndex);
            try {
                mHandler.postDelayed(mTimerCallback, (int) (mAudioDuration.getDouble(mCurrentReadoutIndex) * 1000));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            Intent intent = new Intent(App.get().getAppContext(), PageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            App.get().getAppContext().startActivity(intent);
            Toast.makeText(App.get(), "Content not available for this book, please try again later", Toast.LENGTH_LONG).show();
            e.getMessage();
        }
    }


    private void readPageText(String pMainFolder, String pFileName, Integer pPageNumber) {
        File file = new File(pMainFolder, pFileName);

        if (file.isFile()) {
            try {

                BufferedReader br = new BufferedReader(new FileReader(file));
                parseServerString(br, pPageNumber);
                br.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

        }
    }


    private void parseServerString(BufferedReader pServerBuffer, Integer pPageNumber) {
        try {
            String line;
            List<String> tDisplayStringArray = new ArrayList<String>();
            while ((line = pServerBuffer.readLine()) != null) {
                String[] tCurrentLine = line.toString().split("\\s+");

                tCurrentLine[tCurrentLine.length - 1] = tCurrentLine[tCurrentLine.length - 1] + '\n' /*+ '\n'*/;
                tDisplayStringArray.addAll(Arrays.asList(tCurrentLine));
            }

            mCurrentPage.setDisplayStringArray(tDisplayStringArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Integer getElaspeTime() {
        Integer timeSkip = 0;
        try {
            for (int tCount = 0; tCount < mCurrentReadoutIndex; tCount++) {
                timeSkip += (int) (mAudioDuration.getDouble(tCount) * 1000);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return timeSkip;
    }

}
