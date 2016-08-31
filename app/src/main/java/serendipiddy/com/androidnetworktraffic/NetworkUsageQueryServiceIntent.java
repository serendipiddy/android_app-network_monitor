package serendipiddy.com.androidnetworktraffic;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NetworkUsageQueryServiceIntent extends IntentService {
    private static final String ACTION_READ_TRAFFIC_STATS = "serendipiddy.com.androidnetworktraffic.action.READ_TRAFFIC_STATS";

    // TODO: Rename parameters
    private static final String EXTRA_NAME = "serendipiddy.com.androidnetworktraffic.extra.NAME";
    private static final String EXTRA_UID = "serendipiddy.com.androidnetworktraffic.extra.UID";

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
        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_UID, uid);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            // get data from incoming intent (the packageName and UID)
            final String action = intent.getAction();

            if (ACTION_READ_TRAFFIC_STATS.equals(action)) {
                final String name = intent.getStringExtra(EXTRA_NAME);
                final String uid = intent.getStringExtra(EXTRA_UID);
                handleActionMonitorTraffic(name, uid);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionMonitorTraffic(String name, String uid) {
        // TODO: Handle action Monitor Traffic
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
