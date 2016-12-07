package com.cml.imageselector;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.cml.imageselector.bean.FloderBean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.cml.imageselector.R.id.complete;

public class ImageSelectorActivity extends AppCompatActivity implements View.OnClickListener,GalleryAdapter.SelectOrCancleSelectImage {

    public static final String MODE = "mode";
    public static final String SEPARATE_MODE = "separateMode";
    public static final String TOGETHER_MODE = "togetherMode";

    public static final String RESULT_DATA = "result_data";
    public static final String ISCLIPPING = "isClipping";

    public static final int CAMERA = 3;
    public static final int CLIPPING = 4;
    public static final int REQUEST_EXTERNAL_STORAGE = 0;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private String noScanImage = "没有扫描到图片";

    private boolean isSeparateMode ;
    private boolean isClipping ;
    private Toolbar mToolBar;
    private TextView mPhotograph;
    private TextView mGallery;
    private String imagePath;
    private LinearLayout mGralleryLinearLayout;
    private View mLineView;
    private TextView mId_choose_dir;
    private TextView mId_total_count;
    private RecyclerView mRecyclerView;
    private TextView mComplete;
    private ProgressDialog progressDialog;
    private List<FloderBean> mFloderBeans = new ArrayList<>();
    private static final int DATA_LOADED = 0X110;
    private Set<String> mSelectImagePath = new HashSet<>();
    private int canSelectMaxImageCout = 9;
    private ListPopupWindow listPopupWindow;

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

    private List<String> mImgs;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getParams()){
            setContentView(R.layout.activity_image_selector_separate);
            findViewsForSeparate();
        }else {
            setContentView(R.layout.activity_image_selector_together);
        }
    }

    private void findViewsForSeparate() {
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mToolBar.setVisibility(View.GONE);
        mPhotograph = (TextView) findViewById(R.id.photograph);
        mGallery = (TextView) findViewById(R.id.gallery);
        mGralleryLinearLayout = (LinearLayout) findViewById(R.id.gralleryLinearLayout);
        mPhotograph.setOnClickListener(this);
        mGallery.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.photograph){
            verifyStoragePermissions();
        }else if(v.getId() == R.id.gallery){
            mGralleryLinearLayout.setVisibility(View.VISIBLE);
            mToolBar.setVisibility(View.VISIBLE);
            mLineView = findViewById(R.id.line);
            mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_gallery);
            mId_choose_dir = (TextView) findViewById(R.id.id_choose_dir);
            mId_total_count = (TextView) findViewById(R.id.id_total_count);
            mComplete = (TextView) findViewById(complete);
            mId_choose_dir.setOnClickListener(this);
            mComplete.setOnClickListener(this);
            mRecyclerView.setLayoutManager(new GridLayoutManager(this,3));
            mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });


            scanPhoneImage();
        }else if(v.getId() == R.id.id_choose_dir){
            listPopupWindow.showAsDropDown(mId_choose_dir,0,0);
            lightOff();
        }else if(v.getId() == R.id.complete){
            Intent intent = new Intent();
            ArrayList<String> mDatas = new ArrayList<>();
            Iterator<String> it = mSelectImagePath.iterator();
            while (it.hasNext()) {
                String str = it.next();
                mDatas.add(str);
            }
            intent.putStringArrayListExtra("data",mDatas);
            setResult(2,intent);
        }
    }


    private String storageNoMountedToastString = "当前存储卡不可用";
    private int mMaxCount;
    private File mCurrentDir;
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
                ContentResolver cr = ImageSelectorActivity.this.getContentResolver();
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

    public  void verifyStoragePermissions() {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(this,PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
        }else {
            camera();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if (requestCode == REQUEST_EXTERNAL_STORAGE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                camera();
            } else{
                // Permission Denied
                Toast.makeText(ImageSelectorActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void camera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //判断是否有相机
        if(cameraIntent.resolveActivity(getPackageManager()) != null){
            imagePath = getImagePath();
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(imagePath)));
            startActivityForResult(cameraIntent,CAMERA);
        }else{
            Toast.makeText(this, "Camera does not exist", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean getParams() {
        Intent intent = getIntent();
        String mode = intent.getStringExtra(MODE);
        isClipping = intent.getBooleanExtra(ISCLIPPING,false);
        if(SEPARATE_MODE.equals(mode)){
            isSeparateMode = true;
        }else {
            isSeparateMode = false;
        }
        return isSeparateMode;
    }

    private String getImagePath(){
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        String imageFileName = "JPEG_"+timeStamp;
        File image = null;
        try {
            image = File.createTempFile(imageFileName, ".jpg", path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  image.getAbsolutePath();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            switch (requestCode){
                case CAMERA:
                    if(isClipping){
                        File temp = new File(imagePath);
                        startPhotoZoom(Uri.fromFile(temp));
                    }else {
                        Intent intent = new Intent();
                        sendBroadcastUpdateDICM();
                        intent.putExtra(RESULT_DATA,imagePath);
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                    break;

                case CLIPPING:
                    if (data != null) {
                        setPicToSD(data);
                    }
                    break;
            }
        }


        if(requestCode == 1 && resultCode == 2){//图库返回
            setResult(RESULT_OK,data);
            finish();
        }

    }

    private void sendBroadcastUpdateDICM() {
        Intent updateDICMIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri uri = Uri.fromFile(f);
        updateDICMIntent.setData(uri);
        sendBroadcast(updateDICMIntent);
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    public void startPhotoZoom(Uri uri) {
        /*
         * 至于下面这个Intent的ACTION是怎么知道的，大家可以看下自己路径下的如下网页
		 * yourself_sdk_path/docs/reference/android/content/Intent.html
		 * 直接在里面Ctrl+F搜：CROP ，之前小马没仔细看过，其实安卓系统早已经有自带图片裁剪功能, 是直接调本地库的，小马不懂C C++
		 * 这个不做详细了解去了，有轮子就用轮子，不再研究轮子是怎么 制做的了...吼吼
		 */
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CLIPPING);

    }

    private void setPicToSD(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap headPhoto = extras.getParcelable("data");
            saveBitmap(headPhoto);
            Bitmap calculateBitmapAndSave = calculateBitmapAndSave(100, 100);
            saveBitmap(calculateBitmapAndSave);
        }
    }

    public  Bitmap calculateBitmapAndSave(int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth,reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imagePath, options);
    }

    public  boolean saveBitmap(Bitmap bitmap) {
        boolean isSuccess = false;
        File f = new File(imagePath);
        if (!f.exists()) {
            f.mkdirs();
        }
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, out);
            out.flush();
            out.close();
            isSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isSuccess;
    }



    public  int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        //先根据宽度进行缩小
        while (width / inSampleSize > reqWidth) {
            inSampleSize++;
        }
        //然后根据高度进行缩小
        while (height / inSampleSize > reqHeight) {
            inSampleSize++;
        }
        return inSampleSize;
    }

    @Override
    public void selectOrCancleSelectImage() {
        if(mSelectImagePath.size()>0){
            mComplete.setEnabled(true);
            mComplete.setText("完成("+mSelectImagePath.size()+")");
        }else{
            mComplete.setEnabled(false);
        }
    }

}
