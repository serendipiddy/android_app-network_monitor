package serendipiddy.com.androidnetworktraffic;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
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
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
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
import java.util.Map;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    public final static int GET_NEW_APP_UID = 1923; // putting here keeps the intent references locally consistent
    public final String TAG = "networkUsageMain";
    private final String DATE_FORMAT = "dd/MM/yy HH:mm:ss"; // .SSS";
    private final String USAGE_PERMISSION = AppOpsManager.OPSTR_GET_USAGE_STATS;
    private final String TELEPHONY_PERMISSION =  Manifest.permission.READ_PHONE_STATE;
    private final int REQUEST_TELEPHONY_PERMISSION = 980;
    private String OUTPUT_DIR;

    private final String headerString = "startTime endTime startTimeStamp endTimeStamp "
            + "rxPackets txPackets "
            + "rxBytes txBytes type\n";
    private StringBuilder currentAppWifiResults = null;
    private StringBuilder currentAppMobileResults = null;
    private String currentAppName = null;


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

        OUTPUT_DIR = "Android/data/serendipiddy.com"+getString(R.string.app_name)+"/";
    }

    @Override
    protected void onResume() {
        super.onResume();
        assertPermissions();
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
                Toast.makeText(getBaseContext(), "(Unimplemented)", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.save_usage_button:
                if (currentAppName == null) {
                    Toast.makeText(getBaseContext(), "Please Select An App", Toast.LENGTH_SHORT).show();
                }
                else {
                    writeUsageToFile(currentAppWifiResults, currentAppName + ".wifi.usage");
                    writeUsageToFile(currentAppMobileResults, currentAppName  + ".mobile.usage");
                    Toast.makeText(getBaseContext(), "Saved usage to: " + OUTPUT_DIR + currentAppName, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.open_app_settings_button:
                startActivity(new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));
                Toast.makeText(getBaseContext(), "App Permissions\nEnable/Disable telephony", Toast.LENGTH_LONG).show();
                return true;
            case R.id.open_usage_access_button:
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Checks whether this app has access to usage stats, if not it prompts user to grant this
     */
    private boolean assertPermissions() {
        if (hasUsagePermission()) {
            Log.d(TAG, "Check telephony permission: "+hasPermission(TELEPHONY_PERMISSION));
            if (hasPermission(TELEPHONY_PERMISSION)) {
                return true;
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE},
                    REQUEST_TELEPHONY_PERMISSION);
//            Toast.makeText(getBaseContext(), "Please grant telephony permission", Toast.LENGTH_LONG).show();
            return false;
        }
        Toast.makeText(getBaseContext(), "Please grant usage permission", Toast.LENGTH_LONG).show();
        return false;
    }

    private boolean hasPermission(String permission) {
        return ActivityCompat
                .checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Checks whether the the context of an app has the given permission
     * @return
     */
    private boolean hasUsagePermission() {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo applicationInfo = pm.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager =
                    (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager
                    .checkOpNoThrow(USAGE_PERMISSION, applicationInfo.uid, applicationInfo.packageName);
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
        Toast.makeText(getBaseContext(), "Populating list of apps..", Toast.LENGTH_SHORT).show();
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
        if (!assertPermissions()) {
            return;
        }

        if (requestCode == GET_NEW_APP_UID) {
            if(resultCode == AppCompatActivity.RESULT_OK){
                String appName = data.getStringExtra(InstalledAppList.EXTRA_NAME);
                String appLabel = data.getStringExtra(InstalledAppList.EXTRA_LABEL);
                long installTime = data.getLongExtra(InstalledAppList.EXTRA_INSTALL_TIME, -1);
                int appUID = data.getIntExtra(InstalledAppList.EXTRA_UID, -1);
                Log.d(TAG, appName +" "+ appUID);
                addApplicationLabelToMainView(appName, appLabel, appUID, installTime);
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
        testingUsageStats(name, uid, installTime);
    }

    private void testingUsageStats(String appName, int uid, long installTime) {
        // String builders to capture output
        currentAppName = appName;
        StringBuilder usageResults = new StringBuilder();

        TextView mainText = (TextView) findViewById(R.id.selectedApplicationsView);
        usageResults.append(mainText.getText()+"\n");

        Calendar cal_from = Calendar.getInstance();
        Calendar cal_to = Calendar.getInstance();
        cal_from.setTimeInMillis(installTime);
        cal_to.setTimeInMillis(System.currentTimeMillis());

        long start = cal_from.getTimeInMillis();
        long end = cal_to.getTimeInMillis() + 3600000 * 2;

        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
//        List<UsageStats> us = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end);
//
//        for (UsageStats u : us) {
//            if (u == null)
//                continue;
//            if (u.getPackageName().contains(appName)) {
//                usageResults.append("###" + u.getPackageName() + "\n");
//                usageResults.append("  * Total: " + (double) u.getTotalTimeInForeground()/(1000) + "\n");
//                usageResults.append("  * Beginning: " + getDate(u.getFirstTimeStamp(),DATE_FORMAT) + "\n");
//                usageResults.append("  * End of TS:" + getDate(u.getLastTimeStamp(),DATE_FORMAT) + "\n");
//                usageResults.append("  * Previous:" + getDate(u.getLastTimeUsed(),DATE_FORMAT) + "\n");
//            }
//        }

        // using aggregate/package name method, get total time available..
        Map<String, UsageStats> agg = usm.queryAndAggregateUsageStats(start, end);

        UsageStats u = agg.get(appName);
        if (u == null) return;
        usageResults.append("###" + u.getPackageName() + "\n");
        usageResults.append("  * Total: " + (double) u.getTotalTimeInForeground()/(1000) + "s\n");
        usageResults.append("  * Beginning: " + getDate(u.getFirstTimeStamp(),DATE_FORMAT) + "\n");
        usageResults.append("  * End of TS: " + getDate(u.getLastTimeStamp(),DATE_FORMAT) + "\n");
        usageResults.append("  * Previous:  " + getDate(u.getLastTimeUsed(),DATE_FORMAT) + "\n");


        mainText.setText(usageResults.toString());
    }

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
        currentAppWifiResults = new StringBuilder();
        currentAppMobileResults = new StringBuilder();
        StringBuilder sb_main = new StringBuilder();

        String type_wifi = "wifi", type_mobile = "mobile";

        sb_main.append(appName+"\n");

        // Calculate 'to' and 'from' times
        Calendar cal_from = Calendar.getInstance();
        Calendar cal_to = Calendar.getInstance();
        cal_from.setTimeInMillis(installTime);
        cal_to.setTimeInMillis(System.currentTimeMillis());

        sb_main.append("FROM:\t" + cal_from.getTime() + "\n");
        sb_main.append("TO:\t" + cal_to.getTime() +"\n");
        long start = cal_from.getTimeInMillis();
        long end = cal_to.getTimeInMillis() + 3600000 * 2;

        // Make network stats queries
        NetworkStats queryNetworkStatsWifi = getNetworkStats(start, "", end, uid, ConnectivityManager.TYPE_WIFI);
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String mobileSubscription = tm.getSubscriberId();
        NetworkStats queryNetworkStatsData = getNetworkStats(start, mobileSubscription, end, uid, ConnectivityManager.TYPE_MOBILE);

        // Count packets and read each bucket into StringBuilders.
        TrafficTotals totalTraffic = new TrafficTotals();
        TrafficTotals totals_wifi = readBuckets(queryNetworkStatsWifi, currentAppWifiResults, type_wifi);
        TrafficTotals totals_mobile = readBuckets(queryNetworkStatsData, currentAppMobileResults, type_mobile);

        // Calculate and write summary to main string builder
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

        // Output summary to TextView
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

        public long getRxKilobytes() { return rxBytes / kb; }
        public long getTxKilobytes() { return txBytes / kb; }
        public long getRxMegabytes() { return rxBytes / mb; }
        public long getTxMegabytes() { return rxBytes / mb; }
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
    private NetworkStats getNetworkStats(long start, String subscription, long end, int uid, int type) {
        NetworkStatsManager networkStatsMan = (NetworkStatsManager) getSystemService(Context.NETWORK_STATS_SERVICE);
        try {
            return networkStatsMan.queryDetailsForUid(type, subscription, start, end, uid);
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

        if (sb.length() <= 0) {
            return;
        }
        sb.insert(0, headerString);

        final File logfile = new File(getBaseContext().getExternalFilesDir(""), filename);
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
//        formatter.setTimeZone(TimeZone.getTimeZone("GMT")); // Set to local timezone
        formatter.setTimeZone(TimeZone.getDefault()); // Set to local timezone
//        milliSeconds -= 13 * 3600 * 1000;

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
//        calendar.setTimeZone(TimeZone.getDefault());
        return formatter.format(calendar.getTime());
    }
}
