package com.cml.androidimageselector;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView photograph;
    private TextView gallery;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        photograph = (TextView) findViewById(R.id.photograph);
        gallery = (TextView)findViewById(R.id.gallery);

        photograph.setOnClickListener(this);
        gallery.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.photograph:
                camare();
                break;
            case R.id.gallery:
                startActivityForResult(new Intent(this,GalleryActivity.class),1);
                break;
        }
    }

    private void camare() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //判断是否有相机
        if(cameraIntent.resolveActivity(getPackageManager()) != null){
            imagePath = getImagePath();
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(imagePath)));
            startActivityForResult(cameraIntent,3);
        }else{
            Toast.makeText(this, "相机不存在", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == 2){//图库返回
            setResult(RESULT_OK,data);
            finish();
        }

        if(requestCode == 3 && resultCode == RESULT_OK){//相机返回
            Bundle extras = data.getExtras();
            sendBroadcastUpdateDICM();
            data.putExtra("data",imagePath);
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
}
