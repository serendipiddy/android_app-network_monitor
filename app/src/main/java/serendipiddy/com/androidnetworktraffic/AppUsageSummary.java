package serendipiddy.com.androidnetworktraffic;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

public class AppUsageSummary extends AppCompatActivity {
    private static final String TAG = "AppUsageSummary";

    private final StringBuilder currentAppWifiResults = new StringBuilder();
    private final StringBuilder currentAppMobileResults = new StringBuilder();
    private String OUTPUT_DIR;
    private ApplicationItem thisAppInfo;
    private final int TWO_HOURS_AS_MS = 3600000 * 2;
    public static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public final String NETWORK_OUTPUT = "networkUsage";
    public final String SUMMARY_OUTPUT = "summaryUsage";
    private AppUsageValues thisUsageValues = new AppUsageValues();

    public static final String headerString = "startTime endTime startTimeStamp endTimeStamp "
            + "rxPackets txPackets "
            + "rxBytes txBytes type\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_usage_summary);

        OUTPUT_DIR = "Android/data/serendipiddy.com"+getString(R.string.app_name)+"/";

        acquireAndDisplaySummary();
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
        // TODO this was a copy-paste from old mainActivity
        switch (item.getItemId()) {
            case R.id.save_usage_button:
                if (thisAppInfo.packageName == null) {
                    Toast.makeText(getBaseContext(), "Please Select An App", Toast.LENGTH_SHORT).show();
                }
                else {
                    writeOut(thisAppInfo, thisUsageValues, currentAppWifiResults, currentAppMobileResults);
                    Toast.makeText(getBaseContext(), "Saved usage to: " + OUTPUT_DIR + thisAppInfo.packageName, Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Gets the apps usage information and displays it
     */
    private void acquireAndDisplaySummary() {
        // get the intent used to start activity, extracting the values needed
        thisAppInfo = getAppDetails();

        Log.d(TAG, thisAppInfo.appLabel+" "+thisAppInfo.installTime);

        // perform the usage info extraction, populating thisUsageValues
        getUsageStats(thisAppInfo, thisUsageValues);
        getNetworkUsage(thisAppInfo, thisUsageValues, currentAppWifiResults, currentAppMobileResults);

        // populate the fields on screen
        outputResultsToScreen(thisAppInfo, thisUsageValues);
    }

    /**
     * Extracts the information about this selected application from this activity's intent
     * @return
     */
    private ApplicationItem getAppDetails() {
        Intent data = getIntent();
        String appName = data.getStringExtra(InstalledAppListActivity.EXTRA_NAME);
        String appLabel = data.getStringExtra(InstalledAppListActivity.EXTRA_LABEL);
        long installTime = data.getLongExtra(InstalledAppListActivity.EXTRA_INSTALL_TIME, -1);
        int appUID = data.getIntExtra(InstalledAppListActivity.EXTRA_UID, -1);
        Log.d(TAG, appName +" "+ appUID);

        return new ApplicationItem(appName,appLabel,appUID,installTime);
    }

    /**
     * Retrieves the required app usage information, placing it in values
     * @param app
     * @param values
     */
    private void getUsageStats(ApplicationItem app, AppUsageValues values) {
        Calendar cal_from = Calendar.getInstance();
        Calendar cal_to = Calendar.getInstance();
        cal_from.setTimeInMillis(app.getInstallTime(getPackageManager()));
        cal_to.setTimeInMillis(System.currentTimeMillis());

        long start = cal_from.getTimeInMillis();
        long end = cal_to.getTimeInMillis() + TWO_HOURS_AS_MS;

        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        // using aggregate/package name method, get total time available..
        Map<String, UsageStats> agg = usm.queryAndAggregateUsageStats(start, end);

        UsageStats u = agg.get(app.packageName);
        if (u == null) return;
        values.usage_duration = (double) u.getTotalTimeInForeground()/(1000);
        values.usageDateRange = new DateRange(u.getFirstTimeStamp(),u.getLastTimeStamp());
    }

    /**
     * Retrieves the required network usage information, placing it in values
     */
    private void getNetworkUsage(ApplicationItem app, AppUsageValues values, StringBuilder usageWifi, StringBuilder usageMobile) {
        String type_wifi = "wifi", type_mobile = "mobile";

        values.networkDateRange = new DateRange(app.getInstallTime(getPackageManager()),System.currentTimeMillis());

//        sb_main.append("FROM:\t" + cal_from.getTime() + "\n");
//        sb_main.append("TO:\t" + cal_to.getTime() +"\n");
        long start = values.networkDateRange.start.getTimeInMillis();
        long end = values.networkDateRange.end.getTimeInMillis() + TWO_HOURS_AS_MS;

        // Make network stats queries
        NetworkStats queryNetworkStatsWifi = getNetworkStats(start, "", end, app.uid, ConnectivityManager.TYPE_WIFI, this);
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String mobileSubscription = tm.getSubscriberId();
        NetworkStats queryNetworkStatsData = getNetworkStats(start, mobileSubscription, end, app.uid, ConnectivityManager.TYPE_MOBILE, this);

        // Count packets and read each bucket into StringBuilders.
        values.totalTraffic = new TrafficTotals();
        values.wifiTraffic = readBuckets(queryNetworkStatsWifi, usageWifi, type_wifi);
        values.mobileTraffic = readBuckets(queryNetworkStatsData, usageMobile, type_mobile);

        // Calculate and write summary to main string builder
        values.totalTraffic.rxPackets = values.mobileTraffic.rxPackets + values.wifiTraffic.rxPackets;
        values.totalTraffic.txPackets = values.mobileTraffic.txPackets + values.wifiTraffic.txPackets;
        values.totalTraffic.rxBytes = values.mobileTraffic.rxBytes + values.wifiTraffic.rxBytes;
        values.totalTraffic.txBytes = values.mobileTraffic.txBytes + values.wifiTraffic.txBytes;
    }

    /**
     * Read the buckets in the given query, appending each buckets results to sb.
     * @param query NetworkStats query
     * @param sb    StringBuilder used to save output to file
     * @param type  type of network connection
     * @return
     */
    public TrafficTotals readBuckets(NetworkStats query, StringBuilder sb, String type) {
        TrafficTotals tt = new TrafficTotals();
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();

        while (query != null && query.hasNextBucket()) {
            query.getNextBucket(bucket);
            String startTime = DateRange.getDateString(bucket.getStartTimeStamp(),DATE_FORMAT);
            String endTime = DateRange.getDateString(bucket.getEndTimeStamp(),DATE_FORMAT);
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
     * Get the network stats object to extract usage data from
     * @param type
     * @return
     */
    private NetworkStats getNetworkStats(long start, String subscription, long end, int uid, int type, Context context) {
        NetworkStatsManager networkStatsMan = (NetworkStatsManager) context.getSystemService(Context.NETWORK_STATS_SERVICE);
        try {
            return networkStatsMan.queryDetailsForUid(type, subscription, start, end, uid);
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Distplays the given values to the Activity's TextView fields
     * @param values
     */
    private void outputResultsToScreen(ApplicationItem app, AppUsageValues values) {
        // get all the text fields to change
        TextView label = (TextView) findViewById(R.id.textView_appLabel);
        TextView pkgName = (TextView) findViewById(R.id.textView_packageName);
        TextView duration = (TextView) findViewById(R.id.textView_durationValue);
        TextView datesUsage = (TextView) findViewById(R.id.textView_datesUsageValue);
        TextView datesNetwork = (TextView) findViewById(R.id.textView_datesNetworkValues);
        TextView totalTraffic = (TextView) findViewById(R.id.textView_totalNetworkTrafficValues);
        TextView wifiTraffic = (TextView) findViewById(R.id.textView_wifiNetworkTrafficValues);
        TextView mobileTraffic = (TextView) findViewById(R.id.textView_mobileNetworkTrafficValues);

        // set the new values of the fields
        label.setText(app.appLabel);
        pkgName.setText(app.packageName);
        duration.setText(values.usage_duration+" s");
        datesUsage.setText(values.usageDateRange.toSplitString());
        datesNetwork.setText(values.networkDateRange.toSplitString());
        totalTraffic.setText(values.totalTraffic.displayRxOutput()+"\n"+
                values.totalTraffic.displayTxOutput());
        wifiTraffic.setText(values.wifiTraffic.displayRxOutput()+"\n"+
                values.wifiTraffic.displayTxOutput());
        mobileTraffic.setText(values.mobileTraffic.displayRxOutput()+"\n"+
                values.mobileTraffic.displayTxOutput());
    }

    /**
     * Stages the values to output to file, then writes them
     * @param app
     * @param values
     * @param usageWifi
     * @param usageMobile
     */
    private void writeOut(ApplicationItem app, AppUsageValues values, StringBuilder usageWifi, StringBuilder usageMobile) {
        StringBuilder networkOutput = new StringBuilder();
        StringBuilder summaryOutput = new StringBuilder();

        networkOutput.append(headerString);
        networkOutput.append(usageWifi);
        networkOutput.append(usageMobile);
        writeUsageToFile(networkOutput, app.packageName+NETWORK_OUTPUT);

        summaryOutput.append("Name: "+app.appLabel+"\n");
        summaryOutput.append("Package: "+app.packageName+"\n");
        summaryOutput.append("TimeInstalled: "+values.networkDateRange.start+"\n");
        summaryOutput.append("UsageDuration: "+values.usage_duration+"\n");
        summaryOutput.append("UsageRange: "+values.usageDateRange+"\n");
        // TODO append the other values to output (tx, rx totals etc)

        writeUsageToFile(summaryOutput, app.packageName+SUMMARY_OUTPUT);
    }

    /**
     * Start the Task which writes the usage buckets (sb) to file.
     * @param sb
     * @param filename
     */
    public void writeUsageToFile(final StringBuilder sb, final String filename) {
        Handler handler = new Handler();

        if (sb.length() <= 0) {
            return;
        }
        final File logfile = new File(getExternalFilesDir(""), filename);
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
     * Class for simplifying summary of a traffic
     */
    class TrafficTotals {
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
        public String displayRxOutput() { return "Rx "+rxPackets+" pkts "+getRxKilobytes()+" KB"; }
        public String displayTxOutput() { return "Tx "+txPackets+" pkts "+getTxKilobytes()+" KB"; }

    }

    /**
     * Holds the usage results for this application
     */
    class AppUsageValues {
        public Double usage_duration = -1.0;
        public DateRange usageDateRange = null;
        public DateRange networkDateRange = null;
        public TrafficTotals totalTraffic = null;
        public TrafficTotals wifiTraffic = null;
        public TrafficTotals mobileTraffic = null;
    }
}

class DateRange {
    public Calendar start;
    public Calendar end;

    public DateRange (Calendar start, Calendar end) {
        this.start = start;
        this.end = end;
    }

    public DateRange (long start, long end) {
        this.start = asCalendar(start);
        this.end = asCalendar(end);
    }

    /**
     * Converts a long to a Calendar object
     * @param milliseconds
     * @return
     */
    public static Calendar asCalendar(long milliseconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return calendar;
    }

    /**
     * Return date in specified format.
     * @param cal date to convert
     * @param dateFormat Date format
     * @return String representing date in specified format
     */
    public static String getDateString(Calendar cal, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        formatter.setTimeZone(TimeZone.getDefault()); // Set to local timezone

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        return formatter.format(cal.getTime());
    }

    public static String getDateString(long milliseconds, String dateFormat) {
        return getDateString(asCalendar(milliseconds), dateFormat);
    }

    public String toString() {
        return getDateString(this.start,AppUsageSummary.DATE_FORMAT)
                +" - "+getDateString(this.end,AppUsageSummary.DATE_FORMAT);
    }

    public String toSplitString() {
        return getDateString(this.start,AppUsageSummary.DATE_FORMAT)
                +"\n"+getDateString(this.end,AppUsageSummary.DATE_FORMAT);
    }
}
