package serendipiddy.com.androidnetworktraffic;

import android.app.AppOpsManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public final static int GET_NEW_APP_UID = 1923; // putting here keeps the intent references locally consistent
    public final String TAG = "networkUsageMain";
    private final String DATE_FORMAT = "dd/MM/yy hh:mm:ss.SSS";
    private final String REQUIRED_PERMISSION = AppOpsManager.OPSTR_GET_USAGE_STATS;
    private final String USAGE_DIR = "testing";
    private String OUTPUT_DIR;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNewAppUID();
            }
        });

        OUTPUT_DIR = "Android/data/serendipiddy.com"+getString(R.string.app_name)+"/"+USAGE_DIR;
    }

    @Override
    protected void onResume() {
        super.onResume();
        assertUsagePermissions();
    }

    /**
     * Creates the options menu (top right)
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Handles menu button presses
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_current_app_button:
                Toast.makeText(getBaseContext(), "Unimplemented Yet", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.save_usage_button:
                if (currentAppName == null) {
                    Toast.makeText(getBaseContext(), "No App selected", Toast.LENGTH_SHORT).show();
                }
                else {
                    String outputName = currentAppName + ".usage";
                    writeUsageToFile(sb_wifi, outputName);
                    Toast.makeText(getBaseContext(), "Saved usage to: "+ OUTPUT_DIR + "/" + outputName, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.open_usage_access_button:
                openUsageAccessSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Checks whether this app has access to usage stats, if not it prompts user to grant this
     */
    private boolean assertUsagePermissions() {
        if (hasUsagePermission()) {
            return true;
        }
        Toast.makeText(getBaseContext(), "Please grant usage permission", Toast.LENGTH_LONG).show();
        return false;
    }

    /**
     * Sends the user to the settings page for enabling/disabling usage access
     */
    public void openUsageAccessSettings() {
        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
    }

    /**
     * Checks whether the the context of an app has the given permission
     * @return
     */
    private boolean hasUsagePermission() {
        Context context = getBaseContext();
        String permission = REQUIRED_PERMISSION;
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo applicationInfo = pm.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager =
                    (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager
                    .checkOpNoThrow(permission, applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Get a new app to display network usage for
     */
    private void getNewAppUID() {
        Intent chooseUID = new Intent(this, InstalledAppList.class);
        startActivityForResult(chooseUID, GET_NEW_APP_UID);
    }

    /**
     * Get the result of a invocation of InstalledAppList or other
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!assertUsagePermissions()) {
            return;
        }

        if (requestCode == GET_NEW_APP_UID) {
            if(resultCode == AppCompatActivity.RESULT_OK){
                String appName = data.getStringExtra(InstalledAppList.EXTRA_NAME);
                String appLabel = data.getStringExtra(InstalledAppList.EXTRA_LABEL);
                long installTime = data.getLongExtra(InstalledAppList.EXTRA_INSTALL_TIME, -1);
                int appUID = data.getIntExtra(InstalledAppList.EXTRA_UID, -1);
                Log.d(TAG, appName +" "+ appUID);
                // TODO add it to the current list of apps displayed
                addApplicationLabelToMainView(appName, appLabel, appUID, installTime);
                // TODO add it to the current list of apps, in memory
            }
            if (resultCode == AppCompatActivity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    /**
     * Adds an item to the list of selected apps to monitor
     * @param name
     * @param label
     * @param uid
     */
    private void addApplicationLabelToMainView(String name, String label, int uid, long installTime) {
        testingUsageMon(name, uid, installTime);
    }

    private StringBuilder sb_main = null;
    private StringBuilder sb_wifi = null;
    private StringBuilder sb_mobile = null;
    private String currentAppName = null;

    /**
     * TODO update this description
     * Temporary method for figuring out how the NetworkStats system works.
     * Prints some results for the selected uid.
     * @param uid
     */
    private void testingUsageMon(String appName, int uid, long installTime) {
        // TODO remember the end time of the last read, then only collect updated stats, with option to refresh/collect from scratch

        // String builders to capture output
        currentAppName = appName;
        sb_main = new StringBuilder();
        sb_wifi = new StringBuilder();
        sb_mobile = new StringBuilder();

        String headerString = "startTime endTime startTimeStamp endTimeStamp "
                + "rxPackets txPackets "
                + "rxBytes txBytes type\n";
        String type_wifi = "wifi", type_mobile = "mobile";

        sb_main.append(appName+"\n");
        sb_wifi.append(headerString);
        sb_mobile.append(headerString);

        Calendar cal_from = Calendar.getInstance();
        Calendar cal_to = Calendar.getInstance();
        cal_from.setTimeInMillis(installTime);
        cal_to.setTimeInMillis(System.currentTimeMillis());

        sb_main.append("FROM:\t" + cal_from.getTime() + "\n");
        sb_main.append("TO:\t" + cal_to.getTime() +"\n");
        long start = cal_from.getTimeInMillis();
        long end = cal_to.getTimeInMillis() + 3600000 * 2;

        NetworkStats queryNetworkStatsWifi = getNetworkStats(start, end, uid, ConnectivityManager.TYPE_WIFI);
        NetworkStats queryNetworkStatsData = getNetworkStats(start, end, uid, ConnectivityManager.TYPE_MOBILE);

        TrafficTotals totalTraffic = new TrafficTotals();
        TrafficTotals totals_wifi = readBuckets(queryNetworkStatsWifi, sb_wifi, type_wifi);
        TrafficTotals totals_mobile = readBuckets(queryNetworkStatsData, sb_mobile, type_mobile);

        totalTraffic.rxPackets = totals_mobile.rxPackets + totals_wifi.rxPackets;
        totalTraffic.txPackets = totals_mobile.txPackets + totals_wifi.txPackets;
        totalTraffic.rxBytes = totals_mobile.rxBytes + totals_wifi.rxBytes;
        totalTraffic.txBytes = totals_mobile.txBytes + totals_wifi.txBytes;

        sb_main.append("\n### Total Usage ###\n");
        appendSummary(sb_main, totalTraffic);
        sb_main.append("\n### Mobile Usage ###\n");
        appendSummary(sb_main, totals_mobile);
        sb_main.append("\n### Wifi Usage ###\n");
        appendSummary(sb_main, totals_wifi);

        TextView mainText = (TextView) findViewById(R.id.selectedApplicationsView);
        mainText.setText(sb_main.toString());
    }

    /**
     * Read the buckets in the given query, appending each buckets results to sb.
     * @param query NetworkStats query
     * @param sb    StringBuilder used to save output to file
     * @param type  type of network connection
     * @return
     */
    private TrafficTotals readBuckets(NetworkStats query, StringBuilder sb, String type) {
        TrafficTotals tt = new TrafficTotals();
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();

        while (query != null && query.hasNextBucket()) {
            query.getNextBucket(bucket);
            String startTime = getDate(bucket.getStartTimeStamp(),DATE_FORMAT);
            String endTime = getDate(bucket.getEndTimeStamp(),DATE_FORMAT);
            sb.append("\""+startTime + "\" \"" + endTime +"\" "
                    + bucket.getStartTimeStamp() + " " + bucket.getEndTimeStamp() + " "
                    + bucket.getRxPackets() + " " + bucket.getTxPackets() + " "
                    + bucket.getRxBytes() + " " + bucket.getTxBytes() + " " + type + "\n");
            tt.rxPackets += bucket.getRxPackets();
            tt.rxBytes += bucket.getRxBytes();
            tt.txPackets += bucket.getTxPackets();
            tt.txBytes += bucket.getTxBytes();
        }

        return tt;
    }

    /**
     * Class for simplifying summary of a traffic
     */
    private class TrafficTotals {
        public long rxPackets = 0;
        public long txPackets = 0;
        public long rxBytes = 0;
        public long txBytes = 0;

        private final int kb = 1024;
        private final int mb = 1024 * 1024;

        public long getRxKilobytes() { return rxPackets / kb; }
        public long getTxKilobytes() { return txPackets / kb; }
        public long getRxMegabytes() { return rxPackets / mb; }
        public long getTxMegabytes() { return rxPackets / mb; }
        public long getRxRate() { return rxPackets == 0 ? 0 : rxBytes / rxPackets; }
        public long getTxRate() { return txPackets == 0 ? 0 : txBytes / txPackets; }

    }

    /**
     * Outputs the given summary to given string builder
     */
    private void appendSummary(StringBuilder sb, TrafficTotals tt) {
        String NO_TRAFFIC = " (no record) ";

        sb.append("Packets: \n");
        sb.append("    Rx: "+ (tt.rxPackets == 0 ? NO_TRAFFIC : tt.rxPackets) +" pkts\n");
        sb.append("    Tx: "+ (tt.txPackets == 0 ? NO_TRAFFIC : tt.txPackets) +" pkts\n");

        sb.append("Bytes: \n");
        sb.append("    Rx: "+ (tt.rxBytes == 0 ? NO_TRAFFIC : tt.getRxKilobytes()) +" KB\n");
        sb.append("    Tx: "+ (tt.txBytes == 0 ? NO_TRAFFIC : tt.getTxKilobytes()) +" KB\n");

        sb.append("Average Rate:\n");
        sb.append("    Rx: "+ (tt.rxPackets == 0 ? NO_TRAFFIC : tt.getRxRate()) +" B/pkt\n");
        sb.append("    Rx: "+ (tt.txPackets == 0 ? NO_TRAFFIC : tt.getTxRate()) +" B/pkt\n");
    }

    /**
     * Get the network stats object to extract usage data from
     * @param type
     * @return
     */
    private NetworkStats getNetworkStats(long start, long end, int uid, int type) {
        NetworkStatsManager networkStatsMan = (NetworkStatsManager) getSystemService(Context.NETWORK_STATS_SERVICE);
        try {
            return networkStatsMan.queryDetailsForUid(type, "", start, end, uid);
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Start the Task which writes the usage buckets (sb) to file.
     * @param sb
     * @param filename
     */
    private void writeUsageToFile(final StringBuilder sb, final String filename) {
        Handler handler = new Handler();
        String outputDir = "testing";

        final File logfile = new File(getBaseContext().getExternalFilesDir(outputDir), filename);
        if (logfile.exists())
            { logfile.delete(); }
        try {
            Log.i(TAG, "Creating file "+filename);
            logfile.createNewFile();
        }
        catch (IOException e)
            { e.printStackTrace(); }

        handler.post(new Runnable() {
            /**
             * Write the statistics info buffer to file
             */
            public void run() {
                Log.d(TAG, "Writing data to file \""+ filename +"\"");
                try
                {
                    //BufferedWriter for performance, true to set append to file flag
                    BufferedWriter buf = new BufferedWriter(new FileWriter(logfile, true));
                    buf.append(sb.toString());
                    buf.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Return date in specified format.
     * @param milliSeconds Date in milliseconds
     * @param dateFormat Date format
     * @return String representing date in specified format
     */
    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}
