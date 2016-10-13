package com.cml.androidimageselector;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.cml.androidimageselector.bean.FloderBean;
import com.cml.androidimageselector.utils.ImageLoader;

import java.util.List;

/**
 * 作者：陈明亮 on 2016/10/13 15:31
 * 博客：http://blog.csdn.net/zc2_5781
 */

public class ListPopupWindow extends PopupWindow {

    private  List<FloderBean> mDatas;
    private  View mConvertView;
    private int mWidth;
    private int mHeight;
    private ListView mListView;
    public interface onDirSelectedListener{
        void onSeleted(FloderBean floderBean);
    }
    public onDirSelectedListener onDirSelectedListener;

    public void setonDirSelectedListener(onDirSelectedListener onDirSelectedListener){
        this.onDirSelectedListener = onDirSelectedListener;
    }


    public ListPopupWindow (Context context, List<FloderBean> datas){
        calWidthAndHeight(context);
        this.mDatas = datas;

        mConvertView = LayoutInflater.from(context).inflate(R.layout.popwindow,null);

        setContentView(mConvertView);
        setWidth(mWidth);
        setHeight(mHeight);

        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());

        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction() == MotionEvent.ACTION_OUTSIDE){
                    dismiss();
                    return true;
                }

                return false;
            }
        });

        initViews(context);
        initEvent();
    }
    private void initViews(Context context) {
        mListView = (ListView) mConvertView.findViewById(R.id.listView);
        mListView.setAdapter(new ListDirAdapter(context,mDatas));
    }

    private void initEvent() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(onDirSelectedListener != null){
                    onDirSelectedListener.onSeleted(mDatas.get(position));
                }
            }
        });
    }

    private void calWidthAndHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);

        mWidth = outMetrics.widthPixels;
        mHeight = (int) (outMetrics.heightPixels*0.7);
    }

    private class ListDirAdapter extends ArrayAdapter<FloderBean>{

        private LayoutInflater mInflater;
        private List<FloderBean> mDatas;

        public ListDirAdapter(Context context, List<FloderBean> objects) {
            super(context, 0, objects);

            mInflater = LayoutInflater.from(context);
            this.mDatas = objects;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHoder hoder = null;
            if(convertView == null){
                hoder = new ViewHoder();
                convertView = mInflater.inflate(R.layout.item_popup,null);

                hoder.mImg = (ImageView) convertView.findViewById(R.id.id_dir_item_image);
                hoder.mDirName = (TextView) convertView.findViewById(R.id.id_dir_item_name);
                hoder.mDirCount = (TextView) convertView.findViewById(R.id.id_dir_item_count);

                convertView.setTag(hoder);
            }else{
                hoder = (ViewHoder) convertView.getTag();
            }


            FloderBean bean = getItem(position);
            hoder.mImg.setImageResource(R.mipmap.pictures_no);
            ImageLoader.newInstance().loadImage(bean.getFirstImagePath(),hoder.mImg);

            hoder.mDirCount.setText(bean.getCount()+"");
            hoder.mDirName.setText(bean.getName());

            return convertView;
        }

        private class ViewHoder{
            ImageView mImg;
            TextView mDirName;
            TextView mDirCount;
        }
    }
}
