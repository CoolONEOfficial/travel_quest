package ru.coolone.travelquest.ui.activities;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.seatgeek.placesautocomplete.PlacesAutocompleteTextView;
import com.seatgeek.placesautocomplete.model.AutocompleteResultType;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import lombok.val;
import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.fragments.AboutFrag_;
import ru.coolone.travelquest.ui.fragments.SettingsFrag_;
import ru.coolone.travelquest.ui.fragments.places.PlacesFrag;
import ru.coolone.travelquest.ui.fragments.places.PlacesFrag_;

@SuppressLint("Registered")
@EActivity
public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener,
        PlacesFrag_.SlidingUpPanelListener,
        PlacesFrag_.AutocompleteTextViewGetter {
    static final String TAG = MainActivity.class.getSimpleName();
    static final int NAV_MENU_DEFAULT_MENU_ID = R.id.nav_quests;
    private static final String KEY_MENU_ID = "menuId";

    // Preferences
    public static SharedPreferences settings;

    // Api client
    private static GoogleApiClient apiClient;

    // Firebase user
    public static FirebaseUser firebaseUser;

    // Action bar drawer toggle
    ActionBarDrawerToggle toggle;

    // Fragments array
    SparseArrayCompat<Fragment> fragmentArr = new SparseArrayCompat<>();
    int savedMenuId = -1;
    int currentMenuId;

    // Drawer layout
    @ViewById(R.id.drawer_layout)
    DrawerLayout drawer;

    // Navigation view
    @ViewById(R.id.nav_view)
    NavigationView navigationView;

    // Toolbar
    @ViewById(R.id.app_bar)
    Toolbar toolbar;
    @ViewById(R.id.app_bar_main)
    View toolbarMain;
    @ViewById(R.id.app_bar_title)
    TextView toolbarTitle;
    @ViewById(R.id.app_bar_layout)
    FrameLayout toolbarLayout;

    // Autocomplete place
    PlacesAutocompleteTextView autocompleteTextView;

    // Sliding up panel in PlacesFrag
    SlidingUpPanelLayout slidingPanel;

    @AfterViews
    void afterViews() {
        // Toolbar
        toolbar.setLayoutParams(
                new AppBarLayout.LayoutParams(
                        AppBarLayout.LayoutParams.MATCH_PARENT,
                        PlacesFrag.getActionBarHeight(this)
                ) {{
                    topMargin = getStatusBarHeight(MainActivity.this);
                }}
        );
        setSupportActionBar(toolbar);

        // Drawer layout
        toggle = new ActionBarDrawerToggle(
                this,
                drawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation view
        navigationView.setNavigationItemSelectedListener(this);
    }

    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Current locale: "
                + getLocale(this));
        setTheme(R.style.AppTheme_NoActionBar);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Strict mode
        StrictMode.setThreadPolicy(
                new StrictMode.ThreadPolicy
                        .Builder()
                        .detectAll()
                        .penaltyLog()
                        .build()
        );
        StrictMode.setVmPolicy(
                new StrictMode.VmPolicy
                        .Builder()
                        .detectLeakedSqlLiteObjects()
                        .detectLeakedClosableObjects()
                        .penaltyLog()
                        .build()
        );

        if (!isGooglePlayServicesAvailable(this))
            finish();

        // Switch last login method
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            if (!firebaseUser.isEmailVerified() && !firebaseUser.isAnonymous())
                // To confirm mail screen
                ConfirmMailActivity_.intent(this)
                        .start();
        } else {
            AuthChoiceActivity_.intent(this)
                    .start();
            finish();
        }

        // Get settings
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (settings.getBoolean("firstrun", true)) {
            settings.edit().putBoolean("firstrun", false).apply();

            // On first run
            startTour();
        }

        // Google api client
        apiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

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

        // Restore
        if (savedInstanceState != null) {
            savedMenuId = savedInstanceState.getInt(KEY_MENU_ID);
            for (val mFragId : FragmentId.values()) {
                val mFrag = getSupportFragmentManager().getFragment(
                        savedInstanceState,
                        Integer.toString(mFragId.ordinal())
                );

                if (mFrag != null) {
                    fragmentArr.put(mFragId.ordinal(), mFrag);
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_MENU_ID, currentMenuId);
        for (int mFragId = 0; mFragId < fragmentArr.size(); mFragId++) {
            val mKey = fragmentArr.keyAt(mFragId);
            val mFrag = fragmentArr.get(mKey);
            if (mFrag.isAdded()) {
                getSupportFragmentManager().putFragment(outState, Integer.toString(mKey), mFrag);
            }
        }
    }

    public static int getStatusBarHeight(Activity activity) {
        int result = (int) Math.ceil((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 24 : 25) * activity.getResources().getDisplayMetrics().density);
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        } else Log.e(TAG, "Not founded status bar height!");
        return result;
    }

    public static DocumentReference getQuestsRoot(String lang) {
        return FirebaseFirestore.getInstance()
                .collection(lang)
                .document("quests");
    }

    private void startTour() {
        IntroActivity_.intent(this).start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Retranslate results to frags
        for (int mFragId = 0; mFragId < fragmentArr.size(); mFragId++) {
            val mFragKey = fragmentArr.keyAt(mFragId);
            val mFragVal = fragmentArr.get(mFragKey);
            if (mFragVal != null)
                mFragVal.onActivityResult(requestCode, resultCode, data);
        }
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

    public static SupportLang getLocale(Context context) {
        // Get locale
        String localeStr;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            localeStr = context.getResources().getConfiguration().getLocales().get(0).getCountry();
        else
            localeStr = context.getResources().getConfiguration().locale.getCountry();

        // Check support
        for (val mLocale : SupportLang.values()) {
            Log.d(TAG, "Current locale: " + localeStr
                    + "\n\tmLocale: " + mLocale);
            if (localeStr.equals(mLocale.lang)) {
                Log.d(TAG, "Locale is supported!");
                return mLocale;
            }
        }

        Log.d(TAG, "Unsupported lang, use ENGLISH");

        return SupportLang.ENGLISH;
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
                    fragment = new AboutFrag_();
                    break;
                case QUESTS:
                    fragment = new PlacesFrag_();
                    break;
                case SETTINGS:
                    fragment = new SettingsFrag_();
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
        val menuId = savedMenuId != -1
                ? savedMenuId
                : NAV_MENU_DEFAULT_MENU_ID;
        onNavigationItemSelected(menuId);
        navigationView.getMenu().findItem(menuId).setChecked(true);

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
            val stateListAnimator = new StateListAnimator();
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
                    ? toolbarMain.getHeight()
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

    public void onNavigationItemSelected(int menuId) {
        if (menuId == R.id.nav_logout) {
            // Logout
            FirebaseAuth.getInstance().signOut();

            if (FirebaseAuth.getInstance().getCurrentUser() != null)
                Log.d(TAG, "Signout provider id: " + FirebaseAuth.getInstance().getCurrentUser().getProviderId());
            else Log.d(TAG, "Signout user is null!");

            AuthChoiceActivity_.intent(this)
                    .flags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                    .start();
            finish();
        } else {
            FragmentId fragId = null;
            switch (menuId) {
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
            if (currentMenuId != menuId) {
                // To fragment
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(
                                R.id.fragment_container,
                                getFragmentById(fragId)
                        )
                        .commit();

                if (slidingPanel != null)
                    updatePanelAlpha();
                else setToolbarAlpha(1.0f);
            }

            // --- Toolbar ---

            // Transparency
            setToolbarTransparent(
                    menuId == R.id.nav_quests,
                    menuId != R.id.nav_quests
            );

            // Colors
            val mapStyle = settings.getString(getResources().getString(R.string.settings_map_style_key), null);
            val mapBlack = "night".equalsIgnoreCase(mapStyle)
                    || "solarized".equalsIgnoreCase(mapStyle);
            setToolbarColors(
                    menuId == R.id.nav_quests
                            ? (
                            mapBlack
                                    ? Color.WHITE
                                    : Color.BLACK
                    )
                            : Color.WHITE
            );

            // Title
            int titleId = 0;
            switch (menuId) {
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
            if (menuId == R.id.nav_quests) {
                val autocompleteTextViewParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                autocompleteTextViewParams.setMarginEnd((int) getResources().getDimension(R.dimen.content_inset));
                if (menuId == currentMenuId)
                    toolbarLayout.removeView(autocompleteTextView);
                toolbarLayout.addView(autocompleteTextView, autocompleteTextViewParams);
            } else {
                toolbarLayout.removeView(autocompleteTextView);
            }

            drawer.closeDrawer(GravityCompat.START);

            currentMenuId = menuId;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        val id = item.getItemId();

        onNavigationItemSelected(id);

        return id != R.id.nav_logout;
    }

    private void setToolbarAlpha(float alphaF) {
        val alpha = (int) (alphaF * 255);
        autocompleteTextView.setAlpha(alphaF);
        toggle.getDrawerArrowDrawable().setAlpha(alpha);
        toolbar.setVisibility(
                alphaF < 0.1f
                        ? View.GONE
                        : View.VISIBLE
        );
    }

    @Override
    public void onPanelSlide(SlidingUpPanelLayout panel, float slideOffset) {
        setToolbarAlpha(
                slideOffset < panel.getAnchorPoint()
                        ? 1f :
                        (slideOffset > 0.5f
                                ? 0f
                                : 1f - ((slideOffset - panel.getAnchorPoint()) / (0.5f - panel.getAnchorPoint()))
                        )
        );
    }

    @Override
    public void onPanelCreate(SlidingUpPanelLayout panel) {
        slidingPanel = panel;
    }

    @Override
    public void onPanelStateChanged(
            SlidingUpPanelLayout panel,
            SlidingUpPanelLayout.PanelState previousState,
            SlidingUpPanelLayout.PanelState newState
    ) {
        updatePanelAlpha(newState);
    }

    void updatePanelAlpha() {
        updatePanelAlpha(slidingPanel.getPanelState());
    }

    void updatePanelAlpha(SlidingUpPanelLayout.PanelState state) {
        switch (state) {
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

    /**
     * Ids for @{@link Fragment}s
     */
    enum FragmentId {
        ABOUT,
        QUESTS,
        SETTINGS
    }

    /**
     * Languages info
     */
    public enum SupportLang {
        RUSSIAN("RU", "ru", R.string.add_details_tab_russian_title),
        ENGLISH("US", "en", R.string.add_details_tab_english_title);

        public final String lang;
        public final String yaTranslateLang;
        public final int titleId;

        SupportLang(String lang, String yaTranslateLang, int titleId) {
            this.lang = lang;
            this.yaTranslateLang = yaTranslateLang;
            this.titleId = titleId;
        }

        @Override
        public String toString() {
            return lang;
        }
    }
}
