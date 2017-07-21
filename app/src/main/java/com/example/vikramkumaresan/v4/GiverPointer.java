package com.example.vikramkumaresan.v4;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

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

//The giver's pointer

public class GiverPointer extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,com.google.android.gms.location.LocationListener {
    SensorEventListener listen;
    Sensor accelerometer;
    Sensor magnetic_field;

    GoogleApiClient client;
    LocationRequest request;

    public static boolean takerTimeUp=false;

    public static boolean isAsyncRunning = false;
    public static boolean isAsyncRunning_Posting = false;

    TextView dist;

    boolean already_started = false;
    boolean ispointerchanged=false;

    Intent parallelizer;

    private SensorManager manager;
    float[] gravity;        //Accelerometer Readings
    float[] geomagnetic;    //Magnetometer Readings

    ImageView img;

    Button transactionComplete;

    GetDataFromServer obj = new GetDataFromServer();

    Double[] myaddress = {null, null};
    static Double[] target_address = {null, null};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giver_pointer);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        LocationManager manage = (LocationManager)getApplicationContext().getSystemService(getApplicationContext().LOCATION_SERVICE);
        if(!manage.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Intent settings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS); //Take user to settings page
            startActivity(settings);
            Toast.makeText(getApplicationContext(),"Please Enable Location",Toast.LENGTH_LONG).show();
        }

        transactionComplete=(Button)findViewById(R.id.transaction_complete);

        Toast.makeText(this,"Calibrating....Please Wait...",Toast.LENGTH_LONG).show();

        //GPS Mech Initialization
        request = new LocationRequest();
        request.setInterval(5000);
        request.setFastestInterval(2000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        client = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        client.connect();
        //...........................

        parallelizer=new Intent(this,PhoneParallelizer.class);

        img=(ImageView)findViewById(R.id.img);

        dist = (TextView)findViewById(R.id.distance);

        //.........................Rotation Mech
        manager=(SensorManager)getSystemService(SENSOR_SERVICE);

        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic_field=manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        listen = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
                    gravity=event.values;//Taking Accel Readings
                }
                else if(event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD){
                    geomagnetic=event.values;   //Taking Magneto Readings
                }

                Log.d("screwed","isAsyncRunning = "+isAsyncRunning);
                if(!isAsyncRunning){
                    try{
                        GetDataFromServer obj = new GetDataFromServer();
                        isAsyncRunning=true;
                        obj.execute();
                    }catch (Exception e){
                        Log.d("screwed","GetDataError = "+e.toString());
                    }
                }

                if(gravity!=null && geomagnetic!=null && myaddress[0]!=null && target_address[0]!=null &&!takerTimeUp){
                    Log.d("screwed","Condition Met!");
                    float[] R= new float[9];
                    float[] I = new float[9];

                    if(manager.getRotationMatrix(R,I,gravity,geomagnetic)){ //Getting Rot Matrix from the accel, magneto readings
                        float[] values = new float[3];
                        manager.getOrientation(R,values);   //after getting rot. getting Orientation
                        double pitch=Math.toDegrees(values[1]);
                        if((pitch<60)&&(pitch>-60)){    //To check if phone is not tilted too much (Messes the compass)
                            if(already_started){
                                already_started=false;
                                PhoneParallelizer.obj.finish();
                            }
                            Double azimuth = (values[0]*57.3065);//getOrientation() gives 3 values. [0] gives the angle (aka azimuth angle)

                            Double[] res_lat = latitiude_matcher(myaddress, target_address, azimuth);
                            Double lat_dist = res_lat[0]*111;
                            Double lat_angle=res_lat[1];

                            Double[] res_long = longitude_matcher(myaddress,target_address,azimuth);;
                            Double long_dist= res_long[0]*111;
                            Double long_angle=res_long[1];

                            double dist=showDistance(Math.sqrt(Math.pow(lat_dist,2)+Math.pow(long_dist,2))); //To display distance
                            if(dist>25.0){
                                if(!ispointerchanged){
                                    double resultant_angle=vector_deresolver(coordinate_convertor(lat_angle),coordinate_convertor(long_angle),lat_dist,long_dist);
                                    img.setRotation((float)resultant_angle);
                                }
                                else {
                                    double resultant_angle=vector_deresolver(coordinate_convertor(lat_angle),coordinate_convertor(long_angle),lat_dist,long_dist);
                                    img.setImageResource(com.example.vikramkumaresan.v4.R.mipmap.pointer);
                                    img.setRotation((float)resultant_angle);
                                }
                                transactionComplete.setEnabled(false);
                            }
                            else {
                                img.setRotation(0);
                                img.setImageResource(com.example.vikramkumaresan.v4.R.mipmap.too_close);
                                ispointerchanged=true;
                                transactionComplete.setEnabled(true);
                            }

                        }
                        else {
                            if(!already_started){
                                startActivity(parallelizer);
                                already_started=true;
                            }
                        }

                    }
                }
                else if(takerTimeUp){
                    AddTakerToDenied obj = new AddTakerToDenied();
                    obj.execute();
                    manager.unregisterListener(listen);
                    LocationServices.FusedLocationApi.removeLocationUpdates(client,GiverPointer.this);
                    client.disconnect();
                    Intent go = new Intent(getApplicationContext(),Live.class);
                    startActivity(go);

                    takerTimeUp=false;

                    ResetTakerPosition reset = new ResetTakerPosition();
                    reset.execute();

                    Toast.makeText(getApplicationContext(),"Rebroadcasting Cycle",Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        manager.registerListener(listen,accelerometer,1000000);
        manager.registerListener(listen,magnetic_field,1000000);
        //......................End of rotation mech
    }

    private Double[] latitiude_matcher(Double[] myloc, Double[] targetloc, Double azimuth){
        //Assumes longitude is same
        //Points users to get to the same latitude

        Double lat=0.00;
        Double lat_dist=0.00;
        Double[] lat_details={0.00,0.00};
        if((myloc[0]>targetloc[0])){  //I'm Above Target
            if (azimuth>=0){ //Facing somewhere right
                lat=180-azimuth;
            }
            else{   //Facing somewhere left
                lat=-180-azimuth;
            }
            lat_dist=myloc[0]-targetloc[0];
        }
        else{  //I'm Below Target
            if (azimuth>=0){ //Facing somewhere right
                lat=-azimuth;
            }
            else{   //Facing somewhere left
                lat=-azimuth;
            }
            lat_dist=targetloc[0]-myloc[0];
        }
        lat_details[0]=lat_dist;
        lat_details[1]=lat;
        return lat_details;
    }

    private Double[] longitude_matcher(Double[] myloc,Double[] targetloc,Double azimuth){
        //Assumes lattitude is same
        //Points users to get to the same longitude
        Double long_angle=0.00;
        Double long_dist=0.00;
        Double[] long_details={0.00,0.00};

        if(myloc[1]>targetloc[1]){ //I'm on the right
            if((azimuth>=0)&&(azimuth<=90)){
                long_angle=-90+(-1*azimuth);
            }
            else if((azimuth>90)&&(azimuth<=180)){
                long_angle=180-(azimuth-90);
            }
            else if((azimuth<0)&&(azimuth>=-90)){
                long_angle=-90-azimuth;
            }
            else if((azimuth<-90)&&(azimuth>=-180)){
                long_angle=(-1*azimuth)-90;
            }
            long_dist=myloc[1]-targetloc[1];
        }
        else {  //I'm on the left
            if((azimuth>=0)&&(azimuth<=90)){
                long_angle=90-azimuth;
            }
            else if((azimuth>90)&&(azimuth<=180)){
                long_angle=90-azimuth;
            }
            else if((azimuth<0)&&(azimuth>=-90)){
                long_angle=90+(-1*azimuth);
            }
            else if((azimuth<-90)&&(azimuth>=-180)){
                long_angle= -1*(180+(azimuth+90));
            }
            long_dist=targetloc[1]-myloc[1];
        }
        long_details[0]=long_dist;
        long_details[1]=long_angle;

        return long_details;
    }

    private double coordinate_convertor(double angle){
        //Converts the Azimuth angle from 0 to 180 and 0 to -180 system to 360 degrees clockwise system
        if(angle>0){
            return angle;
        }
        else {
            return 360+angle;
        }
    }

    private double vector_deresolver(double lat_angle,double long_angle,double lat_dist,double long_dist){
        //Combines both lat and long vectors to give the single pointer's angle
        double res_angle=Math.toDegrees(Math.atan(lat_dist/long_dist));
        if(close_to_90(lat_angle-long_angle)){  //Both Within 360

            if(long_angle<lat_angle){   //Latitude vector leads long vector
                return (long_angle+res_angle);
            }
            else {  //Long vector leads lat vectos
                return (long_angle-res_angle);
            }
        }
        else {  //One in first quad and the other in 4th quad       i.e one has started a new rotation

            if(long_angle>lat_angle){   //Long vector is in quad 4 and lat vector quad 1
                return deresolver_filter(long_angle+res_angle);
            }
            else {  //Lat vector in quad 4 and long vector in quad 1
                return deresolver_filter(long_angle-res_angle);
            }
        }

    }

    private boolean close_to_90(double diff){
        if(diff<0){
            diff=diff*(-1);
        }

        if((diff>87)&&(diff<93)){
            return true;
        }
        else
            return false;

    }

    private double deresolver_filter(double res_angle){
        //Takes care of converting negative angles and angles >360 to angles between 0 and 360
        if(res_angle>360){
            return (res_angle-360);
        }
        else if(res_angle<0){
            return (360+res_angle);
        }
        else {
            return res_angle;
        }
    }

    private double showDistance(double distance){ //Checks whether km or m more appropriate
        double rounded_dist = Math.round(distance*1000);    //In metres
        if (rounded_dist>1000){
            dist.setText(""+(rounded_dist/1000)+"km");
        }
        else {
            dist.setText(""+rounded_dist+"m");
        }
        return rounded_dist;
    }

    public static void set_Target(double lat, double Long){
        target_address[0]=lat;
        target_address[1]=Long;
    }

    public void cleanDatabase(View view){
        MainActivity.cleanDatabase();
        manager.unregisterListener(listen);
        try{
            LocationServices.FusedLocationApi.removeLocationUpdates(client,GiverPointer.this);
        }catch (Exception e){}
        client.disconnect();
        finish();
    }

    @Override
    public void onConnected(Bundle bundle) {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(client, request, (com.google.android.gms.location.LocationListener) this);
        Log.d("TAG","FusedLocation Connection Started");

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        myaddress[0] = location.getLatitude();
        myaddress[1] = location.getLongitude();
        Log.d("screwed","GPS Active");
        if(!isAsyncRunning_Posting){
            try{
                PostDataOnServer obj = new PostDataOnServer();
                isAsyncRunning_Posting=true;
                obj.execute(""+myaddress[0],""+myaddress[1]);
            }catch (Exception e){
                Log.d("TAG","Error = "+e.toString());
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("TAG","FusedLocation Connection Failed");

    }

    private class PostDataOnServer extends AsyncTask<String,Void,Void>{

        public PostDataOnServer() {
            Log.d("micro","Posting on Server");
        }

        @Override
        protected Void doInBackground(String... params) {
            Log.d("screwed","Posting Data on Server");
            try {
                URL url = new URL("http://cycleshare.atwebpages.com/PostDataOnServerForGiver_version4.php");
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                OutputStream outStream=connection.getOutputStream();
                OutputStreamWriter outwrite = new OutputStreamWriter(outStream);
                BufferedWriter buffwrite = new BufferedWriter(outwrite);

                String output = URLEncoder.encode("Location","UTF-8")+"="+URLEncoder.encode(params[0]+" "+params[1],"UTF-8")+"&"+URLEncoder.encode("Name","UTF-8")+"="+
                        URLEncoder.encode(MainActivity.publish_name,"UTF-8");

                buffwrite.write(output);
                buffwrite.flush();
                buffwrite.close();

                InputStream instream = connection.getInputStream();
                isAsyncRunning_Posting=false;

            } catch (MalformedURLException e) {
                Log.d("TAG","Malformed URL");
            } catch (ProtocolException e) {
                Log.d("TAG","Protocol Exception");
            } catch (IOException e) {
                Log.d("TAG","IOException "+e.toString());
            }
            return null;
        }

    }

    private class GetDataFromServer extends AsyncTask<Void,Void,Void> {

        public GetDataFromServer() {
            Log.d("micro","Getting from server");
        }

        @Override
        protected Void doInBackground(Void[] params) {
            Log.d("screwed","Getting Data from Server");
            try {
                URL url = new URL("http://cycleshare.atwebpages.com/GetDataFromServerForGiver_version4.php");
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoInput(true);

                OutputStream outStream=connection.getOutputStream();
                OutputStreamWriter outwrite = new OutputStreamWriter(outStream);
                BufferedWriter buffwrite = new BufferedWriter(outwrite);

                String output = URLEncoder.encode("Name","UTF-8")+"="+URLEncoder.encode(MainActivity.publish_name,"UTF-8");

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
                isAsyncRunning=false;

                set_Target(Double.parseDouble(latitude),Double.parseDouble(longitude));
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

    private class AddTakerToDenied extends AsyncTask<Void,Void,Void>{
        public AddTakerToDenied() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL("http://cycleshare.atwebpages.com/AddToDeny_version4.php");
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoInput(true);

                OutputStream outStream=connection.getOutputStream();
                OutputStreamWriter outwrite = new OutputStreamWriter(outStream);
                BufferedWriter buffwrite = new BufferedWriter(outwrite);

                String output = URLEncoder.encode("Name","UTF-8")+"="+URLEncoder.encode(MainActivity.taker_name,"UTF-8")+"&"+URLEncoder.encode("GiveName","UTF-8")+"="+URLEncoder.encode(MainActivity.publish_name,"UTF-8");

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
    }

    public void blockTaker(View view){
        takerTimeUp=true;
        OpenTracking obj = new OpenTracking();
        obj.execute();
    }

    @Override
    public void onBackPressed() {
        MainActivity.cleanDatabase();
        manager.unregisterListener(listen);
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }catch (Exception e){}
        client.disconnect();
        MainActivity.isCurrentlyBroadcasting=false;
        finish();
    }

    private class ResetTakerPosition extends AsyncTask<Void,Void,Void>{
        public ResetTakerPosition() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL("http://cycleshare.atwebpages.com/ResetTakerPosition_version4.php");
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoInput(true);

                OutputStream outStream=connection.getOutputStream();
                OutputStreamWriter outwrite = new OutputStreamWriter(outStream);
                BufferedWriter buffwrite = new BufferedWriter(outwrite);

                String output = URLEncoder.encode("Name","UTF-8")+"="+URLEncoder.encode(MainActivity.publish_name,"UTF-8");

                buffwrite.write(output);
                buffwrite.flush();
                buffwrite.close();

                InputStream instream = connection.getInputStream();

                JobSchedulerUtil.Schedule_Job(getApplicationContext());
                Log.d("TAG","Job Rescheduled");

            } catch (MalformedURLException e) {
                Log.d("TAG","Malformed URL @ Reset");
            } catch (ProtocolException e) {
                Log.d("TAG","Protocol Exception @ Reset");
            } catch (IOException e) {
                Log.d("TAG","IOException "+e.toString());
            }
            return null;
        }
    }
}
