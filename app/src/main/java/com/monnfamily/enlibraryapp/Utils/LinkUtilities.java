package com.monnfamily.enlibraryapp.Utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.monnfamily.enlibraryapp.BuildConfig;
import com.monnfamily.enlibraryapp.Constants.Constant;
import com.monnfamily.enlibraryapp.R;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class LinkUtilities{

    public static void shareThisApp(Context context){
        final String appPackageName = BuildConfig.APPLICATION_ID;
        final String appName = App.get().getString(R.string.share_this_app_email);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.setType("text/plain");
        String shareBodyText = App.get().getString(R.string.share_body)+"\n\nhttps://play.google.com/store/apps/details?id=" + appPackageName;
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, appName);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText);
        context.startActivity(Intent.createChooser(shareIntent, "Share With "));

    }

    public static void rateThisApp(Context context){
        Uri uri = Uri.parse("http://play.google.com/store/apps/details?id=" + App.get().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + App.get().getPackageName())));
        }
    }


    public static void contactUs(Context context){

        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "hello@lovelands.me", null));
        intent.putExtra(Intent.EXTRA_SUBJECT, "");
        intent.putExtra(Intent.EXTRA_TEXT, "");
        context.startActivity(Intent.createChooser(intent, "Choose an Email:"));
    }
}
