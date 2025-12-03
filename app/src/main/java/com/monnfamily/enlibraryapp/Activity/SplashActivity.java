package com.monnfamily.enlibraryapp.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.monnfamily.enlibraryapp.Contentful.Book;
import com.monnfamily.enlibraryapp.Contentful.Page;
import com.monnfamily.enlibraryapp.Fragment.HomeFragment;
import com.monnfamily.enlibraryapp.R;
import com.monnfamily.enlibraryapp.Utils.App;
import com.monnfamily.enlibraryapp.Utils.AppManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;

public class SplashActivity extends AppCompatActivity {
    private VideoView videoHolder;
    private static final String TAG = "SplashActivity";
    private static SplashActivity instance;

    public static SplashActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        if (!isTaskRoot()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_splash);
        if (!AppManager.getInstance().mPreferences.getBoolean("IsTutorialOver", true)) {
            try {
                copyFTUEBook();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        videoHolder = findViewById(R.id.videoViewRelative);
        Thread checkIconVersion = new Thread() {
            @Override
            public void run() {
                File file = new File(App.get().getBookIconDir());
                String status = file.isDirectory() ? "Folder Exist" : "Folder doesn't Exist";
            }
        };
        checkIconVersion.start();
        LibraryViewActivity.get();
        HomeFragment.get();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    private void jump() {
        if (isFinishing())
            return;
        if (isNetworkAvailable()) {

            startActivity(new Intent(this, LibraryViewActivity.class));
            finish();
        } else {
            twoParameterDialog(R.layout.popup_network_error, R.id.img_popRetry, R.id.img_popCancel);
        }

    }

    @Override
    protected void onResume() {
        try {
            Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash640x960);
            //Delete function if not in use
            calculateWidthandHeight();

            videoHolder.setVideoURI(video);

            videoHolder.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    jump();
                }
            });
            videoHolder.start();
        } catch (Exception ex) {
            jump();
        }
        super.onResume();
    }

    /**
     * Delete Function if not of use
     */
    private void calculateWidthandHeight() {
        float originalVideoWidth = 640;
        float originalVideoHeight = 960;
        float deviceHeight = (float)getResources().getDisplayMetrics().heightPixels;
        int newWidth = getResources().getDisplayMetrics().widthPixels;
        int newHeight = (int) ((originalVideoHeight / originalVideoWidth) * newWidth);
        float scale = deviceHeight/newHeight;
//        newWidth *=scale;
        newHeight *=scale;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(newWidth, newHeight);
//        videoHolder.setBackgroundColor(Color.BLACK);
        params.gravity =Gravity.CENTER;
        videoHolder.setLayoutParams(params);

        Log.i(TAG, "calculateWidthandHeight: ");

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
                    boolean isTrue = (Objects.requireNonNull(nc).hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));
                    return isTrue;
                }
            }
        }
        return false;
    }


    private void twoParameterDialog(int dialogContainer, int yesView, int noView) {
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
        //Function Performed When"YES" pressed
        mOkFinish.setOnClickListener(view -> {
            jump();
            alertDialog.dismiss();
        });
        //Function Performed When"NO" pressed
        mNoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadLibrary();
                finish();
                alertDialog.dismiss();
            }
        });
    }

    public void loadLibrary() {
        startActivity(new Intent(this, LibraryViewActivity.class));
    }

    private void copyFTUEBook() throws IOException {
        File mainFolder = new File(App.get().getBookDir());
        if (!mainFolder.exists()) {
            mainFolder.mkdir();
            File ftue = new File(App.get().getBookDir(), "BookCompleted0");
            if (!ftue.exists()) {
                ftue.mkdir();
                try {
                    String[] list = getAssets().list("free_book");
                    //Copying Files from  assets to app internal storage
                    for (String item : list) {
                        InputStream in = null;
                        OutputStream out = null;
                        try {
                            in = getAssets().open("free_book/" + item);

                            String outDir = ftue.getAbsolutePath();

                            File outFile = new File(outDir, item);

                            out = new FileOutputStream(outFile);
                            byte[] buffer = new byte[1024];
                            int read;
                            while ((read = in.read(buffer)) != -1) {
                                out.write(buffer, 0, read);
                            }
                            in.close();
                            in = null;
                            out.flush();
                            out.close();
                            out = null;
                        } catch (IOException e) {
                            Log.e("tag", "Failed to copy asset file:" + item, e);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.i(TAG, "copyFTUEBook: Folder is not Created");
            }
        }
        //Create one book instance
        int numberOfDefault = 15;
        String[] jsondur = new String[numberOfDefault];
        jsondur[0] = " [3.179]";
        jsondur[1] = "[0.394,0.248,0.369,0.302,0.351,0.809]";
        jsondur[2] = "[0.464,0.392,0.241,0.149,0.142,0.218,0.167,0.657,0.235,0.329,0.518,0.212,0.414,0.791]";
        jsondur[3] = " [0.714,0.411,0.311,0.241,0.316,0.278,0.164,0.164,0.234,0.278,0.468,0.001,0.506,0.727]";
        jsondur[4] = " [0.369,0.122,0.117,0.226,0.434,0.471,0.456,1.096,0.668]";
        jsondur[5] = " [0.392,0.111,0.352,0.436,0.285,0.631,0.255,0.194,0.542,0.872,0.418,0.229,0.295,0.154,0.251,0.432,0.401,0.401,1.313,0.775,0.392,0.366,0.128,0.167,0.551,0.335,0.991,0.568,0.511,0.467]";
        jsondur[6] = "  [0.399,0.156,0.234,0.381,0.231,0.459,0.579,0.634,0.491,0.473,0.221,0.243,0.377,0.363,0.298,0.188,0.399,0.221,0.193,0.211,0.354]";
        jsondur[7] = "[0.001,0.231,0.248,0.612,0.533,0.001,0.369,0.654,0.242,0.321,0.163,0.145,0.236,0.254,0.236,0.369,0.412,0.254,0.201,0.248,0.375,0.218,0.171,0.454,0.387,0.248,0.163,0.345,0.285,0.333,0.254,0.327]";
        jsondur[8] = " [0.001,0.299,0.183,0.249,0.228,0.278,0.315,0.561,0.001,0.465,0.286,0.228,0.232,0.461,0.001,0.183,0.245,0.216,0.286,0.311,0.436,0.203,0.777,0.001,0.502,0.319,0.221,0.236,0.473,0.394,0.001,0.241,0.265,0.271,0.274,0.324,0.153,0.672,0.195,0.149,0.344,0.315,0.224,0.241,0.394]";
        jsondur[9] = "  [0.001,0.581,0.255,0.531,0.334,0.251,0.752,0.231,0.506,0.492,0.409,0.208,0.167,0.583,0.261,0.318,0.231,0.746,0.506,0.692,0.278,0.599,0.402,0.171,0.194,0.456,0.176,0.459]";
        jsondur[10] = "[0.576,0.784,0.312,0.257,0.192,0.772,0.474,0.329,0.219,0.765]";
        jsondur[11] = "[0.881,0.671,0.442,0.273,0.173,0.932,0.569,0.651,0.216,0.159,0.885]";
        jsondur[12] = " [0.844,0.593,0.553,0.235,0.151,0.956,0.597,0.298,0.238,0.672]";
        jsondur[13] = " [0.751,0.682,0.368,0.254,0.145,0.805,0.504,0.338,0.489,0.449]";
        jsondur[14] = "[2.0]";
        Book FTUEBook = new Book();
        ArrayList<Page> bookPages = new ArrayList<>();
        for (int i = 1; i <= numberOfDefault; i++) {
            Page item = new Page();
            item.setPageNumber(i);
            item.setPageAudioDuration(jsondur[i - 1]);
            bookPages.add(item);
        }
        FTUEBook.setBookDetail(bookPages);
        FTUEBook.setBookForFree(true);
        FTUEBook.setBookId(0);
        FTUEBook.setBookName("FTUEBook");
        AppManager.getInstance().setCurrentBook(FTUEBook);
        AppManager.getInstance().setmPageNumber(1);
    }


}
