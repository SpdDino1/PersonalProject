package com.example.vikramkumaresan.v4;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class Loading extends AppCompatActivity {
    public static Loading ctx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        ctx=this;
        ImageView img = (ImageView)findViewById(R.id.img);
        Glide.with(this).load(R.mipmap.loading).into(img);
    }
}
