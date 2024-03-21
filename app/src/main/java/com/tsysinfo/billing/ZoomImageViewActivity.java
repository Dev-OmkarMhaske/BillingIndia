package com.tsysinfo.billing;

import android.content.Intent;
 
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

public class ZoomImageViewActivity extends AppCompatActivity {

    String path="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_image_view);
        Intent intent=getIntent();
        path=intent.getStringExtra("img");
        ImageView zoomableImageView= (ImageView) findViewById(R.id.iamg);
        Picasso.get().load(new File(path)).fit().placeholder(R.drawable.noimage).into(zoomableImageView);

    }
}
