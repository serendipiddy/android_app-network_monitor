package serendipiddy.com.androidnetworktraffic;

import android.content.Intent;
import android.net.TrafficStats;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

        showUsingTrafficStats(title, uid, count);
//        showUsingProcUIDStats(title, uid, count);

    }

    private void showUsingProcUIDStats(String title, int uid, int count) {
        TrafficStats ts = new TrafficStats();
        Long  rxBytes = ts.getUidRxBytes(uid);
        Long  txBytes = ts.getUidTxBytes(uid);
        Long  rxPackets = ts.getUidRxPackets(uid);
        Long  txPackets = ts.getUidTxPackets(uid);

        TextView textTitle = (TextView) findViewById(R.id.netStats_title);
        TextView textRxBytes = (TextView) findViewById(R.id.netStats_rxBytes);
        TextView textTxBytes = (TextView) findViewById(R.id.netStats_txBytes);
        TextView textRxPkts = (TextView) findViewById(R.id.netStats_rxPkts);
        TextView textTxPkts = (TextView) findViewById(R.id.netStats_txPkts);

        textTitle.setText(title+"\n("+count+")");
        textRxBytes.setText("Received "+rxBytes.toString()+"B");
        textTxBytes.setText("Sent "+txBytes.toString()+"B");
        textRxPkts.setText("Recieved "+rxPackets.toString()+" packets");
        textTxPkts.setText("Sent "+txPackets.toString()+" packets");
    }

    private void showUsingTrafficStats(String title, int uid, int count) {
        TrafficStats ts = new TrafficStats();
        Long  rxBytes = ts.getUidRxBytes(uid);
        Long  txBytes = ts.getUidTxBytes(uid);
        Long  rxPackets = ts.getUidRxPackets(uid);
        Long  txPackets = ts.getUidTxPackets(uid);

        TextView textTitle = (TextView) findViewById(R.id.netStats_title);
        TextView textRxBytes = (TextView) findViewById(R.id.netStats_rxBytes);
        TextView textTxBytes = (TextView) findViewById(R.id.netStats_txBytes);
        TextView textRxPkts = (TextView) findViewById(R.id.netStats_rxPkts);
        TextView textTxPkts = (TextView) findViewById(R.id.netStats_txPkts);

        textTitle.setText(title);
        textRxBytes.setText("Received "+rxBytes.toString()+"B");
        textTxBytes.setText("Sent "+txBytes.toString()+"B");
        textRxPkts.setText("Recieved "+rxPackets.toString()+" packets");
        textTxPkts.setText("Sent "+txPackets.toString()+" packets");
    }
}
