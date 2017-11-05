package ru.coolone.travelquest.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.view.Menu;
import android.view.MenuItem;

import ru.coolone.travelquest.R;
import ru.coolone.travelquest.fragments.QuestsFragment;
import ru.coolone.travelquest.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static final String TAG = MainActivity.class.getSimpleName();

    public MainActivity() {
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

    // Session key
    public String sessionKey = "";

    // Drawer layout
    DrawerLayout drawer;

    static final String EXTRA_SESSION_KEY = "sessionKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get intent
        Intent intentInput = getIntent();

        // Get settings
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        // Session key
        sessionKey = intentInput.getStringExtra(EXTRA_SESSION_KEY); // from intent (default)
        if (sessionKey == null || sessionKey.isEmpty())
            sessionKey = settings.getString(
                    getResources().getString(R.string.settings_map_style_key),
                    ""); // from settings

        if (sessionKey.isEmpty()) {
            // Go to login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer layout
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation view
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Fragments array
        fragmentArr.put(FragmentId.QUESTS.ordinal(), QuestsFragment.newInstance());
        fragmentArr.put(FragmentId.SETTINGS.ordinal(), SettingsFragment.newInstance());
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
                .replace(R.id.fragment_container, fragmentArr.get(FragmentId.QUESTS.ordinal()))
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
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

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
