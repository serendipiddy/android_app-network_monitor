package serendipiddy.com.androidnetworktraffic;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by anselljord on 25/10/16.
 */
public class SummaryTools {
    private static final String TAG = "SummaryTools";

    public static final String headerString = "startTime endTime startTimeStamp endTimeStamp "
            + "rxPackets txPackets "
            + "rxBytes txBytes type\n";

    /**
     * Start the Task which writes the usage buckets (sb) to file.
     * @param sb
     * @param filename
     */
    public static void writeUsageToFile(final StringBuilder sb, final String filename, final String headerString, Context context) {
        Handler handler = new Handler();



        if (sb.length() <= 0) {
            return;
        }
        sb.insert(0, headerString);

        final File logfile = new File(context.getExternalFilesDir(""), filename);
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
}
