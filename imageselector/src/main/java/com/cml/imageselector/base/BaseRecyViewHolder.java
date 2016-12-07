package com.cml.imageselector.base;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 作者：陈明亮 on 2016/10/8 17:04
 * 博客：http://blog.csdn.net/zc2_5781
 */

public class BaseRecyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    private SparseArray<View> views;
    protected BaseRecyAdapter.OnItemClickListener listener;

    public BaseRecyViewHolder(View itemView,BaseRecyAdapter.OnItemClickListener listener) {
        super(itemView);

        views = new SparseArray<>();

        this.listener = listener;

        itemView.setOnClickListener(this);
    }

    public View getView(int id){
        return findView(id);
    }

    public TextView getTextView(int id){
        return findView(id);
    }

    public ImageView getImageView(int id){
        return findView(id);
    }


    private <T extends View> T findView(int id ){
        View view = views.get(id);
        if(view == null){
            view = itemView.findViewById(id);
            views.put(id,view);
        }

        return (T)view;
    }


    @Override
    public void onClick(View v) {
        if(listener != null){
            listener.OnClick(v,getLayoutPosition());
        }
    }
}
