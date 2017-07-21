package com.example.vikramkumaresan.v4;

//Helps update the gps coordinates from Pointer class on server

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
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

public class PostDataOnServer extends AsyncTask<Object, Object, String> {
    Context ctx;
    public PostDataOnServer(Context ctx) {
        this.ctx=ctx;
    }

    @Override
    protected String doInBackground(Object... params) {
        try {
            URL url = new URL("http://cycleshare.atwebpages.com/postdataonserver_version4.php");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            OutputStream outStream=connection.getOutputStream();
            OutputStreamWriter outwrite = new OutputStreamWriter(outStream);
            BufferedWriter buffwrite = new BufferedWriter(outwrite);

            String output = URLEncoder.encode("Location","UTF-8")+"="+URLEncoder.encode(params[0]+" "+params[1],"UTF-8")+"&"+URLEncoder.encode("Name","UTF-8")+"="+
                    URLEncoder.encode(Custom_Adapter.intent.getStringExtra("giver"),"UTF-8")+"&"+URLEncoder.encode("Taker","UTF-8")+"="+URLEncoder.encode(MainActivity.taker_name,"UTF-8");

            buffwrite.write(output);
            buffwrite.flush();
            buffwrite.close();

            InputStream instream = connection.getInputStream();
            InputStreamReader inread = new InputStreamReader(instream);
            BufferedReader buffread = new BufferedReader(inread);   //Either Done or Denied

            String result = buffread.readLine();
            return result;

        } catch (MalformedURLException e) {
            Log.d("TAG","Malformed URL");
        } catch (ProtocolException e) {
            Log.d("TAG","Protocol Exception");
        } catch (IOException e) {
            Log.d("TAG","IOException "+e.toString());
        } catch (NullPointerException e){

        }
        return "";
    }

    @Override
    protected void onPostExecute(String params) {
        Pointer.isAsyncRunning_Posting=false;
        if(params.equals("Denied")){
            Toast.makeText(ctx,"Your time has expired",Toast.LENGTH_LONG).show();
            Pointer.foreignTerminate();
            Pointer.foreignTerminate.finish();
        }
    }
}
