package com.cml.androidimageselector;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cml.imageselector.ImageSelectorActivity;

import static com.cml.imageselector.ImageSelectorActivity.RESULT_DATA;

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
                Intent intent = new Intent(this, ImageSelectorActivity.class);
                intent.putExtra(ImageSelectorActivity.MODE,ImageSelectorActivity.SEPARATE_MODE);
                startActivityForResult(intent,100);
                break;
            case R.id.gallery:
                break;
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
            data.putExtra("data",imagePath);
            setResult(RESULT_OK,data);
            finish();
        }

        if(resultCode == RESULT_OK && requestCode == 100){
            String stringExtra = data.getStringExtra(RESULT_DATA);
            Toast.makeText(this, stringExtra, Toast.LENGTH_SHORT).show();
        }

    }


}
