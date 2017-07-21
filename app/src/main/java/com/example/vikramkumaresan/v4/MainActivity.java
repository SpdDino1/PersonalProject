package com.example.vikramkumaresan.v4;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

//PROJECT REQUIRES 'live_cycles (name,cycle,location) in DB before running!!
//NAMES CANNOT CONTAIN SPACES

public class MainActivity extends AppCompatActivity {
    public static Intent go;

    public static String publish_name;  //If clicked publish

    public static String taker_name;    //If clicked view live

    public static boolean isCurrentlyBroadcasting = false;

    public static ArrayList<Character>acceptedCharacters;

    char[] acceptedChars = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, 102);       //Location Permission
        }
        LocationManager manage = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if(!manage.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Intent settings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS); //Take user to settings page
            startActivity(settings);
            Toast.makeText(this,"Please Enable Location",Toast.LENGTH_LONG).show();
        }

        acceptedCharacters = new ArrayList<>();

        for(char i:acceptedChars){
            acceptedCharacters.add(i);
        }

    }

    public void get_cycle_info(View view){
        if(!isCurrentlyBroadcasting){
            go = new Intent(this,Get_Cycle_Info.class);
            startActivityForResult(go,102);
        }
        else {
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
            go = new Intent(this,GiverPointer.class);
            startActivity(go);
        }
    }

    public void show_live(View view){
        if(!isCurrentlyBroadcasting){
            go = new Intent(this,Get_Taker_Name.class);
            startActivity(go);
        }
        else {
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
            go = new Intent(this,GiverPointer.class);
            startActivity(go);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==102 && requestCode==102){
            //CHECK NAME FOR SPACES AND SPECIAL CHARACTERS!
            publish_name=go.getStringExtra("name");
            int flag = 0;

            for(int i=0;i<publish_name.length();i++){
                if(!acceptedCharacters.contains(publish_name.charAt(i))){
                    Log.d("TAG","ILLEGAL CHAR = "+publish_name.substring(i,i+1));
                    Toast.makeText(this,"Only English Alphabets Allowed",Toast.LENGTH_SHORT).show();
                    flag=1;
                    break;
                }
            }
            if(publish_name.equals("")){
                Toast.makeText(this,"Please Enter a Name",Toast.LENGTH_LONG).show();
                flag=1;
            }
            if(flag==0) {
                Create_Exchange_Table_After_Publish obj2 = new Create_Exchange_Table_After_Publish(this);
                obj2.execute();

                Publish_Cycle_on_Server obj = new Publish_Cycle_on_Server();
                obj.execute(go.getStringExtra("name"), go.getStringExtra("cycle"), go.getStringExtra("location"));
                try {
                    Loading.ctx.finish();
                } catch (Exception e) {
                }

                Intent temp = new Intent(this, Live.class);
                Log.d("TAG", "Lived");
                startActivity(temp);
            }
        }
    }

    public static void cleanDatabase(){
        CleanDatabase obj = new CleanDatabase();
        obj.execute();
    }

    private static class CleanDatabase extends AsyncTask<Void,Void,Void>{
        public CleanDatabase() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL("http://cycleshare.atwebpages.com/CleanDatabase_version4.php");
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                OutputStream outStream=connection.getOutputStream();
                OutputStreamWriter outwrite = new OutputStreamWriter(outStream);
                BufferedWriter buffwrite = new BufferedWriter(outwrite);

                String output = URLEncoder.encode("Name","UTF-8")+"="+URLEncoder.encode(MainActivity.publish_name,"UTF-8");

                buffwrite.write(output);
                buffwrite.flush();
                buffwrite.close();

                InputStream instream = connection.getInputStream();

            } catch (MalformedURLException e) {
                Log.d("TAG","Malformed URL");
            } catch (ProtocolException e) {
                Log.d("TAG","Protocol Exception");
            } catch (IOException e) {
                Log.d("TAG","IOException "+e.toString());
            } catch(NumberFormatException e){}
            return null;
        }

    }

}

