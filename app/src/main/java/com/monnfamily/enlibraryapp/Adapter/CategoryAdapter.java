package com.monnfamily.enlibraryapp.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.monnfamily.enlibraryapp.Activity.LibraryViewActivity;
import com.monnfamily.enlibraryapp.Constants.Constant;
import com.monnfamily.enlibraryapp.Contentful.Book;
import com.monnfamily.enlibraryapp.Contentful.Category;
import com.monnfamily.enlibraryapp.Fragment.HomeFragment;
import com.monnfamily.enlibraryapp.Model.DummyCategoryModel;
import com.monnfamily.enlibraryapp.R;
import com.monnfamily.enlibraryapp.Utils.App;
import com.monnfamily.enlibraryapp.Utils.AppManager;

import java.io.File;
import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;


public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    Context context;
    //Original Path to get data for category
    static List<Category> mData = new ArrayList<>();
    ClickListener clickListener;
    static TextView mPreviousText = null;
    static Integer mPreviousSelectedID = -1;
    AppManager manager = AppManager.getInstance();
    public static int numbeLoaded = 0;
    private final String TAG="CategoryAdapter";

    public CategoryAdapter(Context context, ClickListener clickListener) {
        this.context = context;
        this.clickListener = clickListener;
    }

    public void itemNotifiedChanged(List<Category> pData) {

        mData.clear();
        //Original Path to get data for category
        mData.addAll(pData);
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo == null) {
            manager.setNetworkPopupForZeroBook(false);
        } else {
            manager.setNetworkPopupForZeroBook(true);
        }
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_item_layout, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = SystemClock.elapsedRealtime();
                if (now - manager.getLastClickMillis() < Constant.THRESHOLD_MILLIS) {
                    manager.setLastClickMillis(now);

                    return;
                }
                manager.setSelectedCategoy(viewHolder.mCategoryID, viewHolder.mPositionIndex);
                clickListener.onItemClicked(v, viewHolder.mCategoryID);
                if (mPreviousSelectedID != -1) {
                    //Unselect Cartegory
                    mPreviousText.setBackground(context.getResources().getDrawable(R.drawable.bg_cat_unselected_name));
                    mPreviousText.setTextColor(context.getResources().getColor(R.color.subscriberadiobutton));
                    //Select Cartegory
                    viewHolder.txtCategoryName.setTextColor(context.getResources().getColor(R.color.white));
                    viewHolder.txtCategoryName.setBackground(context.getResources().getDrawable(R.drawable.bg_cat_selected_name));
                    mPreviousText = viewHolder.txtCategoryName;
                }
                mPreviousSelectedID = viewHolder.getAdapterPosition();
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtCategoryName.setVisibility(View.GONE);
        holder.itemView.setClickable(false);
        Category tCategory = mData.get(position);
        String fileName = App.get().getCategoryIconDir() + "/CategoryIcon" + tCategory.getCategoryId() + ".png";
        File mFile = new File(fileName);
        if (!mFile.isFile()) return;
        Uri uri = Uri.parse("file://" + fileName);
        ParcelFileDescriptor parcelFileDescriptor = null;

//        holder.imgcategory.setImageURI(Uri.parse("file://" + fileName));
        try {
            parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            holder.imgcategory.setImageBitmap(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
        numbeLoaded++;
        if (manager.safeToHidePopup()){
            LibraryViewActivity.get().hideInitialLoadingView();
            HomeFragment.get().hideInitialLoadingView();
            LibraryViewActivity.get().trueAllIconDone();
        }
        holder.mCategoryID = tCategory.getCategoryId();
        holder.mPositionIndex = tCategory.getPositionIndex();
        String[] text;
        if (AppManager.mIsEnglishApp)
            text = tCategory.getCategoryName().trim().split(" ");
        else
            text = tCategory.getBanglaCategoryName().trim().split(" ");
        String mTempText = "";
        for (String i : text) {
            mTempText += i + "\n";
        }
        holder.itemView.setClickable(true);
        holder.txtCategoryName.setVisibility(View.VISIBLE);
        holder.txtCategoryName.setText(mTempText.trim());

        if (position == manager.getmPositionIndexCategory()) {
            mPreviousSelectedID = position;
            holder.txtCategoryName.setTextColor(context.getResources().getColor(R.color.white));
            holder.txtCategoryName.setBackground(context.getResources().getDrawable(R.drawable.bg_cat_selected_name));
            mPreviousText = holder.txtCategoryName;
        } else {
            holder.imgcategory.setBackground(null);
            holder.txtCategoryName.setTextColor(context.getResources().getColor(R.color.subscriberadiobutton));
            holder.txtCategoryName.setBackground(context.getResources().getDrawable(R.drawable.bg_cat_unselected_name));
        }
//        AppManager.isNetworkPopupForZeroBook = false;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgcategory;
        TextView txtCategoryName;
        Integer mCategoryID;
        Integer mPositionIndex;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgcategory = itemView.findViewById(R.id.img_catergory);
            txtCategoryName = itemView.findViewById(R.id.txt_categoryName);
        }
    }

    public interface ClickListener {
        void onItemClicked(View v, int position);
    }
}
