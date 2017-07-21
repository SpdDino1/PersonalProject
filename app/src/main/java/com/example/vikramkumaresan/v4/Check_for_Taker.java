package com.example.vikramkumaresan.v4;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
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

//Checks if the taker location has been updated on the exchange table
//Is scheduled by the JobSchedulerUtil class

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)    //Job service added on Lollipop
public class Check_for_Taker extends JobService {
    Context ctx;
    double taker_lat;
    public static String taker;

    boolean giverLeft=false;    //On back key presses at Live.class = Giver gave up and left

    boolean isAsyncOver=false;

    @Override
    public boolean onStartJob(JobParameters params) {
        ctx=this;

        check_online_taker obj = new check_online_taker();
        obj.execute();

        while(!isAsyncOver){
            Log.d("TAG","Waiting for Async @ CheckForTaker");
            SystemClock.sleep(5000);
        }

        if(taker_lat!=0.0){
            MainActivity.isCurrentlyBroadcasting=true;
            //Notification Mech....
            Intent go = new Intent(this,GiverPointer.class);    //Goes to giverpointer.class
            PendingIntent pIntent = PendingIntent.getActivity(ctx,102,go,0);   //Context,Id,intent,Flags (If needed)
            Notification n = new Notification.Builder(ctx).setContentTitle("'"+taker+"' is Tracking Your Cycle").setAutoCancel(true).setContentIntent(pIntent).setSmallIcon(R.mipmap.big_cycle).build(); //This carries all your UI details
            n.sound = Uri.parse("android.resource://"+ ctx.getPackageName() + "/" + R.raw.notification);    //Could be added to previouos line too
            NotificationManager manage = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);   //Notifications are managed by Android OS. So get the service
            manage.notify(102,n); //Execute the notification. 0 is the id
            //End of Notification mech.........

            Log.d("TAG","Someone Took the Cycle!!!");
            JobSchedulerUtil.Kill_Job(this);
            try{
                Live.ctx.finish();
            }catch (Exception e){}
            try{
                Loading.ctx.finish();
            }catch (Exception e){}

        }
        else{
            if(!giverLeft){
                Log.d("TAG","Rescheduled");
                obj.cancel(true);   //Clear the AsyncTask
                JobSchedulerUtil.Schedule_Job(this);  //Reschedule the job by calling the utility class function
            }
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    class check_online_taker extends AsyncTask<Void,Void,Void>{
        //Checks if the taker location has been updated on the exchange table online
        public check_online_taker() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL("http://cycleshare.atwebpages.com/check_taker_pos_version4.php");
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
                InputStreamReader inread = new InputStreamReader(instream);
                BufferedReader buffread = new BufferedReader(inread);

                String input = buffread.readLine();

                taker_lat = Double.parseDouble(input.split(" ")[0]);
                taker=input.split(" ")[2];

                MainActivity.taker_name=taker;

                Log.d("TAG","taker_lat = "+taker_lat);
                isAsyncOver=true;

            } catch (MalformedURLException e) {
                Log.d("TAG","Malformed URL");
            } catch (ProtocolException e) {
                Log.d("TAG","Protocol Exception");
            } catch (IOException e) {
                Log.d("TAG","IOException "+e.toString());
            }catch (Exception e){
                Log.d("TAG","Job Kill Sent");
                isAsyncOver=true;
                JobSchedulerUtil.Kill_Job(ctx);
                giverLeft=true;
            }

            return null;
        }
    }
}
