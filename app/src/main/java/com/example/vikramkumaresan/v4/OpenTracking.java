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

//Resets tracking back to 0 in live_cycles

public class OpenTracking extends AsyncTask<Void,Void,Void> {

    public OpenTracking() {
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            URL url = new URL("http://cycleshare.atwebpages.com/OpenTracking_version4.php");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);

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
        }catch (Exception e){
        }

        return null;
    }


}
