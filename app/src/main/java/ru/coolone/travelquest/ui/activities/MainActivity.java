package ru.coolone.travelquest.ui.activities;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.fragments.about.AboutFragment;
import ru.coolone.travelquest.ui.fragments.quests.QuestsFragment;
import ru.coolone.travelquest.ui.fragments.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener,
        SlidingUpPanelLayout.PanelSlideListener {

    public static final String[] supportLangs = {
            "US",
            "RU"
    };
    static final String TAG = MainActivity.class.getSimpleName();
    static final int NAV_MENU_DEFAULT_ID = R.id.nav_quests;

    // Preferences
    public static SharedPreferences settings;

    // Api client
    private static GoogleApiClient apiClient;

    // Fragments array
    SparseArrayCompat<Fragment> fragmentArr = new SparseArrayCompat<>();

    // Drawer layout
    DrawerLayout drawer;

    // Preferences
    SharedPreferences prefs = null;

    // Toolbar
    Toolbar toolbar;
    ActionBar oldToolbar;
    View toolbarLayout;

    private void showAuthDialog() {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setCancelable(false);
        ad.setTitle(getString(R.string.alert_splash_title));
        ad.setMessage(getString(R.string.alert_splash_text));
        ad.setPositiveButton(getString(R.string.alert_splash_button_auth),
                (dialog, which) -> toActivity(LoginActivity.class)
        );
        ad.setNegativeButton(getString(R.string.alert_splash_button_anonymous),
                (dialog, which) -> {
                    final ProgressDialog progress = new ProgressDialog(this);
                    progress.setTitle(getString(R.string.login_progress));
                    progress.setCancelable(true);
                    progress.setOnCancelListener(
                            dialog1 -> toActivity(MainActivity.class)
                    );
                    progress.show();

                    FirebaseAuth.getInstance().signInAnonymously()
                            .addOnCompleteListener(this, task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "signInAnonymously:success");
                                } else {
                                    Log.w(TAG, "signInAnonymously:failure", task.getException());
                                    Toast.makeText(this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                                progress.dismiss();
                            });
                }
        );
        ad.show();
    }

    public MainActivity() {
    }

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
        for (String mLocale : supportLangs) {
            Log.d(TAG, "Current locale: " + localeStr
                    + "\n\tmLocale: " + mLocale);
            if (localeStr.equals(mLocale)) {
                Log.d(TAG, "Locale is supported!");
                return localeStr;
            }
        }

        return supportLangs[0];
    }

    public static GoogleApiClient getApiClient() {
        return apiClient;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(
                this,
                getResources().getString(R.string.error_google_api)
                        + '\n' + connectionResult.getErrorMessage(),
                Toast.LENGTH_LONG
        ).show();
        finishAffinity();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (prefs.getBoolean("firstrun", true)) {
            prefs.edit().putBoolean("firstrun", false).apply();

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(getString(R.string.alert_greetings_title))
                    .setMessage(getString(R.string.alert_greetings_text))
                    .setCancelable(false)
                    .setNegativeButton("OK",
                            (dialog, id) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void toActivity(Class<? extends Activity> activity) {
        // Set intent
        Intent intent = new Intent(this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        // Go to target activity
        startActivity(intent);
        finish();
    }

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
                        .build());
        StrictMode.setVmPolicy(
                new StrictMode.VmPolicy
                        .Builder()
                        .detectLeakedSqlLiteObjects()
                        .detectLeakedClosableObjects()
                        .penaltyLog()
                        .build());


        // Switch last login method
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (!user.isEmailVerified() && !user.isAnonymous())
                // To confirm mail screen
                toActivity(ConfirmMailActivity.class);
        } else {
            showAuthDialog();
        }

        setTheme(R.style.AppTheme_NoActionBar);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Preferences
        prefs = getSharedPreferences("ru.coolone.travelquest", MODE_PRIVATE);

        // Google api client
        apiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        // Get settings
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        oldToolbar = getSupportActionBar();
        setSupportActionBar(toolbar);
        toolbarLayout = findViewById(R.id.toolbar_layout);

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
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    Fragment getFragmentById(FragmentId fragmentId) {
        Fragment fragment = fragmentArr.get(fragmentId.ordinal());

        if (fragment == null) {
            switch (fragmentId) {
                case ABOUT:
                    fragment = new AboutFragment();
                    break;
                case QUESTS:
                    fragment = new QuestsFragment();
                    break;
                case SETTINGS:
                    fragment = new SettingsFragment();
                    break;
            }
            if (fragment != null)
                fragmentArr.put(fragmentId.ordinal(), fragment);
            else
                Log.e(TAG, "Unknown fragment id: " + fragmentId);
        }

        return fragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Open default navigation item
        onNavigationItemSelected(NAV_MENU_DEFAULT_ID);

        return true;
    }

    public void setToolbarTransparent(boolean transparent, boolean fragmentMargin) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Transparent
            toolbarLayout.getBackground().setAlpha(
                    transparent
                            ? 0
                            : 255
            );

            // Elevation
            StateListAnimator stateListAnimator = new StateListAnimator();
            stateListAnimator.addState(
                    new int[0],
                    ObjectAnimator.ofFloat(
                            toolbarLayout,
                            "elevation",
                            transparent
                                    ? 0.1f
                                    : 10.0f

                    )
            );
            toolbarLayout.setStateListAnimator(stateListAnimator);

            // Margin
            ((RelativeLayout.LayoutParams) findViewById(R.id.fragment_container).getLayoutParams())
                    .topMargin = fragmentMargin
                    ? toolbar.getHeight()
                    : 0;
        }
    }

    public void onNavigationItemSelected(int id) {
        if (id == R.id.nav_logout) {
            // Logout
            FirebaseAuth.getInstance().signOut();

            if (FirebaseAuth.getInstance().getCurrentUser() != null)
                Log.d(TAG, "Signout provider id: " + FirebaseAuth.getInstance().getCurrentUser().getProviderId());
            else Log.d(TAG, "Signout user is null!");

            showAuthDialog();
        } else {
            findViewById(R.id.toolbar_autocomplete_container).setVisibility(
                    id == R.id.nav_quests
                            ? View.VISIBLE
                            : View.GONE
            );

            setToolbarTransparent(
                    id == R.id.nav_quests,
                    id != R.id.nav_quests
            );

            // To fragment
            FragmentTransaction fragTrans = getSupportFragmentManager().beginTransaction();
            FragmentId fragId = null;
            switch (id) {
                case R.id.nav_quests:
                    fragId = FragmentId.QUESTS;
                    break;
                case R.id.nav_settings:
                    fragId = FragmentId.SETTINGS;
                    break;
                case R.id.nav_about:
                    fragId = FragmentId.ABOUT;
                    break;
            }
            fragTrans.replace(R.id.fragment_container,
                    getFragmentById(fragId))
                    .commit();

            // Update title
            int titleId = 0;
            switch (id) {
                case R.id.nav_settings:
                    titleId = R.string.nav_settings;
                    break;
                case R.id.nav_about:
                    titleId = R.string.nav_about;
            }
            if (titleId != 0)
                setTitle(getResources().getString(titleId));
            else
                setTitle("");

            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        onNavigationItemSelected(id);

        return id != R.id.nav_logout;
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        if (slideOffset > 0)
            toolbarLayout.getBackground().setAlpha(
                    (int) (slideOffset * 255)
            );
    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
    }

    // Fragments id
    enum FragmentId {
        ABOUT,
        QUESTS,
        SETTINGS
    }
}
