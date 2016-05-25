package com.a60circuits.foundbeacons.service;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by zoz on 25/05/2016.
 */
public class NotificationServiceManager {

    private static final String TAG = "NotifServiceManager";

    private PermanentScheduledService notificationService;

    private Activity activity;

    private NotificationServiceManager(){}

    public static NotificationServiceManager getInstance(){
        return NotificationServiceManagerHolder.notificationServiceManager;
    }

    public void startNotificationService(){
        activity.startService(new Intent(activity, NotificationService.class));
    }

    public void killNoficationService() {
        if (isServiceBound())
            notificationService.stopActionEvent();
        doUnbindService();
        activity.stopService(new Intent(activity.getApplicationContext(), NotificationService.class));
        Log.i(TAG, " Notification Service killed");
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            notificationService = ((PermanentScheduledService.LocalBinder)service).getService();
            //chkUseAlarm.setChecked(NotificationService.getUseAlarm());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            notificationService = null;
        }
    };

    public void doBindService() {
        activity.bindService(new Intent(activity, NotificationService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }
    public void doUnbindService() {
        if (notificationService != null) {
            activity.unbindService(serviceConnection);
            notificationService = null;
        }
    }

    private static class NotificationServiceManagerHolder{
        private static NotificationServiceManager notificationServiceManager = new NotificationServiceManager();
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    private boolean isServiceBound() {
        return notificationService != null;
    }
}
