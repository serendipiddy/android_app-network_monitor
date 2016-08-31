package serendipiddy.com.androidnetworktraffic;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.TrafficStats;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NetworkUsageQueryServiceIntent extends IntentService {
    private static final String ACTION_READ_TRAFFIC_STATS = "serendipiddy.com.androidnetworktraffic.action.READ_TRAFFIC_STATS";

    public NetworkUsageQueryServiceIntent() {
        super("NetworkUsageQueryServiceIntent");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionMonitorTraffic (Context context, String name, String uid) {
        Intent intent = new Intent(context, NetworkUsageQueryServiceIntent.class);
        intent.setAction(ACTION_READ_TRAFFIC_STATS);
        intent.putExtra(MainActivity.EXTRA_NAME, name);
        intent.putExtra(MainActivity.EXTRA_UID, uid);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            // get data from incoming intent (the packageName and UID)
            final String action = intent.getAction();

            if (ACTION_READ_TRAFFIC_STATS.equals(action)) {
                final String name = intent.getStringExtra(MainActivity.EXTRA_NAME);
                final String uid = intent.getStringExtra(MainActivity.EXTRA_UID);
                handleActionMonitorTraffic(name, Integer.parseInt(uid));
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionMonitorTraffic(String name, int uid) {
//        throw new UnsupportedOperationException("Not yet implemented");
        Long rxBytes = TrafficStats.getUidRxBytes(uid);
        Long txBytes = TrafficStats.getUidTxBytes(uid);
        Long rxPackets = TrafficStats.getUidRxPackets(uid);
        Long txPackets = TrafficStats.getUidTxPackets(uid);
        Long timestamp = System.currentTimeMillis() / 1000L;

        try {
            FileOutputStream fout = openFileOutput("usage_"+name+".csv", MODE_APPEND);
            String output = timestamp+","+rxBytes+","+txBytes+","+rxPackets+","+txPackets+"\n";
            fout.write(output.getBytes());
        }
        catch (FileNotFoundException e) {
            Log.e("error","Could not find file usage_"+name);
        }
        catch (IOException e) {
            Log.e("error","Could not write to file usage_"+name);
        }

        Toast.makeText(getBaseContext(), "Updating app usage ("+uid+")", Toast.LENGTH_SHORT).show();
    }

}
