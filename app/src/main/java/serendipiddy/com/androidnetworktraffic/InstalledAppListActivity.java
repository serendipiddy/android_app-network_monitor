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
    public final static String EXTRA_UID = "androidnetworktraffic.extra.UID";
    public final static String EXTRA_LABEL = "androidnetworktraffic.extra.LABEL";
    private final String TAG = "InstalledAppList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installed_app_list);

    }

    @Override
    public void onBackPressed() {
        // Stop going back to check permissions page
    }

    @Override
    protected void onStart() {
        super.onStart();
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> pinfo = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        ArrayList<ApplicationItem> applications = new ArrayList<>();

        uid_map = new HashMap<Integer,Integer>();
        for (ApplicationInfo ai : pinfo) {
            ApplicationItem app = new ApplicationItem( ai.packageName, (String) pm.getApplicationLabel(ai), ai.uid);
            applications.add(app);
            if (uid_map.containsKey(ai.uid)){
                uid_map.put(ai.uid, uid_map.get(ai.uid)+1);
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
                return false;
            }
        });
        Toast.makeText(getBaseContext(), "Long press to select app", Toast.LENGTH_LONG).show();
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
