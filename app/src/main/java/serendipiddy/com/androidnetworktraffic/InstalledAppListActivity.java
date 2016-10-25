package serendipiddy.com.androidnetworktraffic;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstalledAppListActivity extends AppCompatActivity {

    private Map<Integer, Integer> uid_map;
    public final static String EXTRA_NAME = "androidnetworktraffic.extra.NAME";
    public final static String EXTRA_LABEL = "androidnetworktraffic.extra.LABEL";
    public final static String EXTRA_UID = "androidnetworktraffic.extra.UID";
    public final static String EXTRA_INSTALL_TIME = "androidnetworktraffic.extra.INSTALL_TIME";
    private final String TAG = "InstalledAppList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installed_app_list_);
        Toast.makeText(this, "Long press to select app", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onBackPressed() {
        // Stop going back to check permissions page
        // TODO exit app
    }

    @Override
    protected void onStart() {
        super.onStart();
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> pinfo = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        ArrayList<ApplicationItem> applications = new ArrayList<>();

        uid_map = new HashMap<Integer,Integer>();
        for (ApplicationInfo ai : pinfo) {
            // save the app info
            ApplicationItem app = new ApplicationItem( ai.packageName, (String) pm.getApplicationLabel(ai), ai.uid);
            applications.add(app);
            // count how many times this UID was found
            if (uid_map.containsKey(ai.uid)){
                uid_map.put(ai.uid, uid_map.get(ai.uid)+1);
                Log.d(TAG, "More than one of this UID exists (UID:"+ai.uid+", count:"+uid_map.get(ai.uid)+" name:"+ai.packageName+")");
            }
            else {
                uid_map.put(ai.uid,1);
            }
        }

        Collections.sort(applications);

        PackageArrayAdapter adapter = new PackageArrayAdapter(this,  R.layout.application_text, applications);
        ListView applicationListView = (ListView) findViewById(R.id.installedAppListView);
        applicationListView.setAdapter(adapter);
        applicationListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ApplicationItem ai = (ApplicationItem)  parent.getAdapter().getItem(position);
                Intent intent = new Intent(getBaseContext(), AppUsageSummary.class);
                intent.putExtra(EXTRA_NAME,ai.packageName);
                intent.putExtra(EXTRA_LABEL,ai.appLabel);
                intent.putExtra(EXTRA_UID,ai.uid);
                intent.putExtra(EXTRA_INSTALL_TIME,ai.getInstallTime(getPackageManager()));
                startActivity(intent);
                return false;
            }
        });

        ProgressBar pb = (ProgressBar) findViewById(R.id.loading_apps_list_bar);
        pb.setVisibility(View.GONE);

    }

    /**
     * Adapter for displaying package information
     */
    private class PackageArrayAdapter extends ArrayAdapter<ApplicationItem> {

        public PackageArrayAdapter(Context context, int layout, ArrayList<ApplicationItem> pkgs){
            super(context, layout, pkgs);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ApplicationItem ai = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.application_item, parent, false);
            }
            // TODO display package icon
            TextView pkgName = (TextView) convertView.findViewById(R.id.package_list_name);
            pkgName.setText(ai.appLabel);
            return convertView;
        }
    }
}
