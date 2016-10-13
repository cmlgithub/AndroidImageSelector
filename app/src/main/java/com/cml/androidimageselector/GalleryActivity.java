package com.cml.androidimageselector;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.cml.androidimageselector.base.BaseRecyViewHolder;
import com.cml.androidimageselector.base.SimpleRecyAdapter;
import com.cml.androidimageselector.bean.FloderBean;
import com.cml.androidimageselector.utils.ImageLoader;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GalleryActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView mRecyclerView;
    private TextView mId_choose_dir;
    private TextView mId_total_count;
    private String storageNoMountedToastString = "当前存储卡不可用";
    private String noScanImage = "没有扫描到图片";
    private ProgressDialog progressDialog;
    private Set<String> mSelectImagePath = new HashSet<>();
    /**
     * 扫描拿到所有的图片文件夹
     */
    private List<FloderBean> mFloderBeans = new ArrayList<>();
    /**
     * 存储文件夹中的图片数量
     */
    private int mMaxCount;
    private File mCurrentDir;
    private static final int DATA_LOADED = 0X110;

    /**
     * 所有的图片
     */
    private List<String> mImgs;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == DATA_LOADED){
                progressDialog.dismiss();
                data2View();

                initPopWindow();
            }
        }
    };
    private View lineView;
    private ListPopupWindow listPopupWindow;

    private void data2View() {
        if(mCurrentDir == null){
            Toast.makeText(this, noScanImage, Toast.LENGTH_SHORT).show();
            return;
        }

        mImgs = Arrays.asList(mCurrentDir.list());


        mRecyclerView.setLayoutManager(new GridLayoutManager(this,3));

        mRecyclerView.setAdapter(new SimpleRecyAdapter<String>(GalleryActivity.this,mImgs,R.layout.item_gallery) {
            @Override
            public void bindData(BaseRecyViewHolder viewHolder,  String path, int position) {
                ImageView imageView = viewHolder.getImageView(R.id.id_item_image);
                final ImageButton imageButton = (ImageButton) viewHolder.getView(R.id.id_item_select);
                imageView.setImageResource(R.mipmap.pictures_no);
                path = mCurrentDir.getAbsolutePath() + "/" + path;
                ImageLoader.newInstance().loadImage(path,imageView);

//                Glide.with(GalleryActivity.this).
//                        load(path).
//                        asBitmap(). //强制处理为bitmap
//                        placeholder(R.mipmap.pictures_no).//加载中显示的图片
//                        error(R.mipmap.pictures_no).//加载失败时显示的图片
//                        into(imageView);//显示到目标View中

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(GalleryActivity.this, "点击了图片", Toast.LENGTH_SHORT).show();
                    }
                });

                final String  key = path;

                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Object tag = imageButton.getTag();
                        if(tag == null){//未选中
                            imageButton.setImageResource(R.mipmap.pictures_selected);
                            imageButton.setTag("");
                            mSelectImagePath.add(key);
                        }else{
                            imageButton.setImageResource(R.mipmap.picture_unselected);
                            imageButton.setTag(null);
                            mSelectImagePath.remove(key);
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
        });


        mId_choose_dir.setText(mCurrentDir.getAbsolutePath());
        mId_total_count.setText(mCurrentDir.list().length+"");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);


        findView();


        initDatas();

    }

    private void initPopWindow() {
        listPopupWindow = new ListPopupWindow(this, mFloderBeans);
        listPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                lightOn();
            }
        });

        listPopupWindow.setonDirSelectedListener(new ListPopupWindow.onDirSelectedListener() {
            @Override
            public void onSeleted(FloderBean floderBean) {
                mCurrentDir = new File(floderBean.getDir());
                mImgs = Arrays.asList(mCurrentDir.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        if(filename.endsWith(".jpg")||filename.endsWith(".jpeg")||filename.endsWith(".png"))
                            return true;
                        return false;
                    }
                }));

                mRecyclerView.setAdapter(new SimpleRecyAdapter<String>(GalleryActivity.this,mImgs,R.layout.item_gallery) {
                    @Override
                    public void bindData(BaseRecyViewHolder viewHolder, String path, int position) {
                        ImageView imageView = viewHolder.getImageView(R.id.id_item_image);
                        final ImageButton imageButton = (ImageButton) viewHolder.getView(R.id.id_item_select);
                        imageView.setImageResource(R.mipmap.pictures_no);
                        imageButton.setImageResource(R.mipmap.picture_unselected);
                        imageButton.setTag(null);
                        path = mCurrentDir.getAbsolutePath() + "/" + path;
                        ImageLoader.newInstance().loadImage(path,imageView);


                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(GalleryActivity.this, "点击了图片", Toast.LENGTH_SHORT).show();
                            }
                        });

                        final String  key = path;

                        imageButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Object tag = imageButton.getTag();
                                if(tag == null){//未选中
                                    imageButton.setImageResource(R.mipmap.pictures_selected);
                                    imageButton.setTag("");
                                    mSelectImagePath.add(key);
                                }else{
                                    imageButton.setImageResource(R.mipmap.picture_unselected);
                                    imageButton.setTag(null);
                                    mSelectImagePath.remove(key);
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
                });



                mId_choose_dir.setText(mCurrentDir.getAbsolutePath());
                mId_total_count.setText(mCurrentDir.list().length+"");

                listPopupWindow.dismiss();
            }
        });
    }

    private void lightOn() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 1.0f;
        getWindow().setAttributes(lp);
    }

    /**
     * 扫描手机中的所有图片
     */
    private void initDatas() {

        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(this, storageNoMountedToastString, Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = ProgressDialog.show(this,null,"loading...");

        //开启线程扫描手机中的所有图片
        new Thread(){
            @Override
            public void run() {
                //Uri指向所有图片
                Uri mImgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                //ContentResolver查询uri
                ContentResolver cr = GalleryActivity.this.getContentResolver();
                Cursor cursor = cr.query(mImgUri, null,
                        MediaStore.Images.Media.MIME_TYPE + "=? or "
                                + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[] { "image/jpeg", "image/png" },
                        MediaStore.Images.Media.DATE_MODIFIED);
                //防止重复遍历
                Set<String> mDirPaths = new HashSet<String>();


                while (cursor.moveToNext()){
                    //当前图片的路径
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

                    File parentFile = new File(path).getParentFile();

                    if(parentFile == null){
                        continue;
                    }

                    String dirPath = parentFile.getAbsolutePath();

                    FloderBean floderBean = null;

                    if(mDirPaths.contains(dirPath)){
                        continue;
                    }else {
                        mDirPaths.add(dirPath);
                        floderBean = new FloderBean();
                        floderBean.setDir(dirPath);
                        floderBean.setFirstImagePath(path);
                    }

                    if(parentFile.list() == null){
                        continue;
                    }

                    int picSize = parentFile.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String filename) {

                            if(filename.endsWith(".jpg")||filename.endsWith(".jpeg")||filename.endsWith(".png"))
                                return true;
                            return false;
                        }
                    }).length;

                    floderBean.setCount(picSize);
                    mFloderBeans.add(floderBean);

                    if(picSize > mMaxCount){
                        mMaxCount = picSize;
                        mCurrentDir = parentFile;
                    }

                }

                cursor.close();
                //通知图片扫描完成
                mHandler.sendEmptyMessage(0x110);
            }
        }.start();

    }

    private void findView() {
        lineView = findViewById(R.id.line);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_gallery);
        mId_choose_dir = (TextView) findViewById(R.id.id_choose_dir);
        mId_total_count = (TextView) findViewById(R.id.id_total_count);
        mId_choose_dir.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.id_choose_dir:
                listPopupWindow.showAsDropDown(mId_choose_dir,0,0);
                lightOff();
                break;
        }
    }

    private void lightOff() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.3f;
        getWindow().setAttributes(lp);
    }
}
