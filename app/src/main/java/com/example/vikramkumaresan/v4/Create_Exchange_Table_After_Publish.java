package com.example.vikramkumaresan.v4;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import static android.content.Context.LOCATION_SERVICE;

public class Create_Exchange_Table_After_Publish extends AsyncTask<Void, Void, Void> implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,com.google.android.gms.location.LocationListener {
    private String publish_name = MainActivity.publish_name;
    private double giver_lat=0.0;
    private double giver_long = 0.0;

    GoogleApiClient client;
    LocationRequest request;

    private String giver_location="";

    private Context ctx; //Main Activity's
    private LocationManager manage;

    public Create_Exchange_Table_After_Publish(Context ctx) {
        this.ctx = ctx;
        manage = (LocationManager) ctx.getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onPreExecute() {
        request = new LocationRequest();
        request.setInterval(5000);
        request.setFastestInterval(2000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        client = new GoogleApiClient.Builder(ctx).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        client.connect();

    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            URL url = new URL("http://cycleshare.atwebpages.com/create_exchange_table_version4.php");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            OutputStream outStream=connection.getOutputStream();
            OutputStreamWriter outwrite = new OutputStreamWriter(outStream);
            BufferedWriter buffwrite = new BufferedWriter(outwrite);

            while(giver_lat==0.0){  //Waits for gps
                SystemClock.sleep(5000);
                Log.d("TAG","Sleeping...");
            }
            giver_location=""+giver_lat+" "+giver_long;

            String output = URLEncoder.encode("Name","UTF-8")+"="+URLEncoder.encode(publish_name,"UTF-8")+"&"+URLEncoder.encode("Giver_position","UTF-8")+"="+
                            URLEncoder.encode(giver_location,"UTF-8");

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
        //client.disconnect();  //Disengage GPS
        //LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        JobSchedulerUtil.Schedule_Job(ctx);
    }

    @Override
    public void onConnected(Bundle bundle) {
        PendingResult<com.google.android.gms.common.api.Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(client, request, (com.google.android.gms.location.LocationListener) this);
        Log.d("TAG","FusedLocation Connection Started");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("TAG","FusedLocation Connection Failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        giver_lat=location.getLatitude();
        giver_long=location.getLongitude();
        Log.d("TAG","GPS Activated");
    }

}
