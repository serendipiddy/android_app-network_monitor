package serendipiddy.com.androidnetworktraffic;

import android.content.Intent;
import android.net.TrafficStats;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class TrafficStatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic_stats);

        Intent intent = getIntent();
        String title = intent.getStringExtra(MainActivity.EXTRA_NAME);
        int uid = intent.getIntExtra(MainActivity.EXTRA_UID, -2);
        int count = intent.getIntExtra(MainActivity.EXTRA_UID_COUNT, 0);

//        showUsingTrafficStats(title, uid, count);
        showAndWriteToFile(title, uid, count);

    }

    private void showAndWriteToFile(String title, int uid, int count) {
        TrafficStats ts = new TrafficStats();
        Long rxBytes = ts.getUidRxBytes(uid);
        Long txBytes = ts.getUidTxBytes(uid);
        Long rxPackets = ts.getUidRxPackets(uid);
        Long txPackets = ts.getUidTxPackets(uid);
        Long timestamp = System.currentTimeMillis() / 1000L;

        TextView textTitle = (TextView) findViewById(R.id.netStats_title);
        TextView textRxBytes = (TextView) findViewById(R.id.netStats_rx);
        TextView textTxBytes = (TextView) findViewById(R.id.netStats_tx);

        textTitle.setText(title); // +"\n("+count+")");
        textRxBytes.setText("Rx "+rxBytes.toString()+"B "+rxPackets.toString()+"pkt");
        textTxBytes.setText("Tx "+txBytes.toString()+"B "+txPackets.toString()+"pkt");

//        try {
//            FileOutputStream fout = openFileOutput("usage_"+title+".csv", MODE_APPEND);
//            String output = timestamp+","+rxBytes+","+txBytes+","+rxPackets+","+txPackets+"\n";
//            fout.write(output.getBytes());
//        }
//        catch (FileNotFoundException e) {
//            Log.e("error","Could not find file usage_"+title);
//        }
//        catch (IOException e) {
//            Log.e("error","Could not write to file usage_"+title);
//        }
    }

    private void showUsingTrafficStats(String title, int uid, int count) {
        TrafficStats ts = new TrafficStats();
        Long  rxBytes = ts.getUidRxBytes(uid);
        Long  txBytes = ts.getUidTxBytes(uid);
        Long  rxPackets = ts.getUidRxPackets(uid);
        Long  txPackets = ts.getUidTxPackets(uid);

        TextView textTitle = (TextView) findViewById(R.id.netStats_title);
        TextView textRxBytes = (TextView) findViewById(R.id.netStats_rx);
        TextView textTxBytes = (TextView) findViewById(R.id.netStats_tx);

        textTitle.setText(title);
        textRxBytes.setText("Rx "+rxBytes.toString()+"B "+rxPackets.toString()+"pkt");
        textTxBytes.setText("Tx "+txBytes.toString()+"B "+txPackets.toString()+"pkt");
    }

    /**
     * A button action which starts monitoring the selected app in the background
     */
    public void startAsService(View view) {
        Intent intent = getIntent();
        String title = intent.getStringExtra(MainActivity.EXTRA_NAME);
        int uid = intent.getIntExtra(MainActivity.EXTRA_UID, -2);

        // START THE SERVICE
        NetworkUsageQueryServiceIntent.startActionMonitorTraffic(view.getContext(), title, new Integer(uid).toString());
    }
}
