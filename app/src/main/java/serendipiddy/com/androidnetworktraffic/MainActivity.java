package serendipiddy.com.androidnetworktraffic;

import android.content.pm.ApplicationInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PackageManager pm = getPackageManager();
        List<ApplicationInfo> pinfo = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<String> applications = new ArrayList<String>();

        for (ApplicationInfo ai : pinfo) {
            StringBuilder sb = new StringBuilder();
            sb.append(ai.dataDir+" ");
            sb.append(ai.processName+" ");
            sb.append(ai.uid);
            applications.add(sb.toString());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,  R.id.applicationTextView, applications);
    }
}
