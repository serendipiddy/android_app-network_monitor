package serendipiddy.com.androidnetworktraffic;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class CheckPermissions extends AppCompatActivity {
    private final String TAG = "CheckPermissions";
    private final String USAGE_PERMISSION = AppOpsManager.OPSTR_GET_USAGE_STATS;
    private final String TELEPHONY_PERMISSION =  Manifest.permission.READ_PHONE_STATE;
    private final int REQUEST_TELEPHONY_PERMISSION = 980;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_permissions);

        // check permissions are available:
        boolean telephonyPerm = hasPermission(TELEPHONY_PERMISSION);
        boolean usagePerm = hasUsagePermission();
        if (!telephonyPerm) { // request telephony permissions
            Button button = (Button) findViewById(R.id.permissions_activity_access_button);
            button.setEnabled(true);
        }
        if (!usagePerm) { // guide user to the correct activity to grant this
            // enable button to start usage permission activity
            Button button = (Button) findViewById(R.id.permissions_activity_telephony_button);
            button.setEnabled(true);
        }

        if (telephonyPerm && usagePerm) {
            // if permissions are available continue on to regular activity
            startActivity(new Intent(this, InstalledAppList.class));
        }
    }

    /**
     * Sends user to the Activity for changing usage access permissions
     */
    public void startUsagePermissionActivity(View view) {
        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
    }

    /**
     * Request Telephony access from the user
     */
    public void requestTelephonyPermission(View view) {
        ActivityCompat.requestPermissions( this,
                new String[]{Manifest.permission.READ_PHONE_STATE},
                REQUEST_TELEPHONY_PERMISSION);
    }

    /**
     * Checks whether this app/context has the given request-able permission
     * @param permission
     * @return
     */
    private boolean hasPermission(String permission) {
        return ActivityCompat
                .checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Checks whether the context of an app has the given permission
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
}
