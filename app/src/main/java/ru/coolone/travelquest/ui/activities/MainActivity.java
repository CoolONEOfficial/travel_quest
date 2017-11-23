package ru.coolone.travelquest.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.fragments.quests.QuestsFragment;
import ru.coolone.travelquest.ui.fragments.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {

    static final String TAG = MainActivity.class.getSimpleName();

    // Arguments
    enum ArgKeys {
        TITLE("user");

        private final String val;

        ArgKeys(String val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return val;
        }
    }

    public MainActivity() {
    }

    // Api client
    private static GoogleApiClient apiClient;

    // Firebase user
    public FirebaseUser user;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    // Fragments id
    enum FragmentId {
        ABOUT,
        ACHIEVEMENTS,
        FRIENDS,
        MESSAGES,
        PROFILE,
        QUESTS,
        SETTINGS
    }

    static final FragmentId FRAGMENT_DEFAULT_ID = FragmentId.QUESTS;

    // Fragments array
    SparseArrayCompat<Fragment> fragmentArr = new SparseArrayCompat<>();

    // Default fragment
    static final FragmentId FRAGMENT_DEFAULT = FragmentId.QUESTS;

    // Preferences
    public static SharedPreferences settings;

    public static void setDefaultSettings(Context context) {
        // Map style
        settings.edit()
                .putString(context.getResources().getString(R.string.settings_map_style_key),
                        context.getResources().getStringArray(R.array.settings_map_style_values)[0])
                .apply();
    }

    // Drawer layout
    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "Current locale: "
                + getLocaleStr(this));

        // Strict mode
        StrictMode.setThreadPolicy(
                new StrictMode.ThreadPolicy
                        .Builder()
                        .detectAll()
                        .penaltyLog()
//                        .penaltyDeath()
                        .build());
        StrictMode.setVmPolicy(
                new StrictMode.VmPolicy
                        .Builder()
                        .detectLeakedSqlLiteObjects()
                        .detectLeakedClosableObjects()
                        .penaltyLog()
//                        .penaltyDeath()
                        .build());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = FirebaseAuth.getInstance().getCurrentUser();

        // User not authenticated?
        if (user == null) {
            // Go to login
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        // Init task
        InitTask initTask = new InitTask(this, this);
        initTask.execute();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer layout
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation view
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Fragments array
        fragmentArr.put(FragmentId.QUESTS.ordinal(), QuestsFragment.newInstance());
        fragmentArr.put(FragmentId.SETTINGS.ordinal(), SettingsFragment.newInstance());
    }

    private class InitTask extends AsyncTask<Void, Void, Void> {

        AppCompatActivity parent;
        GoogleApiClient.OnConnectionFailedListener connectionFailedListener;

        InitTask(
                AppCompatActivity parent,
                GoogleApiClient.OnConnectionFailedListener connectionFailedListener
        ) {
            this.parent = parent;
            this.connectionFailedListener = connectionFailedListener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Google api client
            apiClient = new GoogleApiClient
                    .Builder(parent)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .enableAutoManage(parent, connectionFailedListener)
                    .build();

            // Get settings
            settings = PreferenceManager.getDefaultSharedPreferences(parent);

            // Session key
            user = FirebaseAuth.getInstance().getCurrentUser();

            return null;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Set default fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragmentArr.get(FRAGMENT_DEFAULT_ID.ordinal()))
                .commit();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // To fragment
        FragmentTransaction fragTrans = getSupportFragmentManager().beginTransaction();
        FragmentId fragId = FRAGMENT_DEFAULT;
        switch (id) {
            case R.id.nav_quests:
                fragId = FragmentId.QUESTS;
                break;
            case R.id.nav_settings:
                fragId = FragmentId.SETTINGS;
                break;
        }
        fragTrans.replace(R.id.fragment_container,
                fragmentArr.get(fragId.ordinal()))
                .commit();

        switch (id) {
            case R.id.nav_settings:
                setTitle(getResources().getString(R.string.title_frag_settings));
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static final String[] supportLangs = {
            "US",
            "RU"
    };

    static public int getAppHeight(Activity activity) {
        return activity.findViewById(R.id.fragment_container).getHeight();
    }

    static public int getAppHeightWithoutBar(Activity activity) {
        return getAppHeight(activity) - getHeightBar(activity);
    }

    static public int getHeightBar(Activity activity) {
        return activity.findViewById(R.id.toolbar).getHeight();
    }

    public static String getLocaleStr(Context context) {
        // Get locale
        String localeStr;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            localeStr = context.getResources().getConfiguration().getLocales().get(0).getCountry();
        else
            localeStr = context.getResources().getConfiguration().locale.getCountry();

        // Check support
        boolean support = false;
        for (String mLocale : supportLangs) {
            if (localeStr == mLocale) {
                support = true;
                break;
            }
        }
        if (!support)
            return supportLangs[0];

        return localeStr;
    }

    public static GoogleApiClient getApiClient() {
        return apiClient;
    }
}
