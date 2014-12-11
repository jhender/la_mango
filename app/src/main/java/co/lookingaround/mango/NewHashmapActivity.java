package co.lookingaround.mango;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class NewHashmapActivity extends Activity {

    private Button saveButton;
    private Button deleteButton;
    private EditText hashmapText;
    private Hashmap hashmap;
    private String hashmapId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_hashmap);

        // Fetch the hashmapId from the Extra data
        if (getIntent().hasExtra("ID")) {
            hashmapId = getIntent().getExtras().getString("ID");
        }

        hashmapText = (EditText) findViewById(R.id.hashmap_text);
        saveButton = (Button) findViewById(R.id.saveButton);
        deleteButton = (Button) findViewById(R.id.deleteButton);

        if (hashmapId == null) {
            hashmap = new Hashmap();
            hashmap.setUuidString();
        } else {
            ParseQuery<Hashmap> query = Hashmap.getQuery();
            query.fromLocalDatastore();
            query.whereEqualTo("uuid", hashmapId);
            query.getFirstInBackground(new GetCallback<Hashmap>() {

                @Override
                public void done(Hashmap object, ParseException e) {
                    if (!isFinishing()) {
                        hashmap = object;
                        hashmapText.setText(hashmap.getTitle());
                        deleteButton.setVisibility(View.VISIBLE);
                    }
                }

            });

        }

        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                hashmap.setTitle(hashmapText.getText().toString());
                hashmap.setDraft(true);
                hashmap.setAuthor(ParseUser.getCurrentUser());
                hashmap.pinInBackground("ALL_HASHTAGS",
                        new SaveCallback() {

                            @Override
                            public void done(ParseException e) {
                                if (isFinishing()) {
                                    return;
                                }
                                if (e == null) {
                                    setResult(Activity.RESULT_OK);
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "Error saving: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            }

                        });
            }

        });

        deleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // The hashmap will be deleted eventually but will
                // immediately be excluded from query results.
                hashmap.deleteEventually();
                setResult(Activity.RESULT_OK);
                finish();
            }

        });

    }

}