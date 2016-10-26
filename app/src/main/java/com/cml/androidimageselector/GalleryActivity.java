package com.cml.androidimageselector;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
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
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.cml.androidimageselector.bean.FloderBean;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GalleryActivity extends AppCompatActivity implements View.OnClickListener ,GalleryAdapter.SelectOrCancleSelectImage{

    private int canSelectMaxImageCout = 9;
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
    private TextView complete;
    private Toolbar mToolbar;


    private void data2View() {
        if(mCurrentDir == null){
            Toast.makeText(this, noScanImage, Toast.LENGTH_SHORT).show();
            return;
        }

        mImgs = Arrays.asList(mCurrentDir.list());
        mImgs = Arrays.asList(mCurrentDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if(filename.endsWith(".jpg")||filename.endsWith(".jpeg")||filename.endsWith(".png"))
                    return true;
                return false;
            }
        }));
        GalleryAdapter galleryAdapter = new GalleryAdapter(this, mImgs, mCurrentDir, mSelectImagePath,canSelectMaxImageCout);
        galleryAdapter.setSelectOrCancleSelectImage(this);
        mRecyclerView.setAdapter(galleryAdapter);
        String[] split = mCurrentDir.getAbsolutePath().split("/");
        mId_choose_dir.setText(split[split.length-1]);
        mId_total_count.setText(mCurrentDir.list().length+"");


        if(listPopupWindow != null){
            listPopupWindow.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        findView();
        initDatas();
    }

    private void findView() {
        mToolbar = (Toolbar) findViewById(R.id.gralleryToolbar);
        lineView = findViewById(R.id.line);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_gallery);
        mId_choose_dir = (TextView) findViewById(R.id.id_choose_dir);
        mId_total_count = (TextView) findViewById(R.id.id_total_count);
        complete = (TextView) findViewById(R.id.complete);
        mId_choose_dir.setOnClickListener(this);
        complete.setOnClickListener(this);

        mRecyclerView.setLayoutManager(new GridLayoutManager(this,3));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * 扫描手机中的所有图片
     */
    private void initDatas() {
        scanPhoneImage();
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
                data2View();
            }
        });
    }


    /**
     * 扫描手机中的图片
     */
    private void scanPhoneImage() {

        //判断sd卡是否挂载
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
                Set<String> mDirPaths = new HashSet<String>();//Camera路径
                while (cursor.moveToNext()){
                    //当前图片的路径
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));//150451

                    File parentFile = new File(path).getParentFile();//Camera

                    if(parentFile == null){
                        continue;
                    }

                    String dirPath = parentFile.getAbsolutePath();//Camera absolutePath

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



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.id_choose_dir:
                listPopupWindow.showAsDropDown(mId_choose_dir,0,0);
                lightOff();
                break;
            case R.id.complete:
                Intent intent = new Intent();
                ArrayList<String> mDatas = new ArrayList<>();
                Iterator<String> it = mSelectImagePath.iterator();
                while (it.hasNext()) {
                    String str = it.next();
                    mDatas.add(str);
                }
                intent.putStringArrayListExtra("data",mDatas);
                setResult(2,intent);
                break;
        }
    }

    private void lightOn() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 1.0f;
        getWindow().setAttributes(lp);
    }


    private void lightOff() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.3f;
        getWindow().setAttributes(lp);
    }

    @Override
    public void selectOrCancleSelectImage() {
        if(mSelectImagePath.size()>0){
            complete.setEnabled(true);
            complete.setText("完成("+mSelectImagePath.size()+")");
        }else{
            complete.setEnabled(false);
        }
    }
}
