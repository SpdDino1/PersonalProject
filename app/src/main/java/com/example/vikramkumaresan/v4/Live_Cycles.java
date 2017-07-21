package com.example.vikramkumaresan.v4;

import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

//Shows all available live cycles

public class Live_Cycles extends AppCompatActivity {
    ListView list;
    boolean isAsyncComplete=false;

    public static Live_Cycles ctx;

    String JSON_string;

    int count=0;    //This increments if server provides data AND cycle is trackable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live__cycles);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ctx=this;

        list=(ListView)findViewById(R.id.list);

        Get_Live obj = new Get_Live();
        obj.execute();

        while(!isAsyncComplete){
            Log.d("TAG","Waiting....");
            SystemClock.sleep(1000);
        }

        if(!JSON_string.equals("[")){
            ArrayList<ArrayList<String>> data_to_adapter=new ArrayList<>();

            try {   //Preparing Adapter Data
                JSONArray parent = new JSONArray(JSON_string);

                for(int i =0;i<parent.length();i++){
                    JSONObject object = parent.getJSONObject(i);
                    String isTrackable = object.getString("tracking");
                    if(isTrackable.equals("0")){
                        ArrayList<String> obj_data = new ArrayList<>();
                        obj_data.add(object.getString("name"));
                        obj_data.add(object.getString("cycle"));
                        obj_data.add(object.getString("location"));
                        data_to_adapter.add(obj_data);
                        count++;
                    }
                }
            } catch (JSONException e) {
                Log.d("TAG","JSON Exception");
            }
            //....................................
            if(count!=0){
                ListAdapter adapt = new Custom_Adapter(this,data_to_adapter);
                list.setAdapter(adapt);
                Toast.makeText(this,"Tap a Name to Track",Toast.LENGTH_LONG).show();
            }
            else {  //Server provides data but all cycles are being tracked
                Toast.makeText(this,"No Live Cycles Available",Toast.LENGTH_LONG).show();
            }
        }
        else {  //No server data
            Toast.makeText(this,"No Live Cycles Available",Toast.LENGTH_LONG).show();
        }

    }

    private class Get_Live extends AsyncTask<Void,Void,Void>{
        //Gets JSON data from Live_Cycles table in server
        public Get_Live() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL("http://cycleshare.atwebpages.com/jsonify_version4.php");
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");

                InputStream instream = connection.getInputStream();
                InputStreamReader inread = new InputStreamReader(instream);
                BufferedReader buffread = new BufferedReader(inread);

                String data = buffread.readLine();

                JSON_string="[";
                String[] parts = data.split("\\+");

                for(int i=0;i<parts.length;i++){
                    if(i==(parts.length-1)){
                        JSON_string=JSON_string+parts[i]+"]";
                    }
                    else {
                        JSON_string=JSON_string+parts[i]+",";
                    }
                }
                isAsyncComplete=true;

            } catch (MalformedURLException e) {
                Log.d("TAG","Malformed URL");
            } catch (ProtocolException e) {
                Log.d("TAG","Protocol Exception");
            } catch (IOException e) {
                Log.d("TAG","IOException "+e.toString());
            }
            catch (NullPointerException e){
                isAsyncComplete=true;
            }
            return null;
        }
    }
}

