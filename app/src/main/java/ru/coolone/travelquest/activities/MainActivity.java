package ru.coolone.travelquest.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import ru.coolone.travelquest.R;
import ru.coolone.travelquest.activities.LoginActivity;
import ru.coolone.travelquest.fragments.QuestsFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

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

    // Preferences
    public static SharedPreferences preferences;

    // Session key
    public String sessionKey = "";

    // Fragments array
    SparseArrayCompat<Fragment> fragmentArr = new SparseArrayCompat<>();

    // Drawer layout
    DrawerLayout drawer;

    static final String EXTRA_SESSION_KEY = "sessionKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intentInput = getIntent();

        // Get preferences
        preferences = getPreferences(MODE_PRIVATE);

        // Session key
        sessionKey = intentInput.getStringExtra(EXTRA_SESSION_KEY); // from bundle (default)
        if(sessionKey == null || sessionKey.isEmpty())
            sessionKey = preferences.getString(EXTRA_SESSION_KEY, ""); // from properties

        if(sessionKey.isEmpty())
        {
            // Go to login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        else
            Toast.makeText(this, "sessionKey: " + sessionKey, Toast.LENGTH_LONG).show();

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
        fragmentArr.put(FragmentId.QUESTS.ordinal(), new QuestsFragment());
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
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // TODO: handlers

        FragmentTransaction fragTrans = getSupportFragmentManager().beginTransaction();
        switch (id)
        {
            case R.id.nav_quests:
                fragTrans.replace(R.id.fragment_container, fragmentArr.get(FragmentId.QUESTS.ordinal()));
        }
        fragTrans.commit();

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
