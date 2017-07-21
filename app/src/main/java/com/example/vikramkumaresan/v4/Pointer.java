package com.example.vikramkumaresan.v4;

import android.Manifest;
import android.content.Context;
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
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

//The actual pointing goodness!
//Pointer for the taker's side.

public class Pointer extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,com.google.android.gms.location.LocationListener {
    static SensorEventListener listen;
    Sensor accelerometer;
    Sensor magnetic_field;

    static Context ctx;
    public static Pointer foreignTerminate;

    static GoogleApiClient client;
    LocationRequest request;

    public static boolean isTerminated =false; //Will be set true after 5min by PostDataonServer

    public static boolean isAsyncRunning = false;
    public static boolean isAsyncRunning_Posting = false;

    TextView dist;

    LocationManager manage;

    boolean already_started = false;
    boolean ispointerchanged=false;

    Intent parallelizer;

    static private SensorManager manager;
    float[] gravity;        //Accelerometer Readings
    float[] geomagnetic;    //Magnetometer Readings

    ImageView img;

    GetDataFromServer obj = new GetDataFromServer();

    Double[] myaddress = {null, null};
    static Double[] target_address = {null, null};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pointer);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ctx=this;
        foreignTerminate=this;

        manage = (LocationManager)getApplicationContext().getSystemService(getApplicationContext().LOCATION_SERVICE);
        if(!manage.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Intent settings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS); //Take user to settings page
            startActivity(settings);
            Toast.makeText(getApplicationContext(),"Please Enable Location",Toast.LENGTH_LONG).show();
        }

        Toast.makeText(this,"Calibrating....Please Wait...",Toast.LENGTH_LONG).show();

        //GPS Mech
        request = new LocationRequest();
        request.setInterval(5000);
        request.setFastestInterval(2000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        client = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        client.connect();
        //......................................End of GPS Mech

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

                if(!isAsyncRunning){
                    try{
                        GetDataFromServer obj = new GetDataFromServer();
                        isAsyncRunning=true;
                        obj.execute();
                    }catch (Exception e){
                        Log.d("TAG","Error = "+e.toString());
                    }
                }

                if(gravity!=null && geomagnetic!=null && myaddress[0]!=null &&target_address[0]!=null){
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
                            }
                            else {
                                img.setRotation(0);
                                img.setImageResource(com.example.vikramkumaresan.v4.R.mipmap.too_close);
                                ispointerchanged=true;
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

    @Override
    public void onBackPressed() {
        LocationServices.FusedLocationApi.removeLocationUpdates(client, this);  //Stops Updates
        client.disconnect();    //Disconnects from service
        manager.unregisterListener(listen);
        OpenTracking obj = new OpenTracking();
        obj.execute();
        finish();
    }

    public static void foreignTerminate(){
        LocationServices.FusedLocationApi.removeLocationUpdates(client, (com.google.android.gms.location.LocationListener) ctx);  //Stops Updates
        client.disconnect();    //Disconnects from service
        manager.unregisterListener(listen);
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
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("TAG","FusedLocation Connection Failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        myaddress[0] = location.getLatitude();
        myaddress[1] = location.getLongitude();
        if(!isAsyncRunning_Posting){
            try{
                PostDataOnServer obj = new PostDataOnServer(this);
                isAsyncRunning_Posting=true;
                obj.execute(""+myaddress[0],""+myaddress[1]);
                Log.d("TAG","Latitude = "+location.getLatitude());
            }catch (Exception e){
                Log.d("TAG","Error = "+e.toString());
            }
        }
    }
}
