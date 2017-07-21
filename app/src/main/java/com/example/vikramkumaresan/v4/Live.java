package com.example.vikramkumaresan.v4;

import android.content.pm.ActivityInfo;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class Live extends AppCompatActivity {
    public static Live ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Log.d("dead","Hi");


        ctx=this;
        ImageView img = (ImageView)findViewById(R.id.img);
        Glide.with(this).load(R.mipmap.live).into(img);
        Toast.makeText(this,"You will be notified if tracked...",Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        MainActivity.cleanDatabase();
        MainActivity.isCurrentlyBroadcasting=false;
        try {
            JobSchedulerUtil.Kill_Job(getApplicationContext());
        }catch (Exception e){}
        finish();
    }
}
