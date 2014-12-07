package co.lookingaround.mango;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;
import com.parse.PushService;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Parse Backend Login
        Parse.initialize(this, "hfhVukFAkDhY90KnLStI5k8phj2gtmaau05nIo5w", "Awt2mfIyP6wXdtjRLSWuyXPngRaOsgKaFNPwljxc");
        // Also in this method, specify a default Activity to handle push notifications
        PushService.setDefaultPushCallback(this, MainActivity.class);

        //Parse Test Connection Code. Can remove else it will put an object every single login.
        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();

        //Parse Analytics
        ParseAnalytics.trackAppOpened(getIntent());
//        ParseAnalytics.trackAppOpenedInBackground(android.content.Intent);
//        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
