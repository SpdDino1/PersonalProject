package com.example.vikramkumaresan.v4;

import android.os.AsyncTask;
import android.util.Log;

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


public class Publish_Cycle_on_Server extends AsyncTask<String,Void,Void> {

    public Publish_Cycle_on_Server() {
    }

    @Override
    protected Void doInBackground(String... params) {
        String name = params[0];
        String cycle = params[1];
        String location = params[2];

        try {
            URL url = new URL("http://cycleshare.atwebpages.com/add_live_version4.php");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            OutputStream outStream=connection.getOutputStream();
            OutputStreamWriter outwrite = new OutputStreamWriter(outStream);
            BufferedWriter buffwrite = new BufferedWriter(outwrite);

            String output = URLEncoder.encode("Name","UTF-8")+"="+URLEncoder.encode(name,"UTF-8")+"&"+URLEncoder.encode("Cycle","UTF-8")+"="+
                    URLEncoder.encode(cycle,"UTF-8")+"&"+URLEncoder.encode("Location","UTF-8")+"="+URLEncoder.encode(location,"UTF-8");

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
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        //Log.d("TAG","Posted");
    }
}
