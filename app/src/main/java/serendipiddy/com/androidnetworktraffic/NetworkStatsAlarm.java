package serendipiddy.com.androidnetworktraffic;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Listens to alarms for running this service.
 */
public class NetworkStatsAlarm extends BroadcastReceiver {

    private static final int ALARM_INTERVAL = 10 * 1000; // 10 seconds

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TSA","Received alarm signal");
        String title = intent.getStringExtra(MainActivity.EXTRA_NAME);
        int uid = intent.getIntExtra(MainActivity.EXTRA_UID, -2);
        NetworkUsageQueryServiceIntent.startActionMonitorTraffic(context, title, new Integer(uid).toString());
    }

    public void setAlarm(Context context, String name, String uid)
    {
        Log.d("TSA","Alarm set");
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NetworkStatsAlarm.class);
        intent.putExtra(MainActivity.EXTRA_NAME, name);
        intent.putExtra(MainActivity.EXTRA_UID, uid);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, System.currentTimeMillis(), ALARM_INTERVAL, pi); // Millisec * Second * Minute
    }

    public void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, NetworkStatsAlarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }


}
