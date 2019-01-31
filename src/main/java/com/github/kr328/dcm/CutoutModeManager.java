package com.github.kr328.dcm;

import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.WindowManager;

import java.util.List;

public class CutoutModeManager extends TaskStackListener {
    CutoutModeManager() {
        this.context = ActivityThread.currentActivityThread().getSystemContext();
        this.uiContext = ActivityThread.currentActivityThread().getSystemUiContext();
        this.notificationManager = uiContext.getSystemService(NotificationManager.class);
        this.activityManager = (IActivityManager) ServiceManager.getService(Context.ACTIVITY_SERVICE);
        this.storage = new ConfigStorage();

        createNotificationChannel();
        registerListeners();
    }

    int notePackageCutout(String packageName, int defaultValue) {
        return storage.get(packageName ,defaultValue);
    }

    @Override
    public void onTaskStackChanged() throws RemoteException {
        long token = Binder.clearCallingIdentity();

        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getTasks(1);

        if ( tasks.isEmpty() ) {
            notificationManager.cancel(Global.NOTIFICATION_ID);
            return;
        }

        String packageName = tasks.get(0).topActivity.getPackageName();

        notificationManager.notify(Global.NOTIFICATION_ID ,new Notification.Builder(uiContext,Global.NOTIFICATION_CHANNEL_ID)
                .setContentTitle(loadApplicationLabel(packageName))
                .setSmallIcon(android.R.drawable.star_on)
                .setContentText(Utils.cutoutModeIntToString(storage.get(packageName
                        ,WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT)))
                .setSubText("CutoutMode")
                .setOngoing(true)
                .setContentIntent(PendingIntent.getBroadcast(context ,Global.BROADCAST_ID
                        ,new Intent(Global.INTENT_ACTION_CHANGE).putExtra("package" ,packageName)
                        ,PendingIntent.FLAG_UPDATE_CURRENT))
                .build());

        Binder.restoreCallingIdentity(token);
    }

    private void registerListeners() {
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ( !Global.INTENT_ACTION_CHANGE.equals(intent.getAction()) )
                    return;

                String packageName = intent.getStringExtra("package");
                showConfigDialog(packageName);
            }
        } ,new IntentFilter(Global.INTENT_ACTION_CHANGE));
        activityManager.registerTaskStackListener(this);
    }

    private void showConfigDialog(String packageName) {
        Log.i(Global.TAG ,"Dialog for " + packageName);

        AlertDialog dialog = new AlertDialog.Builder(uiContext)
                .setTitle(loadApplicationLabel(packageName))
                .setSingleChoiceItems(
                new CharSequence[]{"Default" ,"Never" ,"ShortEdges"} ,
                cutoutIntToIndex(storage.get(packageName , WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT)) ,
                ((dialogInterface, i) -> storage.put(packageName ,indexToCutoutInt(i)))
        ).create();

        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);

        dialog.show();
    }

    private void createNotificationChannel() {
        NotificationChannel channel =
                new NotificationChannel(Global.NOTIFICATION_CHANNEL_ID ,"Cutout Mode"
                        ,NotificationManager.IMPORTANCE_MIN);

        try {
            //noinspection JavaReflectionMemberAccess
            channel.getClass().getMethod("setBlockableSystem" ,boolean.class).invoke(channel ,true);
        } catch (Exception e) {
            Log.w(Global.TAG ,"setBlockableSystem failure");
        }

        notificationManager.createNotificationChannel(channel);
    }

    private int cutoutIntToIndex(int mode) {
        switch (mode) {
            case WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT:
                return 0;
            case WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER:
                return 1;
            case WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES:
                return 2;
        }
        return 0;
    }

    private int indexToCutoutInt(int index) {
        switch (index) {
            case 0 :
                return WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
            case 1 :
                return WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
            case 2 :
                return WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        return WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
    }

    private CharSequence loadApplicationLabel(String packageName) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName ,0);
            return info.loadLabel(context.getPackageManager());
        } catch (Exception e) {
            return packageName;
        }
    }

    private Context uiContext;
    private Context context;
    private NotificationManager notificationManager;
    private IActivityManager activityManager;
    private ConfigStorage storage;
}
