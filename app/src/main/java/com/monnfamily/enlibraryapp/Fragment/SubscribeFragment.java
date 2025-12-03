package com.monnfamily.enlibraryapp.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.android.billingclient.api.SkuDetails;
import com.monnfamily.enlibraryapp.Activity.LibraryViewActivity;
import com.monnfamily.enlibraryapp.Activity.PageActivity;
import com.monnfamily.enlibraryapp.Constants.Constant;
import com.monnfamily.enlibraryapp.R;
import com.monnfamily.enlibraryapp.Utils.App;
import com.monnfamily.enlibraryapp.Utils.AppManager;
import com.monnfamily.enlibraryapp.Utils.PurchaseHelper;

import java.text.DecimalFormat;
import java.util.Objects;

import butterknife.BindView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SubscribeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SubscribeFragment#} factory method to
 * create an instance of this fragment.
 */
public class SubscribeFragment extends Fragment implements PurchaseHelper.PurchaseListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    static SubscribeFragment mInstance;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    @BindView(R.id.rg_ChooseSubsccription)
    RadioGroup rgSubscription;
    static boolean isGoogleConnectedShown = true;
    private OnFragmentInteractionListener mListener;
    View view;
    RadioGroup rg;
    TextView mCancelBtn, mDoneBtn;
    AppManager manager = AppManager.getInstance();
    String tMonthValue = " ....";
    String tYearValue = " ....";
    LinearLayout layout_radioButton;
    TextView txt_FreeApp;
    // variable for handling multiple clicks
//    private static final long THRESHOLD_MILLIS = 400L;
    private long lastClickMillis;

    private static FragmentActivity activity;

    public SubscribeFragment() {
        // Required empty public constructor
        if (mInstance != null)
            mInstance = null;
        mInstance = this;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SubscribeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SubscribeFragment get() {
        if (mInstance == null) {
            new SubscribeFragment();
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
        view = inflater.inflate(R.layout.fragment_subscribe, container, false);
        mDoneBtn = view.findViewById(R.id.mDoneSubsc);
        mCancelBtn = view.findViewById(R.id.mcancelSubscr);
        rg = view.findViewById(R.id.rg_ChooseSubsccription);
        layout_radioButton = view.findViewById(R.id.layout_radioButton);
        txt_FreeApp = view.findViewById(R.id.txt_FreeApp);
        App.get().getPurchaseHelper().appResumed();
//        mDoneBtn.setOnClickListener(null);

        mDoneBtn.setOnClickListener(v ->
                {
                    long now = SystemClock.elapsedRealtime();
                    if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {
                        manager.setLastClickMillis(now);

                        twoParameterDialog(R.layout.popup_new_subscription, R.id.img_subscribe_Continue, R.id.img_subscribe_Cancel, view, false, rg.getCheckedRadioButtonId());
                    }

                }
        );

        App.get().getPurchaseHelper().setListener(SubscribeFragment.this);

        mCancelBtn.setOnClickListener((v) -> {
            long now = SystemClock.elapsedRealtime();
            if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {
                manager.setLastClickMillis(now);
                mListener.mLoadHomeFragment();
            }
        });
/**
 * Commented out Cancel Functionaliy As Asked by the client and add interface function when called loads home fragment
 */


        if (App.get().getPurchaseHelper().isServiceConnected()) {

            SkuDetails tMonthSKU = App.get().getPurchaseHelper().getSubscribeDetailsForID(Constant.monthly_purchased);
            SkuDetails tYearSKU = App.get().getPurchaseHelper().getSubscribeDetailsForID(Constant.yearly_purchased);

            if (tMonthSKU != null)
                tMonthValue = "  " + tMonthSKU.getOriginalPrice();
            if (tYearSKU != null)
                tYearValue = "  " + tYearSKU.getOriginalPrice();
        }

        RadioButton tMonthText = view.findViewById(R.id.mMonthRadio);
        tMonthText.setText(getString(R.string.submonth) + tMonthValue + getString(R.string.submonthper));

        RadioButton tYearText = view.findViewById(R.id.mYearRadio);
        tYearText.setText(getString(R.string.yearsub) + tYearValue + getString(R.string.yearsubper));
        //anonymous Block to change Text For Discount

        if (!tYearValue.trim().equals("....") && !tMonthValue.trim().equals("....")) {
            Float mMonthlyCharge = (float) App.get().getPurchaseHelper().getSubscribeDetailsForID(Constant.monthly_purchased).getPriceAmountMicros() / 1000000;
            Float mYearlyCharge = (float) App.get().getPurchaseHelper().getSubscribeDetailsForID(Constant.yearly_purchased).getPriceAmountMicros() / 1000000;

            float mTotalDiscount = ((mMonthlyCharge * 12) - mYearlyCharge) / 12;
            float mPerMonth = (mMonthlyCharge - mTotalDiscount);
            //Check Whether the values have anything after decimal or not
            String month = Integer.parseInt(String.valueOf(mPerMonth).split("\\.")[1]) == 0 ? String.valueOf(mPerMonth).split("\\.")[0] : String.valueOf(mPerMonth);
            String year = Integer.parseInt(String.valueOf(mTotalDiscount).split("\\.")[1]) == 0 ? String.valueOf(mTotalDiscount).split("\\.")[0] : String.valueOf(mTotalDiscount);

            String monthCurrency = tMonthValue.replace(mMonthlyCharge.toString(), month).replace(" ", "");
            String yearCurrency = tMonthValue.replace(mMonthlyCharge.toString(), year).replace(" ", "");
            Double monthly = Double.parseDouble(monthCurrency.substring(1));
            Double yearly = Double.parseDouble(yearCurrency.substring(1));
            DecimalFormat format = new DecimalFormat(".##");
            String newMonth = format.format(monthly);
            String newYear = format.format(yearly);
            ((TextView) view.findViewById(R.id.txtDiscountOnYearSubscription)).setText("(" + monthCurrency.substring(0, 1) + newMonth + " per month, a savings of " + monthCurrency.substring(0, 1) + newYear + " per month)");
        } else if (isGoogleConnectedShown) {
            isGoogleConnectedShown = false;
            manager.setLastClickMillis(SystemClock.elapsedRealtime());
            new AlertDialog.Builder(getContext()).setMessage(getString(R.string.googlepaly_error)).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show().setCancelable(false);
        }
        updateSubscribeRadioButton();
        mListener.isFragmentLoaded();
        return view;
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

    @Override
    public void purchaseSuccess() {
//        TextView tCancelBtn = view.findViewById(R.id.mcancelSubscr);
//        tCancelBtn.getBackground().setAlpha(255);
//        tCancelBtn.setClickable(true);
//        tCancelBtn.setOnClickListener(null);
//
//        tCancelBtn.setOnClickListener(cancelView -> {
//            long now = SystemClock.elapsedRealtime();
//            if (now - lastClickMillis > THRESHOLD_MILLIS) {
//                lastClickMillis = now;
//                twoParameterDialog(R.layout.popup_cancel_subscription, R.id.img_unsubscribe_Continue, R.id.img_unsubscribe_notNow, view, true, 0);
//            }
//
//        });
    }

    @Override
    public void purchaseFailure() {

    }

    @Override
    public void onResume() {
        isGoogleConnectedShown = true;
//        App.get().volleyGet();
        super.onResume();
        if (activity == null)
            activity = getActivity();

        layout_radioButton.setVisibility(View.GONE);
        txt_FreeApp.setVisibility(View.GONE);
        if (manager.isAppFree())
            txt_FreeApp.setVisibility(View.VISIBLE);
        else
            layout_radioButton.setVisibility(View.VISIBLE);

    }

    @Override
    public void updatePurchase() {
//        TextView tCancelBtn = view.findViewById(R.id.mcancelSubscr);
//        tCancelBtn.setOnClickListener(null);
//        if (App.get().getPurchaseHelper().isAlreadySubscribed()) {
//            tCancelBtn.getBackground().setAlpha(255);
//            tCancelBtn.setClickable(true);
//            tCancelBtn.setOnClickListener(cancelView -> {
//                long now = SystemClock.elapsedRealtime();
//                if (now - lastClickMillis > THRESHOLD_MILLIS) {
//                    lastClickMillis = now;
//                    twoParameterDialog(R.layout.popup_cancel_subscription, R.id.img_unsubscribe_Continue, R.id.img_unsubscribe_notNow, view, true, 0);
//                }
//
//            });
//        } else {
//            tCancelBtn.getBackground().setAlpha(100);
//            tCancelBtn.setClickable(false);
//        }

        updateSubscribeRadioButton();
        if (manager.isNeedSubscription() && App.get().getPurchaseHelper().isAlreadySubscribed()) {
            manager.setNeedSubscription(false);
            manager.setFragmentName(manager.getPreviousFragmentName());
            Intent intent = new Intent(activity, PageActivity.class);
            intent.putExtra("mBookID", manager.getCurrentBook().getBookId());
            startActivity(intent);

        }

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

        void mLoadHomeFragment();
    }


    private void twoParameterDialog(int dialogContainer, int yesView, int noView, View v, boolean isCancelSubscription, int pLayoutID) {
        App.get().getPurchaseHelper().appResumed();
        ViewGroup viewGroup = v.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(getActivity()).inflate(dialogContainer, viewGroup, false);
        TextView mOkFinish = dialogView.findViewById(yesView);
        TextView mNoBtn = dialogView.findViewById(noView);
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
                if (isCancelSubscription) {
                    App.get().getPurchaseHelper().cancelSubscribed(getActivity());
                } else {
                    initilizeBillingFlow(pLayoutID);

                }
                alertDialog.dismiss();
            }
        });
        //Function Performed When "NO" pressed
        mNoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(PopupActivity.this, "No is Clicked", Toast.LENGTH_SHORT).show();
                long now = SystemClock.elapsedRealtime();
                if (now - manager.getLastClickMillis() > Constant.THRESHOLD_MILLIS) {
                    manager.setLastClickMillis(now);
                    alertDialog.dismiss();
                }
            }
        });
    }


    private void updateSubscribeRadioButton() {
        try {

            RadioButton tMonthText = view.findViewById(R.id.mMonthRadio);
            RadioButton tYearText = view.findViewById(R.id.mYearRadio);
            // Disable the radio button when the user subscribe to any of the program
            if (App.get().getPurchaseHelper().isAlreadySubscribed()) {
                String purchasedSKU = App.get().getPurchaseHelper().getPurchasedSKU().getSku();
                //"If" will run when the user subscribe to monthly program
                if (purchasedSKU.equals(Constant.monthly_purchased)) {
                    tMonthText.setAlpha(0.5f);
                    tMonthText.setEnabled(false);

                    tYearText.setChecked(true);
                    tMonthText.setChecked(false);
                }
                //"Else" will run when the user subscribe to yearly program
                else {
                    disableRadioButton(tMonthText, tYearText);
                }
                return;
            }
            //Below will run when the app doesn't get any data for the play store
            if (tYearValue.trim().equals("....") && tMonthValue.trim().equals("....")) {

                disableRadioButton(tMonthText, tYearText);
                return;
            }
            /**the Below code runs when play store is successfully sending the data but the user is
             * not subscribe to any the program...
             */
            tYearText.setAlpha(1.0f);
            tMonthText.setAlpha(1.0f);
            tYearText.setEnabled(true);
            tMonthText.setEnabled(true);
            mDoneBtn.setEnabled(true);
            mDoneBtn.setClickable(true);
            mCancelBtn.setEnabled(true);
            mCancelBtn.setClickable(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disableRadioButton(RadioButton tMonthText, RadioButton tYearText) {
        tMonthText.setAlpha(0.5f);
        tMonthText.setEnabled(false);

        tYearText.setAlpha(0.5f);
        tYearText.setEnabled(false);

        tYearText.setChecked(true);
        tMonthText.setChecked(false);

        Button mDoneBtn = view.findViewById(R.id.mDoneSubsc);
        mDoneBtn.getBackground().setAlpha(100);
        mDoneBtn.setClickable(false);
        mDoneBtn.setEnabled(false);
        mDoneBtn.setOnClickListener(null);

        Button mCancelBtn = view.findViewById(R.id.mcancelSubscr);
        mCancelBtn.getBackground().setAlpha(100);
        mCancelBtn.setClickable(false);
        mCancelBtn.setEnabled(false);
        mCancelBtn.setOnClickListener(null);
    }

    private void initilizeBillingFlow(int pLayoutID) {
        String tProductID = Constant.monthly_purchased;
        if (pLayoutID == R.id.mYearRadio)
            tProductID = Constant.yearly_purchased;
        App.get().getPurchaseHelper().launchBillingFlow(activity, tProductID);
    }

}
