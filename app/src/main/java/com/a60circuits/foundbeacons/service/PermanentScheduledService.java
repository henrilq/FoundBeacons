package com.a60circuits.foundbeacons.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by zoz on 21/05/2016.
 */
public abstract class PermanentScheduledService extends Service{
    public static final String TAG = "NOTIFICATION_SERVICE";
    public static final String PREFS_NAME = "defaultPrefs";

    private static SharedPreferences settings;
    private static boolean useAlarm;

    private final Handler handler = new Handler();
    private final IBinder mBinder = new LocalBinder();

    private int delay;

    public PermanentScheduledService(int delay){
        this.delay = delay;
    }

    public static void setUseAlarm(boolean enabled) {
        if (useAlarm != enabled) {
            useAlarm = enabled;
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("useAlarm", useAlarm);
            editor.commit();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        settings = getSharedPreferences(PREFS_NAME, 0);
        useAlarm = settings.getBoolean("useAlarm", true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startActionEvent();
        return START_STICKY;
    }

    public void startActionEvent() {
        Log.i(TAG, "Start");
        stopActionEvent();
        handler.postDelayed(actionRunnable, delay);

        if (useAlarm)
        {
            // Using an Alarm to restart the Service, which should be reliable in 4.4.1 / 4.4.2.
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getApplicationContext(), PermanentScheduledService.class);
            intent.putExtra("callType", "Alarm");
            PendingIntent scheduledIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2)
                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delay, scheduledIntent);
            else
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delay, scheduledIntent);
        }
    }

    public abstract void doAction();

    private void doActionEvent() {
        doAction();
        startActionEvent();
    }

    public void stopActionEvent() {
        // Remove Handler-based events
        handler.removeCallbacks(actionRunnable);

        // Remove Alarm-based events
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), PermanentScheduledService.class );
        PendingIntent scheduledIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(scheduledIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private Runnable actionRunnable = new Runnable() {
        @Override
        public void run() {
            doActionEvent();
        }
    };

    public class LocalBinder extends Binder {
        public PermanentScheduledService getService() {
            return PermanentScheduledService.this;
        }
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
    }

    public static boolean getUseAlarm() {
        return useAlarm;
    }
}
