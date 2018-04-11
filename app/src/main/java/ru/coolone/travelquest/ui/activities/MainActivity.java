package ru.coolone.travelquest.ui.activities;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
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
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.seatgeek.placesautocomplete.PlacesAutocompleteTextView;
import com.seatgeek.placesautocomplete.model.AutocompleteResultType;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.fragments.about.AboutFragment;
import ru.coolone.travelquest.ui.fragments.quests.QuestsFragment;
import ru.coolone.travelquest.ui.fragments.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener,
        QuestsFragment.SlidingUpPanelListener,
        QuestsFragment.AutocompleteTextViewGetter {

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

    // Action bar drawer toggle
    ActionBarDrawerToggle toggle;

    // Fragments array
    SparseArrayCompat<Fragment> fragmentArr = new SparseArrayCompat<>();

    // Drawer layout
    DrawerLayout drawer;

    // Toolbar
    Toolbar toolbar;
    View toolbarMain;
    TextView toolbarTitle;
    FrameLayout toolbarLayout;

    // Autocomplete place
    PlacesAutocompleteTextView autocompleteTextView;

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
        return activity.findViewById(R.id.app_bar).getHeight();
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

        if (settings.getBoolean("firstrun", true)) {
            settings.edit().putBoolean("firstrun", false).apply();

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
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        toolbarMain = findViewById(R.id.app_bar_main);
        toolbarTitle = findViewById(R.id.app_bar_title);
        toolbarLayout = findViewById(R.id.app_bar_layout);

        // Drawer layout
        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this,
                drawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation view
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Autocomplete place
        autocompleteTextView = new PlacesAutocompleteTextView(
                this,
                getString(R.string.GOOGLE_MAPS_API_KEY)
        );
        autocompleteTextView.setLocationBiasEnabled(true);
        autocompleteTextView.setRadiusMeters(10000L);
        autocompleteTextView.setResultType(AutocompleteResultType.GEOCODE);
        autocompleteTextView.setLanguageCode("ru");
        autocompleteTextView.setHint(getString(R.string.place_autocomplete_hint));
        Location targetLocation = new Location("");
        targetLocation.setLatitude(56.326887);
        targetLocation.setLongitude(44.005986);
        autocompleteTextView.setCurrentLocation(targetLocation);
        autocompleteTextView.setLocationBiasEnabled(true);
        autocompleteTextView.setInputType(InputType.TYPE_CLASS_TEXT);
        autocompleteTextView.setMaxLines(1);
        autocompleteTextView.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        autocompleteTextView.showClearButton(s.length() > 0);
                    }
                }
        );
    }

    private void setToolbarColors(int color) {
        // Autocomplete text view
        autocompleteTextView.setTextColor(color);
        ColorStateList colorStateList = ColorStateList.valueOf(color);
        ViewCompat.setBackgroundTintList(autocompleteTextView, colorStateList);
        autocompleteTextView.setHintTextColor(color);

        // App bar
        toggle.getDrawerArrowDrawable().setColor(color);
        toolbarTitle.setTextColor(color);
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
            toolbarMain.getBackground().setAlpha(
                    transparent
                            ? 0
                            : 255
            );

            // Elevation
            StateListAnimator stateListAnimator = new StateListAnimator();
            stateListAnimator.addState(
                    new int[0],
                    ObjectAnimator.ofFloat(
                            toolbarMain,
                            "elevation",
                            transparent
                                    ? 0.1f
                                    : 10.0f

                    )
            );
            toolbarMain.setStateListAnimator(stateListAnimator);

            // Margin
            ((RelativeLayout.LayoutParams) findViewById(R.id.fragment_container).getLayoutParams())
                    .topMargin = fragmentMargin
                    ? toolbar.getHeight()
                    : 0;
        }
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(getText(titleId));
    }

    @Override
    public void setTitle(CharSequence title) {
        toolbarTitle.setText(title);

        super.setTitle(title);
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

            // --- Toolbar ---

            // Transparency
            setToolbarTransparent(
                    id == R.id.nav_quests,
                    id != R.id.nav_quests
            );

            // Colors
            final String mapStyle = settings.getString(getResources().getString(R.string.settings_map_style_key), null);
            final boolean mapBlack = "night".equalsIgnoreCase(mapStyle)
                    || "solarized".equalsIgnoreCase(mapStyle);
            setToolbarColors(
                    id == R.id.nav_quests
                            ? (
                            mapBlack
                                    ? Color.WHITE
                                    : Color.BLACK
                    )
                            : Color.WHITE
            );

            // Title
            int titleId = 0;
            switch (id) {
                case R.id.nav_settings:
                    titleId = R.string.nav_settings;
                    break;
                case R.id.nav_about:
                    titleId = R.string.nav_about;
            }
            toolbarTitle.setVisibility(
                    titleId != 0
                            ? View.VISIBLE
                            : View.GONE
            );
            if (titleId != 0) {
                setTitle(getResources().getString(titleId));
            }

            // Autocomplete place text
            if (id == R.id.nav_quests) {
                final FrameLayout.LayoutParams autocompleteTextViewParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                autocompleteTextViewParams.setMarginEnd((int) getResources().getDimension(R.dimen.content_inset));
                toolbarLayout.addView(autocompleteTextView, autocompleteTextViewParams);
            } else {
                toolbarLayout.removeView(autocompleteTextView);
            }

            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        onNavigationItemSelected(id);

        return id != R.id.nav_logout;
    }

    private void setToolbarAlpha(float alphaF) {
        int alpha = (int) (alphaF * 255);
        autocompleteTextView.setAlpha(alphaF);
        toggle.getDrawerArrowDrawable().setAlpha(alpha);
    }

    @Override
    public void onPanelSlide(SlidingUpPanelLayout panel, float slideOffset) {
        if (slideOffset >= panel.getAnchorPoint() && slideOffset <= 0.5f) {
            slideOffset -= panel.getAnchorPoint();
            slideOffset /= 0.5f - panel.getAnchorPoint();

            setToolbarAlpha(1.0f - slideOffset);
        }
    }

    @Override
    public void onPanelStateChanged(SlidingUpPanelLayout panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
        switch (newState) {
            case HIDDEN:
            case ANCHORED:
            case COLLAPSED:
                setToolbarAlpha(1.0f);
                break;
            case EXPANDED:
                setToolbarAlpha(0.0f);
                break;
        }
    }

    @Override
    public PlacesAutocompleteTextView getAutocompleteTextView() {
        return autocompleteTextView;
    }

    // Fragments id
    enum FragmentId {
        ABOUT,
        QUESTS,
        SETTINGS
    }
}
