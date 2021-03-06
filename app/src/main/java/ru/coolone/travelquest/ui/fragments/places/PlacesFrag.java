package ru.coolone.travelquest.ui.fragments.places;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.seatgeek.placesautocomplete.DetailsCallback;
import com.seatgeek.placesautocomplete.PlacesAutocompleteTextView;
import com.seatgeek.placesautocomplete.model.PlaceDetails;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

import lombok.Getter;
import lombok.val;
import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.activities.MainActivity;
import ru.coolone.travelquest.ui.fragments.SettingsFrag;
import ru.coolone.travelquest.ui.fragments.places.details.PlaceDetailsFrag;

import static android.app.Activity.RESULT_OK;
import static ru.coolone.travelquest.ui.fragments.places.details.PlaceDetailsFrag.REQUEST_CODE_ADD_DETAILS;

@EFragment(R.layout.frag_places)
public class PlacesFrag extends Fragment
        implements OnMapReadyCallback,
        GoogleMap.OnPoiClickListener,
        PlaceDetailsFrag.FragmentListener {

    static final String TAG = PlacesFrag.class.getSimpleName();

    @FragmentArg
    LatLng startPosition;

    @FragmentArg
    String startPlaceId;
    Place startPlace;

    // Map
    @ViewById(R.id.places_map)
    public MapView mapView;
    public GoogleMap map;

    // Sliding layout
    @ViewById(R.id.places_sliding_container)
    FrameLayout slidingLayout;

    // Sliding panel
    @ViewById(R.id.places_sliding_panel)
    public SlidingUpPanelLayout slidingPanel;
    float slideOffset;
    SlidingUpPanelListener parentListener;
    SlidingUpPanelLayout.PanelSlideListener slideListener = new SlidingUpPanelLayout.PanelSlideListener() {
        @Override
        public void onPanelSlide(View panel, float slideOffset) {
            PlacesFrag.this.slideOffset = slideOffset;

            // Refresh photos size
            refreshPhotosSize(
                    slidingLayout.findViewById(R.id.details_photos_layout),
                    slidingLayout.findViewById(R.id.details_photos_scroll),
                    slideOffset
            );

            // Notify parent
            parentListener.onPanelSlide(slidingPanel, slideOffset);
        }

        @Override
        public void onPanelStateChanged(View panel,
                                        SlidingUpPanelLayout.PanelState previousState,
                                        SlidingUpPanelLayout.PanelState newState) {
            // Set map padding
            switch (newState) {
                case HIDDEN:
                    map.setPadding(0, getActionBarHeight(getActivity())
                                    + MainActivity.getStatusBarHeight(getActivity()),
                            0, 0);
                    break;
                case COLLAPSED:
                    map.setPadding(0, getActionBarHeight(getActivity())
                                    + MainActivity.getStatusBarHeight(getActivity()),
                            0, slidingLayout.findViewById(R.id.details_layout_header)
                                    .getHeight());
                    break;
                case EXPANDED:
                    if (!detailsLoaded) {
                        // Load details
                        detailsLoaded = true;
                        placeDetailsFrag.setPlaceId(placeDetailsFrag.getPlaceId());
                    }
                    break;
            }

            // Notify parent
            parentListener.onPanelStateChanged(slidingPanel, previousState, newState);
        }
    };

    public void invalidateSlidingPanel() {
        slideListener.onPanelSlide(slidingLayout, slideOffset);
    }

    // Fragment details
    private static final String FRAG_PLACE_DETAILS_ID = "placeDetails";
    public PlaceDetailsFrag placeDetailsFrag;

    // Place
    @Getter
    Place currentPlace;
    PointOfInterest currentPoi;

    private FragmentActivity context;

    // Details loaded
    boolean detailsLoaded = false;

    @AfterViews
    void afterViews() {
        // Sliding panel

        if (slidingLayout != null)
            slidingLayout.setLayoutParams(
                    new SlidingUpPanelLayout.LayoutParams(
                            ViewGroup.MarginLayoutParams.MATCH_PARENT,
                            ViewGroup.MarginLayoutParams.MATCH_PARENT

                    ) {{
                        topMargin = MainActivity.getStatusBarHeight(getActivity());
                    }}
            );

        // Hide quest panel if empty
        if (slidingLayout == null || slidingLayout.getChildCount() == 0)
            slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        // Add listeners
        slidingPanel.addPanelSlideListener(slideListener);
        parentListener.onPanelCreate(slidingPanel);

        // Autocomplete fragment
        val autocompleteTextView = ((AutocompleteTextViewGetter) getActivity())
                .getAutocompleteTextView();
        autocompleteTextView.setOnPlaceSelectedListener(
                place -> {
                    // Hide keyboard
                    MainActivity.hideKeyboard(getActivity());

                    // Create progress bar
                    ProgressBar progressBar = new ProgressBar(
                            getContext()
                    );
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, 100);
                    params.gravity = Gravity.CENTER;
                    progressBar.setLayoutParams(params);

                    // Add progress bar
                    mapView.addView(progressBar);

                    autocompleteTextView.getDetailsFor(place,
                            new DetailsCallback() {
                                @Override
                                public void onSuccess(PlaceDetails placeDetails) {
                                    Log.d(TAG, "Place selected:\nname: " + place.description + "\nunical id: " + place.place_id);

                                    // Go to place
                                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                            new LatLng(
                                                    placeDetails.geometry.location.lat,
                                                    placeDetails.geometry.location.lng
                                            ),
                                            getResources().getDimension(R.dimen.map_link_zoom)
                                    ));

                                    mapView.removeView(progressBar);
                                }

                                @Override
                                public void onFailure(Throwable throwable) {
                                    Toast.makeText(
                                            getContext(),
                                            getString(R.string.map_place_autocomplete_error),
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    // Remove progress bar
                                    mapView.removeView(progressBar);
                                }
                            }
                    );
                }
        );
    }

    static public float getPanelAnchoredOffset(Activity activity) {
        return activity.getResources().getDimension(R.dimen.details_photos_size_anchored)
                / MainActivity.getAppHeightWithoutBar(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (startPlaceId != null) {
            Places.GeoDataApi.getPlaceById(
                    MainActivity.getApiClient(),
                    startPlaceId
            ).setResultCallback(
                    places -> {
                        if (places.getStatus().isSuccess() &&
                                places.getCount() > 0) {
                            // Select place
                            startPlace = places.get(0);

                            if (map != null) {
                                map.moveCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                                startPlace.getLatLng(),
                                                getResources().getDimension(R.dimen.map_link_zoom)
                                        )
                                );
                            }
                        }
                    }
            );
        }

        if (savedInstanceState != null) {
            placeDetailsFrag = (PlaceDetailsFrag) context.getSupportFragmentManager().getFragment(
                    savedInstanceState,
                    FRAG_PLACE_DETAILS_ID
            );
            if (placeDetailsFrag != null) {
                placeDetailsFrag.setParentListener(this);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (placeDetailsFrag != null)
            context.getSupportFragmentManager().putFragment(
                    outState,
                    FRAG_PLACE_DETAILS_ID,
                    placeDetailsFrag
            );
    }

    @Override
    public void onAttach(Context context) {
        this.context = (FragmentActivity) context;
        parentListener = (SlidingUpPanelListener) getActivity();
        super.onAttach(context);
    }

    static public int getActionBarHeight(Activity activity) {
        TypedValue tv = new TypedValue();
        if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            return TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources().getDisplayMetrics());
        else Log.e(TAG, "Not founded actionbar height!");
        return 0;
    }

    private void refreshPhotosSize(
            LinearLayout photosLayout,
            HorizontalScrollView photosScroll,
            float slideOffset
    ) {
        float anchoredOffset = getPanelAnchoredOffset(getActivity());
        // Change photos height
        for (int mPhotoId = 0; mPhotoId < photosLayout.getChildCount(); mPhotoId++) {
            val height = (int) (
                    getResources().getDimension(R.dimen.details_photos_size_anchored) * (
                            1 - (
                                    Math.max(
                                            Math.min(slideOffset, 1.0f),
                                            anchoredOffset
                                    ) - anchoredOffset
                            )
                    )
            );

            photosLayout.getChildAt(mPhotoId).getLayoutParams().height = height;

            photosScroll.getLayoutParams().height = height;
        }
        photosLayout.requestLayout();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Map view
        MapView mapView = view.findViewById(R.id.places_map);
        mapView.onCreate(null);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());

        map = googleMap;

        // Type
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Padding
        map.setPadding(0, getActionBarHeight(getActivity())
                        + MainActivity.getStatusBarHeight(getActivity()),
                0, 0);

        // Style
        updateMapStyle();

        // To city
        map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                        startPosition != null ? startPosition :
                                startPlace != null ? startPlace.getLatLng() :
                                        MainActivity.getSettingsLatLng(
                                                MainActivity.settings,
                                                getString(R.string.settings_key_city_coord),
                                                SettingsFrag.SupportCity.NN.coord // Nizhny Novgorod is default
                                        ),
                        getResources().getDimension(
                                startPosition != null || startPlace != null
                                        ? R.dimen.map_link_zoom
                                        : R.dimen.map_zoom
                        )
                )
        );

        // Listen poi clicks
        map.setOnPoiClickListener(this);
    }

    private void updateMapStyle() {
        // Get style
        String mapStyle = MainActivity.settings.getString(
                getResources().getString(R.string.settings_key_map_style),
                "retro"
        );

        // Add prefix
        mapStyle = "map_" + mapStyle;

        // Set style
        try {
            if (!map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getActivity().getApplicationContext(),
                            getResources().getIdentifier(
                                    mapStyle,
                                    "raw",
                                    getActivity().getPackageName())
                    )))
                Log.e(TAG, "Parse style failed");
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Find style \"" + mapStyle + "\" failed", e);
        }
    }

    @Override
    public void onPoiClick(PointOfInterest poi) {
        Log.d(TAG, "Poi " + poi.name + " clicked!");

        currentPoi = poi;

        // Find place by id (from poi)
        Places.GeoDataApi.getPlaceById(MainActivity.getApiClient(), poi.placeId)
                .setResultCallback(places -> {
                    if (places.getStatus().isSuccess() &&
                            places.getCount() > 0) {
                        // Select place
                        currentPlace = places.get(0);
                        Log.d(TAG,
                                "Place selected:" + '\n' +
                                        "name: " + currentPlace.getName() + '\n' +
                                        "unical id: " + currentPlace.getId() + '\n' +
                                        "types: " + currentPlace.getPlaceTypes()
                        );

                        // Create details fragment
                        placeDetailsFrag = PlaceDetailsFrag.newInstance(currentPlace, getContext());
                        detailsLoaded = false;
                        placeDetailsFrag.setParentListener(PlacesFrag.this);

                        if (slidingPanel.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED) {

                            val slideListener = new SlidingUpPanelLayout.PanelSlideListener() {
                                @Override
                                public void onPanelSlide(View panel, float slideOffset) {
                                }

                                @Override
                                public void onPanelStateChanged(
                                        View panel,
                                        SlidingUpPanelLayout.PanelState previousState,
                                        SlidingUpPanelLayout.PanelState newState
                                ) {
                                    if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                                        changePlaceDetailsFrag(placeDetailsFrag);

                                        slidingPanel.removePanelSlideListener(this);
                                    }
                                }
                            };

                            slidingPanel.addPanelSlideListener(slideListener);
                            slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        } else
                            changePlaceDetailsFrag(placeDetailsFrag);
                    }
                });
    }

    void changePlaceDetailsFrag(PlaceDetailsFrag placeDetailsFrag) {
        // Set
        val fragTrans = getFragmentManager().beginTransaction();
        fragTrans.replace(
                R.id.places_sliding_container,
                placeDetailsFrag
        );
        fragTrans.commit();
    }


    @Override
    public void onResume() {
        super.onResume();

        // Hide quest panel if empty
        if (slidingLayout == null || slidingLayout.getChildCount() == 0)
            if (slidingPanel != null)
                slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        // Update map
        if (map != null) {
            updateMapStyle();
        }
    }

    @OnActivityResult(REQUEST_CODE_ADD_DETAILS)
    void onResult(int result) {
        if (result == RESULT_OK && placeDetailsFrag != null && currentPoi != null) {
            onPoiClick(currentPoi);
            slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
        }
    }

    @Override
    public void onQuestDetailsCreateView(
            @NonNull View view,
            ViewGroup container, Bundle savedInstanceState) {
        // Set on view sizes initialized
        view.post(
                () -> {
                    // Set panel height
                    slidingPanel.setPanelHeight(
                            view.findViewById(R.id.details_layout_header)
                                    .getHeight());

                    // Show if hidden
                    if (slidingPanel.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN)
                        slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }
        );

        // Set on click header
        view.findViewById(R.id.details_layout_header).setOnClickListener(
                v -> {
                    slidingPanel.setMinimumHeight(view.findViewById(R.id.details_layout_header)
                            .getHeight());

                    // Click header event
                    slidingLayout.callOnClick();
                }
        );

        // Set recycler is scrollable
        slidingPanel.setScrollableView(view.findViewById(R.id.details_details_scroll));

        // Set panel anchor point
        slidingPanel.setAnchorPoint(1f);
    }

    private void setPanelOffset() {
        // Set panel anchor point
        slidingPanel.setAnchorPoint(getPanelAnchoredOffset(getActivity()));
    }

    @Override
    public void onPhotosLoadingStarted() {
        if (slidingLayout.isActivated())
            setPanelOffset();
        else
            slidingLayout.post(this::setPanelOffset);
    }

    @Override
    public PlacesFrag getPlacesFrag() {
        return this;
    }

    public interface AutocompleteTextViewGetter {
        PlacesAutocompleteTextView getAutocompleteTextView();
    }

    public interface SlidingUpPanelListener {
        void onPanelCreate(SlidingUpPanelLayout panel);

        void onPanelSlide(SlidingUpPanelLayout panel, float slideOffset);

        void onPanelStateChanged(SlidingUpPanelLayout panel,
                                 SlidingUpPanelLayout.PanelState previousState,
                                 SlidingUpPanelLayout.PanelState newState);
    }
}
