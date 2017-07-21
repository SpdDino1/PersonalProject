package com.example.vikramkumaresan.v4;

//Helps the Pointer class. Gets giver coordinates

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

public class GetDataFromServer extends AsyncTask<Void,Void,Void> {

    public GetDataFromServer() {
    }

    @Override
    protected Void doInBackground(Void[] params) {

        try {
            URL url = new URL("http://cycleshare.atwebpages.com/getdatafromserver_version4.php");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoInput(true);

            OutputStream outStream=connection.getOutputStream();
            OutputStreamWriter outwrite = new OutputStreamWriter(outStream);
            BufferedWriter buffwrite = new BufferedWriter(outwrite);

            String output = URLEncoder.encode("Name","UTF-8")+"="+URLEncoder.encode(Custom_Adapter.intent.getStringExtra("giver"),"UTF-8");

            buffwrite.write(output);
            buffwrite.flush();
            buffwrite.close();

            InputStream instream = connection.getInputStream();
            InputStreamReader inread = new InputStreamReader(instream);
            BufferedReader buffread = new BufferedReader(inread);

            String[] iteration = buffread.readLine().split(" ");
            String latitude = iteration[0];
            String longitude = iteration[1];

            instream.close();
            inread.close();
            buffread.close();

            Pointer.set_Target(Double.parseDouble(latitude),Double.parseDouble(longitude));
        } catch (MalformedURLException e) {
            Log.d("TAG","Malformed URL");
        } catch (ProtocolException e) {
            Log.d("TAG","Protocol Exception");
        } catch (IOException e) {
            Log.d("TAG","IOException "+e.toString());
        } catch(NumberFormatException e){}
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Pointer.isAsyncRunning=false;
    }
}

