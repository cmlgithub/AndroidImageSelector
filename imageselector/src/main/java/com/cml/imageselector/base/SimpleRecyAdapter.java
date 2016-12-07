package com.cml.imageselector.base;

import android.content.Context;

import java.util.List;

/**
 * 作者：陈明亮 on 2016/10/8 17:23
 * 博客：http://blog.csdn.net/zc2_5781
 */

public abstract class SimpleRecyAdapter<T> extends BaseRecyAdapter<T,BaseRecyViewHolder> {


    public SimpleRecyAdapter(Context context, List<T> datas, int layoutResId) {
        super(context, datas, layoutResId);
    }

}
