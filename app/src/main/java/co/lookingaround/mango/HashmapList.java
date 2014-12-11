package co.lookingaround.mango;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.ui.ParseLoginBuilder;


import java.util.List;

/**
 * Created by jianhui.ho on 12/8/2014.
 */
public class HashmapList extends ActionBarActivity {

    private static final int LOGIN_ACTIVITY_CODE = 100;
    private static final int EDIT_ACTIVITY_CODE = 200;

    // Adapter for the Hashmaps Parse Query
    private ParseQueryAdapter<Hashmap> hashmapListAdapter;

    private LayoutInflater inflater;

    // For showing empty and non-empty hashmap views
    private ListView hashmapListView;
    private LinearLayout noHashmapsView;

    private TextView loggedInInfoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hashmap_list);

        // Set up the views
        hashmapListView = (ListView) findViewById(R.id.hashmap_list_view);
        noHashmapsView = (LinearLayout) findViewById(R.id.no_hashmaps_view);
        hashmapListView.setEmptyView(noHashmapsView);
        loggedInInfoView = (TextView) findViewById(R.id.loggedin_info);

        // Set up the Parse query to use in the adapter
        ParseQueryAdapter.QueryFactory<Hashmap> factory = new ParseQueryAdapter.QueryFactory<Hashmap>() {
            public ParseQuery<Hashmap> create() {
                ParseQuery<Hashmap> query = Hashmap.getQuery();
                query.orderByDescending("createdAt");
                query.fromLocalDatastore();
                return query;
            }
        };
        // Set up the adapter
        inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        hashmapListAdapter = new HashTagListAdapter(this, factory);

        // Attach the query adapter to the view
        ListView hashmapListView = (ListView) findViewById(R.id.hashmap_list_view);
        hashmapListView.setAdapter(hashmapListAdapter);

        hashmapListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Hashmap hashmap = hashmapListAdapter.getItem(position);
                openEditView(hashmap);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if we have a real user
        if (!ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())) {
            // Sync data to Parse
            syncHashmapsToParse();
            // Update the logged in label info
            updateLoggedInInfo();
        }
    }

    private void updateLoggedInInfo() {
        if (!ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())) {
            ParseUser currentUser = ParseUser.getCurrentUser();
            loggedInInfoView.setText(getString(R.string.logged_in,
                    currentUser.getString("name")));
        } else {
            loggedInInfoView.setText(getString(R.string.not_logged_in));
        }
    }

    private void openEditView(Hashmap hashmap) {
        Intent i = new Intent(this, NewHashmapActivity.class);
        i.putExtra("ID", hashmap.getUuidString());
        startActivityForResult(i, EDIT_ACTIVITY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // An OK result means the pinned dataset changed or
        // log in was successful
        if (resultCode == RESULT_OK) {
            if (requestCode == EDIT_ACTIVITY_CODE) {
                // Coming back from the edit view, update the view
                hashmapListAdapter.loadObjects();
            } else if (requestCode == LOGIN_ACTIVITY_CODE) {
                // If the user is new, sync data to Parse,
                // else get the current list from Parse
                if (ParseUser.getCurrentUser().isNew()) {
                    syncHashmapsToParse();
                } else {
                    loadFromParse();
                }
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.hashmap_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_new) {
            // Make sure there's a valid user, anonymous
            // or regular
            if (ParseUser.getCurrentUser() != null) {
                startActivityForResult(new Intent(this, NewHashmapActivity.class),
                        EDIT_ACTIVITY_CODE);
            }
        }

        if (item.getItemId() == R.id.action_sync) {
            syncHashmapsToParse();
        }

        if (item.getItemId() == R.id.action_logout) {
            // Log out the current user
            ParseUser.logOut();
            // Create a new anonymous user
            ParseAnonymousUtils.logIn(null);
            // Update the logged in label info
            updateLoggedInInfo();
            // Clear the view
            hashmapListAdapter.clear();
            // Unpin all the current objects
            ParseObject
                    .unpinAllInBackground("ALL_HASHTAGS");
        }

//        if (item.getItemId() == R.id.action_login) {
            ParseLoginBuilder builder = new ParseLoginBuilder(this);
            startActivityForResult(builder.build(), LOGIN_ACTIVITY_CODE);
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean realUser = !ParseAnonymousUtils.isLinked(ParseUser
                .getCurrentUser());
        menu.findItem(R.id.action_login).setVisible(!realUser);
        menu.findItem(R.id.action_logout).setVisible(realUser);
        return true;
    }

    private void syncHashmapsToParse() {
        // We could use saveEventually here, but we want to have some UI
        // around whether or not the draft has been saved to Parse
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if ((ni != null) && (ni.isConnected())) {
            if (!ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())) {
                // If we have a network connection and a current logged in user,
                // sync the
                // hashmaps

                // In this app, local changes should overwrite content on the
                // server.

                ParseQuery<Hashmap> query = Hashmap.getQuery();
                query.fromPin("ALL_HASHTAGS");
                query.whereEqualTo("isDraft", true);
                query.findInBackground(new FindCallback<Hashmap>() {
                    public void done(List<Hashmap> hashmaps, ParseException e) {
                        if (e == null) {
                            for (final Hashmap hashmap : hashmaps) {
                                // Set is draft flag to false before
                                // syncing to Parse
                                hashmap.setDraft(false);
                                hashmap.saveInBackground(new SaveCallback() {

                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            // Let adapter know to update view
                                            if (!isFinishing()) {
                                                hashmapListAdapter
                                                        .notifyDataSetChanged();
                                            }
                                        } else {
                                            // Reset the is draft flag locally
                                            // to true
                                            hashmap.setDraft(true);
                                        }
                                    }

                                });

                            }
                        } else {
                            Log.i("HashmapListActivity",
                                    "syncHashmapsToParse: Error finding pinned hashmaps: "
                                            + e.getMessage());
                        }
                    }
                });
            } else {
//                If we have a network connection but no logged in user, direct
//                the person to log in or sign up.
                ParseLoginBuilder builder = new ParseLoginBuilder(this);
                startActivityForResult(builder.build(), LOGIN_ACTIVITY_CODE);
            }
        } else {
            // If there is no connection, let the user know the sync didn't
            // happen
            Toast.makeText(
                    getApplicationContext(),
                    "Your device appears to be offline. Some hashmaps may not have been synced to Parse.",
                    Toast.LENGTH_LONG).show();
        }

    }

    private void loadFromParse() {
        ParseQuery<Hashmap> query = Hashmap.getQuery();
        query.whereEqualTo("author", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<Hashmap>() {
            public void done(List<Hashmap> hashmaps, ParseException e) {
                if (e == null) {
                    ParseObject.pinAllInBackground((List<Hashmap>) hashmaps,
                            new SaveCallback() {
                                public void done(ParseException e) {
                                    if (e == null) {
                                        if (!isFinishing()) {
                                            hashmapListAdapter.loadObjects();
                                        }
                                    } else {
                                        Log.i("HashmapListActivity",
                                                "Error pinning hashmaps: "
                                                        + e.getMessage());
                                    }
                                }
                            });
                } else {
                    Log.i("HashmapListActivity",
                            "loadFromParse: Error finding pinned hashmaps: "
                                    + e.getMessage());
                }
            }
        });
    }

    private class HashTagListAdapter extends ParseQueryAdapter<Hashmap> {

        public HashTagListAdapter(Context context,
                               QueryFactory<Hashmap> queryFactory) {
            super(context, queryFactory);
        }

        @Override
        public View getItemView(Hashmap hashmap, View view, ViewGroup parent) {
            ViewHolder holder;
            if (view == null) {
                view = inflater.inflate(R.layout.list_item_hashmap, parent, false);
                holder = new ViewHolder();
                holder.hashmapTitle = (TextView) view
                        .findViewById(R.id.hashmap_title);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            TextView hashmapTitle = holder.hashmapTitle;
            hashmapTitle.setText(hashmap.getTitle());
            if (hashmap.isDraft()) {
                hashmapTitle.setTypeface(null, Typeface.ITALIC);
            } else {
                hashmapTitle.setTypeface(null, Typeface.NORMAL);
            }
            return view;
        }
    }

    private static class ViewHolder {
        TextView hashmapTitle;
    }
}
