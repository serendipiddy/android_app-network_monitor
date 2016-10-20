package serendipiddy.com.androidnetworktraffic;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public final static int GET_NEW_APP_UID = 1923; // putting here keeps the intent references locally consistent
    public final String TAG = "networkUsageMain";
    private final String DATE_FORMAT = "dd/MM/yy hh:mm:ss.SSS";

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
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });


    }

    /**
     * Get a new app
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
        if (requestCode == GET_NEW_APP_UID) {
            if(resultCode == AppCompatActivity.RESULT_OK){
                String appName = data.getStringExtra(InstalledAppList.EXTRA_NAME);
                String appLabel = data.getStringExtra(InstalledAppList.EXTRA_LABEL);
                int appUID = data.getIntExtra(InstalledAppList.EXTRA_UID, -1);
                Log.d(TAG, appName +" "+ appUID);
                // TODO add it to the current list of apps displayed
                addApplicationLabelToMainView(appName, appLabel, appUID);
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
    private void addApplicationLabelToMainView(String name, String label, int uid) {
        TextView mainText = (TextView) findViewById(R.id.selectedApplicationsView);
        mainText.setText(label +" "+ uid);
        // TODO use a list instead of a textview
        testingUsageMon(uid);
    }

    /**
     * Temporary method for figuring out how the NetworkStats system works.
     * Prints some results for the selected uid.
     * @param uid
     */
    private void testingUsageMon(int uid) {
        // get the existing textview
        TextView mainText = (TextView) findViewById(R.id.selectedApplicationsView);
        NetworkStatsManager networkStatsMan = (NetworkStatsManager) getSystemService(Context.NETWORK_STATS_SERVICE);
        StringBuilder sb = new StringBuilder();
        sb.append(mainText.getText()+"\n");

        Calendar cal_from = Calendar.getInstance();
        Calendar cal_to = Calendar.getInstance();
        // TODO try adjusting granularity to within a few seconds, and see how accurate it is
        cal_from.add(Calendar.HOUR_OF_DAY, -2);
        cal_to.add(Calendar.HOUR_OF_DAY, +2);
        NetworkStats queryNetworkStats;

        // TODO handle case where usage permission isn't granted
        try {
            long start = cal_from.getTimeInMillis();
            long end = cal_to.getTimeInMillis();
//            long end = System.currentTimeMillis();
            sb.append("FROM: " + cal_from.getTime() + "\nTO:   " + cal_to.getTime() +"\n");

            queryNetworkStats = networkStatsMan
                    .queryDetailsForUid(ConnectivityManager.TYPE_WIFI, "", start, end, uid);
        }
        catch (RemoteException e) {
            e.printStackTrace();
            return;
        }

        // Iterate through the buckets and
        sb.append("### Buckets\n");
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        while (queryNetworkStats.hasNextBucket()) {
            queryNetworkStats.getNextBucket(bucket);

            String startTime = getDate(bucket.getStartTimeStamp(),DATE_FORMAT);
            String endTime = getDate(bucket.getEndTimeStamp(),DATE_FORMAT);

            sb.append(startTime + " - " + endTime
                    + "\n   " + bucket.getRxPackets() + "Rx " + bucket.getTxPackets() + "Tx\n");
        }
        sb.append("### end buckets\n");
        mainText.setText(sb.toString());
    }

    /**
     * Retrieves the list of monitored UID/apps, checks if they're valid, then displays them
     */
    private void populateMonitoredAppList() {

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
