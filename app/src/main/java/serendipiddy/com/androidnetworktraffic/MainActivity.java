package serendipiddy.com.androidnetworktraffic;

import android.content.pm.ApplicationInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PackageManager pm = getPackageManager();
        List<ApplicationInfo> pinfo = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        ArrayList<String> applications = new ArrayList<String>();

        for (ApplicationInfo ai : pinfo) {
            StringBuilder sb = new StringBuilder();
            sb.append(ai.packageName+" ");
            sb.append(ai.uid);
            applications.add(sb.toString());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,  R.layout.application_text, applications);
        ListView applicationListView = (ListView) findViewById(R.id.applicationListView);
        applicationListView.setAdapter(adapter);
    }

    private class ApplicationItem implements Comparable {
        public final String uid;
        public final String packageName;

        public ApplicationItem(String packageName, String uid) {
            this.packageName = packageName;
            this.uid = uid;
        }

        @Override
        public int compareTo(Object o) {
            if (o instanceof ApplicationItem) {
                ApplicationItem other = (ApplicationItem) o;
                return this.packageName.compareTo(other.packageName);
            }
            else
                return 0;
        }
    }
}
