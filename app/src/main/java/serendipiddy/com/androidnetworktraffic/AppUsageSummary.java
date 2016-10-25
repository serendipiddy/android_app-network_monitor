package serendipiddy.com.androidnetworktraffic;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class AppUsageSummary extends AppCompatActivity {

    private final String currentAppName = "";
    private final StringBuilder currentAppWifiResults = new StringBuilder();
    private final StringBuilder currentAppMobileResults = new StringBuilder();
    private String OUTPUT_DIR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_usage_summary);

        OUTPUT_DIR = "Android/data/serendipiddy.com"+getString(R.string.app_name)+"/";

        // get the intent used to start activity, extracting the values needed

        // perform the usage extraction

        // populate the fields on screen
    }

    /**
     * Creates the options menu (top right)
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Handles menu button presses
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_current_app_button:
                Toast.makeText(getBaseContext(), "(Unimplemented)", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.save_usage_button:
                if (currentAppName == null) {
                    Toast.makeText(getBaseContext(), "Please Select An App", Toast.LENGTH_SHORT).show();
                }
                else {
                    SummaryTools.writeUsageToFile(currentAppWifiResults, currentAppName + ".wifi.usage", SummaryTools.headerString, this);
                    SummaryTools.writeUsageToFile(currentAppMobileResults, currentAppName  + ".mobile.usage", SummaryTools.headerString, this);
                    Toast.makeText(getBaseContext(), "Saved usage to: " + OUTPUT_DIR + currentAppName, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.open_app_settings_button:
                startActivity(new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));
                Toast.makeText(getBaseContext(), "App Permissions\nEnable/Disable telephony", Toast.LENGTH_LONG).show();
                return true;
            case R.id.open_usage_access_button:
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
