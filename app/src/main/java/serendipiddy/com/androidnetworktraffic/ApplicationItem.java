package serendipiddy.com.androidnetworktraffic;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Class holding the package information of an App, for displaying in a list
 */
class ApplicationItem implements Comparable {
    public final String packageName;
    public final String appLabel;
    public final int uid;
    public final String TAG = "ApplicationItem";

    public ApplicationItem(String packageName, String label, int uid) {
        this.packageName = packageName;
        this.appLabel = label;
        this.uid = uid;
    }

    public Drawable getIcon(PackageManager pm) {
        Drawable icon = null;
        try {
            icon = pm.getApplicationIcon(this.packageName);
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.toString());
        }
        return icon;
    }

    public long getInstallTime(PackageManager pm) {
        // Calendar cal = Calendar.getInstance();
        long time = -1;
        try {
            PackageInfo pi = pm.getPackageInfo(packageName,0);
            // cal.setTimeInMillis( pi.firstInstallTime );
            time = pi.firstInstallTime;
        }
        catch(PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.toString());
        }
        return time;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof ApplicationItem) {
            ApplicationItem other = (ApplicationItem) o;
            return this.appLabel.compareTo(other.appLabel);
        }
        else
            return 0;
    }
}