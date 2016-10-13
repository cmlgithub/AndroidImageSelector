package com.cml.androidimageselector.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * 作者：陈明亮 on 2016/10/8 16:52
 * 博客：http://blog.csdn.net/zc2_5781
 */

public abstract class BaseRecyAdapter<T,H extends BaseRecyViewHolder> extends RecyclerView.Adapter<BaseRecyViewHolder>{

    private  Context context;
    protected List<T> mDatas;
    protected LayoutInflater mInflater;
    protected int mLayoutResId;

    protected OnItemClickListener listener;

    public interface OnItemClickListener{
        void OnClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    public BaseRecyAdapter(Context context,List<T> datas,int layoutResId){
        this.context = context;
        this.mDatas = datas;
        this.mLayoutResId = layoutResId;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public BaseRecyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View view = mInflater.inflate(mLayoutResId,viewGroup,false);

        return new BaseRecyViewHolder(view,listener);
    }

    @Override
    public void onBindViewHolder(BaseRecyViewHolder baseViewHolder, int position) {

        T t = getItem(position);

        bindData(baseViewHolder,t,position);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public T getItem(int position){
        return mDatas.get(position);
    }



    public abstract void bindData(BaseRecyViewHolder viewHolder,T t,int position);


    public List<T> getmDatas(){
        return mDatas;
    }

    public void clearData(){
        mDatas.clear();
        notifyItemRangeRemoved(0,mDatas.size());
    }

    public void addData(List<T> datas){
        addData(0,datas);
    }

    public void removeData(int position){
        mDatas.remove(position);
    }

    public void addData(int position,List<T> datas){
        if(datas != null && datas.size()>0){
            mDatas.addAll(datas);
            notifyItemRangeChanged(position,mDatas.size());
        }
    }
}
