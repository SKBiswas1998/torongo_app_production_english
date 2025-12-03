package com.monnfamily.enlibraryapp.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.monnfamily.enlibraryapp.Constants.Constant;
import com.monnfamily.enlibraryapp.Contentful.Cast;
import com.monnfamily.enlibraryapp.R;
import com.monnfamily.enlibraryapp.Utils.App;
import com.monnfamily.enlibraryapp.Utils.AppManager;

import java.io.File;
import java.util.List;

public class CastAdapter extends RecyclerView.Adapter<CastAdapter.ViewHolder> {
    private Context context;
    private List<Cast> mData;
    private ClickListener clickListener;
    private static TextView previousView=null;
    private static Integer mPreviousSelected = -1;
    AppManager  manager = AppManager.getInstance();

    public CastAdapter(Context context, ClickListener clickListener) {
        this.context = context;
        this.mData = manager.getOrignalCastList();
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cast_item_layout, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                long now = SystemClock.elapsedRealtime();
                if (now - manager.getLastClickMillis() < Constant.THRESHOLD_MILLIS) {
                    manager.setLastClickMillis(now);

                    return;
                }
                manager.setSelectedCast(viewHolder.mCastID,viewHolder.mPositionIndex);
                clickListener.onItemClicked(v, viewHolder.mCastID);
                if(mPreviousSelected != -1){
                    //unselect Cast
                    previousView.setTextColor(context.getResources().getColor(R.color.red));
                    previousView.setBackground(context.getResources().getDrawable(R.drawable.bct_get_bg));
                    //Select Cast
                    viewHolder.txtCastName.setTextColor(context.getResources().getColor(R.color.white));
                    viewHolder.txtCastName.setBackground(context.getResources().getDrawable(R.drawable.bct_open_bg));
                    previousView= viewHolder.txtCastName;

                }

                mPreviousSelected = viewHolder.getAdapterPosition();
            }
        });
        return viewHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.itemView.setClickable(false);
        holder.txtCastName.setVisibility(View.GONE);
        Cast tCast = mData.get(position);
        String fileName =  App.get().getCastIconDir() + "/CastIcon" + tCast.getCastId() + ".png";
        File mFile =  new File(fileName);
        if(!mFile.isFile()) return;

        holder.imgCast.setImageURI(Uri.parse("file://" + fileName));
        holder.mCastID = tCast.getCastId();

        holder.mPositionIndex =tCast.getPositionIndex();
        if (AppManager.mIsEnglishApp)
            holder.txtCastName.setText(tCast.getCastName());
        else
            holder.txtCastName.setText(tCast.getBanglaCastName());

        holder.itemView.setClickable(true);
        holder.txtCastName.setVisibility(View.VISIBLE);
        if(position == manager.getmPositionIndexCast()){
            holder.txtCastName.setBackground(context.getResources().getDrawable(R.drawable.bct_open_bg));
            holder.txtCastName.setTextColor(Color.WHITE);
            previousView = holder.txtCastName;
            mPreviousSelected = position;
        } else {
            //unselect Cast
            holder.txtCastName.setTextColor(context.getResources().getColor(R.color.red));
            holder.txtCastName.setBackground(context.getResources().getDrawable(R.drawable.bct_get_bg));
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCast;
        TextView txtCastName;
        Integer mCastID;
        Integer mPositionIndex;


        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCast = itemView.findViewById(R.id.imgCastAvatar);
            txtCastName = itemView.findViewById(R.id.txtCastAvatarName);
        }
    }

    public interface ClickListener {
        void onItemClicked(View v, int position);
    }
}
