package com.example.vikramkumaresan.v4;


import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

//Helps with the Job Scheduling. It 'schedules' the Check_for_Taker Service
//It is generally better to schedule services

public class JobSchedulerUtil {
    static JobScheduler jobScheduler;

    @TargetApi(Build.VERSION_CODES.M)       //JobScheduler added in lollipop

    public static void Schedule_Job(Context ctx){
        ComponentName serviceComponent = new ComponentName(ctx, Check_for_Taker.class); //Target service
        JobInfo.Builder builder = new JobInfo.Builder(102, serviceComponent);   //102 = Job Id
        builder.setMinimumLatency(1 * 1000);    //Fires every 1 sec

        jobScheduler = ctx.getSystemService(JobScheduler.class);    //Builds the job
        jobScheduler.schedule(builder.build()); //Attaches to android system's job scheduler api
        //Since it's linked to the android OS, it can be fired even when app is closed
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void Kill_Job(Context ctx){
        try {
            jobScheduler.cancelAll();   //This kills 'ALL' jobs built by this variable.
        }catch (Exception e){}
    }
}
