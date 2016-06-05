package com.a60circuits.foundbeacons.service;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;



/**
 * Created by zoz on 25/05/2016.
 */
public class NotificationServiceManager {

    private static final String TAG = "NotifServiceManager";

    private static final int idJob = 1274;

    private PermanentScheduledService notificationService;

    private Activity activity;

    private NotificationServiceManager(){}

    public static NotificationServiceManager getInstance(){
        return NotificationServiceManagerHolder.notificationServiceManager;
    }

    public void startNotificationService(){
        ComponentName serviceName = new ComponentName(activity.getApplicationContext(), NotificationService.class);
        JobInfo task = new JobInfo.Builder(idJob, serviceName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setRequiresDeviceIdle(true)
                .setPeriodic(NotificationService.PERIOD)
                .setPersisted(true)
                .build();

        JobScheduler jobScheduler = (JobScheduler) activity.getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(task);
    }

    public void killNoficationService() {
        JobScheduler jobScheduler = (JobScheduler) activity.getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(idJob);
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    private static class NotificationServiceManagerHolder{
        private static NotificationServiceManager notificationServiceManager = new NotificationServiceManager();
    }

}
