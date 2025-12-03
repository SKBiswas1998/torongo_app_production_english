package com.monnfamily.enlibraryapp.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.monnfamily.enlibraryapp.Activity.LibraryViewActivity;
import com.monnfamily.enlibraryapp.Contentful.Book;
import com.monnfamily.enlibraryapp.Fragment.HomeFragment;
import com.monnfamily.enlibraryapp.R;
import com.monnfamily.enlibraryapp.Utils.App;
import com.monnfamily.enlibraryapp.Utils.AppManager;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;



public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {
    Context context;
    List<Book> mData = new ArrayList<>();
    ClickListener clickListener;
    AppManager manager = AppManager.getInstance();
    public static int numbeLoaded = 0;
    private final String TAG="BookAdapter";

    public BookAdapter(Context context, ClickListener clickListener) {
        this.context = context;
        this.clickListener = clickListener;
    }

    public void updateData(List<Book> pData) {
        this.mData.clear();
        this.mData.addAll(pData);
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.book_card_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvStatus.setVisibility(View.GONE);
        holder.imgFavoraites.setVisibility(View.GONE);
        Book tBook = mData.get(position);
        String fileName = App.get().getBookIconDir() + "/BookIcon" + tBook.getBookId() + ".png";
        File mFile = new File(fileName);
        if (!mFile.isFile()) return;
        Uri uri = Uri.parse("file://" + fileName);
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.imgFavoraites.setVisibility(View.VISIBLE);
            holder.tvStatus.setOnClickListener(null);
            holder.tvStatus.setOnClickListener(v -> clickListener.onItemClicked(v, holder.mBookId, holder.itemView));
            holder.imgFavoraites.setOnClickListener(null);
            holder.imgFavoraites.setOnClickListener(v -> clickListener.onItemFavFrgment(v, holder.getAdapterPosition(), holder.mBookId, holder.imgFavoraites));

            parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            holder.imgBookCover.setImageBitmap(image);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

//        holder.imgBookCover.setImageURI(Uri.parse("file://" + App.get().getBookIconDir() + "/BookIcon" + tBook.getBookId() + ".png"));
         numbeLoaded++;
//        if (manager.safeToHidePopup()){
//            LibraryViewActivity.get().hideInitialLoadingView();
//            HomeFragment.get().hideInitialLoadingView();
//            LibraryViewActivity.get().trueAllIconDone();
//        }
        holder.mBookId = tBook.getBookId();
        //this happen's in start to get the list of book's that are already Downloaded
        if (manager.isBookDownloaded(holder.mBookId)) {
            if (manager.checkForBookVersion(tBook.getBookId())) {
                //open
                holder.tvStatus.setText(R.string.open);
            } else {
                //update
                holder.tvStatus.setText(R.string.update);
            }
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.white));
            holder.tvStatus.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bct_open_bg));
        } else {
            holder.tvStatus.setText(R.string.get);
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.red));
            holder.tvStatus.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bct_get_bg));

        }

        //this happen's in start to get the list of book's that are already in favoraites
        if (manager.getFavoriteBook().contains(tBook.getBookId())) {
            holder.imgFavoraites.setImageResource(R.drawable.homefavoriteen);

        } else {
            holder.imgFavoraites.setImageResource(R.drawable.favorite_unselected);
        }

    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imgBookCover)
        ImageView imgBookCover;
        @BindView(R.id.imgBookCoverFavoraites)
        ImageView imgFavoraites;
        @BindView(R.id.txtStatus)
        TextView tvStatus;
        @BindView(R.id.progress_DownloadBook)
        ProgressBar mProgressbar;
        Integer mBookId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    //Interface for When book is clicked
    public interface ClickListener {
        void onItemClicked(View v, int mBookId, View mBookItemView);

        void onItemFavFrgment(View v, int position, int mBookId, ImageView mFavIcon);
    }

}
