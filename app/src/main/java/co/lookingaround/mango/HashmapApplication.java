package co.lookingaround.mango;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class HashmapApplication extends Application {
	
	public static final String HASHMAP_GROUP_NAME = "ALL_HASHMAPS";
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// add Todo subclass
		ParseObject.registerSubclass(Hashmap.class);
		
		// enable the Local Datastore
		Parse.enableLocalDatastore(getApplicationContext());
		Parse.initialize(this, "hfhVukFAkDhY90KnLStI5k8phj2gtmaau05nIo5w", "Awt2mfIyP6wXdtjRLSWuyXPngRaOsgKaFNPwljxc");
		ParseUser.enableAutomaticUser();
		ParseACL defaultACL = new ParseACL();
		ParseACL.setDefaultACL(defaultACL, true);	
	}
	
	

}
