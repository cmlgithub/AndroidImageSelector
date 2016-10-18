package com.cml.androidimageselector;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.cml.androidimageselector.base.BaseRecyViewHolder;
import com.cml.androidimageselector.base.SimpleRecyAdapter;
import com.cml.androidimageselector.utils.ImageLoader;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * 作者：陈明亮 on 2016/10/18 09:53
 * 博客：http://blog.csdn.net/zc2_5781
 */

public class GalleryAdapter extends SimpleRecyAdapter<String> {

    private List<String> mImgs;
    private File mCurrentDir;
    private Context context;
    private Set<String> mSelectImagePath;
    private int canSelectMaxImageCout;

    public GalleryAdapter(Context context, List<String> datas, File mCurrentDir,Set<String> mSelectImagePath,int canSelectMaxImageCout) {
        super(context, datas, R.layout.item_gallery);
        this.context = context;
        this.mImgs = datas;
        this.mCurrentDir = mCurrentDir;
        this.mSelectImagePath = mSelectImagePath;
        this.canSelectMaxImageCout = canSelectMaxImageCout;
    }

    @Override
    public void bindData(BaseRecyViewHolder viewHolder, String path, int position) {
        ImageView imageView = viewHolder.getImageView(R.id.id_item_image);
        final ImageButton imageButton = (ImageButton) viewHolder.getView(R.id.id_item_select);
        imageView.setImageResource(R.mipmap.pictures_no);
        imageButton.setImageResource(R.mipmap.picture_unselected);
        path = mCurrentDir.getAbsolutePath() + "/" + path;
        ImageLoader.newInstance().loadImage(path,imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "点击了图片", Toast.LENGTH_SHORT).show();
            }
        });

        final String  key = path;
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = imageButton.getTag();
                if(tag == null){//未选中

                    if(mSelectImagePath.size() >= canSelectMaxImageCout){
                        Toast.makeText(context, "够了", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    imageButton.setImageResource(R.mipmap.pictures_selected);
                    imageButton.setTag("");
                    mSelectImagePath.add(key);
                }else{
                    imageButton.setImageResource(R.mipmap.picture_unselected);
                    imageButton.setTag(null);
                    mSelectImagePath.remove(key);
                }

                if(selectOrCancleSelectImage != null){
                    selectOrCancleSelectImage.selectOrCancleSelectImage();
                }

            }
        });
        if(mSelectImagePath.size() > 0 ){
            if(mSelectImagePath.contains(key)){
                imageButton.setImageResource(R.mipmap.pictures_selected);
                imageButton.setTag("");
            }
        }
    }

    private SelectOrCancleSelectImage selectOrCancleSelectImage ;

    public void setSelectOrCancleSelectImage(SelectOrCancleSelectImage selectOrCancleSelectImage){
        this.selectOrCancleSelectImage = selectOrCancleSelectImage;
    }

    public interface SelectOrCancleSelectImage{
        void selectOrCancleSelectImage();
    }
}
