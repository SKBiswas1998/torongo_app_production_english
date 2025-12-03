package com.monnfamily.enlibraryapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.monnfamily.enlibraryapp.R;
import com.monnfamily.enlibraryapp.Utils.AppManager;
import com.monnfamily.enlibraryapp.Utils.LinkUtilities;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.toolBar_activity)
    Toolbar toolbar;


    @BindView(R.id.privacy)
    RelativeLayout linearPrivacy;
    @BindView(R.id.ShareThisAppButton)
    RelativeLayout layoutShare;
    @BindView(R.id.RateThisAppButton)
    RelativeLayout layoutRateApp;
    @BindView(R.id.contactus)
    RelativeLayout layoutContact;
    @BindView(R.id.clickhere)
    TextView txtClickHere;
    @BindView(R.id.txt_Emailid)
    TextView txtEmail;
    @BindView(R.id.switchBackgroundMusic)
    Switch switchBackground;
    @BindView(R.id.switchVocalSound)
    Switch switchVocal;
    AppManager  manager = AppManager.getInstance();

    private boolean mIsButtonClicked = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        linearPrivacy.setOnClickListener(this);
        layoutContact.setOnClickListener(this);
        layoutRateApp.setOnClickListener(this);
        layoutShare.setOnClickListener(this);
        txtClickHere.setOnClickListener(this);
        txtEmail.setOnClickListener(this);
        switchBackground.setChecked(manager.getMusicOn());
        switchVocal.setChecked(manager.getPageAudio());
        switchBackground.setOnCheckedChangeListener((buttonView, isChecked) -> manager.setMusicOn(isChecked));
        switchVocal.setOnCheckedChangeListener((buttonView, isChecked) -> manager.setmMusicOnforPage(isChecked));

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onClick(View v) {
        if(mIsButtonClicked) return;
        mIsButtonClicked = true;
            switch (v.getId()) {
                case R.id.RateThisAppButton:
                case R.id.clickhere:
                    LinkUtilities.rateThisApp(SettingActivity.this);
                    break;
                case R.id.ShareThisAppButton:
                    LinkUtilities.shareThisApp(SettingActivity.this);
                    break;
                case R.id.contactus:
                case R.id.txt_Emailid:
                    LinkUtilities.contactUs(SettingActivity.this);
                    break;
                case R.id.privacy:
                    startActivity(new Intent(SettingActivity.this, PrivacyActivity.class));
                    break;
            }
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

    }

    @Override
    protected void onResume() {
        mIsButtonClicked = false;
        super.onResume();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
