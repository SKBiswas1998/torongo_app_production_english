package com.monnfamily.enlibraryapp.Utils;

import android.os.AsyncTask;
import android.util.Log;

import com.monnfamily.enlibraryapp.Model.DownloadModel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;

public class DownloadBookPages extends AsyncTask<DownloadModel, Integer, Boolean> {
    String mFileName;
    String mAssetsUrl;
    static boolean isInterrupt = false;
    private WeakReference<DownloadModel> mData;

    @Override
    protected Boolean doInBackground(DownloadModel... params) {
        int len;
        try {
            this.mData = new WeakReference<>(params[0]);
            Log.i("TAG", "downloadBookData: Multiple download thread key = " +mData.get().getmFileLocation()+ ", Value = " + mData.get().getmAssetUrl());
            int streamSize = 9 * 1024;
            URL url = new URL(mData.get().getmAssetUrl());
            URLConnection ucon = url.openConnection();
            ucon.setReadTimeout(5000);
            ucon.setConnectTimeout(10000);

            Log.i("TAG", "doInBackground: UCOn => "+ucon.toString());
            ucon.connect();
            InputStream is = ucon.getInputStream();
            BufferedInputStream inStream = new BufferedInputStream(is, streamSize);
            File directory = new File(mData.get().getmFileLocation());
            FileOutputStream outStream = new FileOutputStream(directory);

            byte[] buff = new byte[streamSize];
             while ((len = inStream.read(buff)) != -1) {
                outStream.write(buff, 0, len);
            }
            outStream.flush();
            outStream.close();
            inStream.close();
        } catch (Exception e) {
            MyDownloaderAsyncTask.checkHalfDownloadBooks();
            isInterrupt= true;
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        DownloadContentManager.getInstance().updateProgress(isInterrupt,mData.get().getmFileLocation());
    }
}
