package ru.coolone.travelquest.ui.fragments.quests;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.activities.MainActivity;
import ru.coolone.travelquest.ui.fragments.quests.details.QuestDetailsFragment;

public class QuestsFragment extends Fragment
        implements OnMapReadyCallback,
        GoogleMap.OnPoiClickListener,
        QuestDetailsFragment.FragmentListener {

    static final String TAG = QuestsFragment.class.getSimpleName();

    // Map
    private GoogleMap map;

    // Sliding layout
    private FrameLayout slidingLayout;

    // Sliding panel
    private SlidingUpPanelLayout slidingPanel;

    public QuestsFragment() {
        // Required empty public constructor
    }

    static public float getPanelAnchoredOffset(Activity activity) {
        return activity.getResources().getDimension(R.dimen.details_photos_size_anchored)
                / MainActivity.getAppHeightWithoutBar(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private int getActionBarHeight() {
        TypedValue tv = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            return TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        return 0;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quests, container, false);

        // Sliding layout
        slidingLayout = view.findViewById(R.id.sliding_container);

        // Sliding panel
        slidingPanel = view.findViewById(R.id.sliding_panel);
        slidingPanel.addPanelSlideListener((SlidingUpPanelLayout.PanelSlideListener) getActivity());
        slidingPanel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                // Refresh photos size
                refreshPhotosSize(
                        panel.findViewById(R.id.details_photos_layout),
                        slideOffset
                );
            }

            @Override
            public void onPanelStateChanged(View panel,
                                            SlidingUpPanelLayout.PanelState previousState,
                                            SlidingUpPanelLayout.PanelState newState) {
                // Set map padding
                switch (newState) {
                    case HIDDEN:
                        map.setPadding(0, getActionBarHeight(),
                                0, 0);
                        break;
                    case COLLAPSED:
                        map.setPadding(0, getActionBarHeight(),
                                0, panel.findViewById(R.id.layout_details_header)
                                        .getHeight());
                        break;
                }
            }
        });

        // Autocomplete fragment
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        // Autocomplete filter
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_GEOCODE)
                .setCountry("RU")
                .build();
        autocompleteFragment.setFilter(typeFilter);
        autocompleteFragment.setOnPlaceSelectedListener(
                new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(Place place) {
                        QuestsFragment.this.onPlaceSelected(place, false);
                    }

                    @Override
                    public void onError(Status status) {
                        Toast.makeText(getActivity(),
                                "Error place select: " + status.getStatusMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }
        );

        return view;
    }

    void onPlaceSelected(Place place, boolean showDetails) {
        Log.d(TAG, "Place selected:\nname: " + place.getName() + "\nunical id: " + place.getId());

        // Go to place
        float currentZoom = map.getCameraPosition().zoom;
        float defaultZoom = getResources().getDimension(R.dimen.map_zoom);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                place.getLatLng(),
                (currentZoom < defaultZoom)
                        ? defaultZoom
                        : currentZoom
        ));

        // - Show details -

        if (showDetails) {
            // Create details fragment
            QuestDetailsFragment detailsFragment = QuestDetailsFragment.newInstance(place);
            detailsFragment.setFragmentListener(QuestsFragment.this);

            // Set
            FragmentTransaction fragTrans = getFragmentManager().beginTransaction();
            fragTrans.replace(R.id.sliding_container,
                    detailsFragment);
            fragTrans.commit();
        }
    }

    private void refreshPhotosSize(LinearLayout photosLayout, float slideOffset) {
        float anchoredOffset = getPanelAnchoredOffset(getActivity());
        if (slideOffset > anchoredOffset) {
            // Change photos height
            for (int mPhotoId = 0; mPhotoId < photosLayout.getChildCount(); mPhotoId++) {
                photosLayout.getChildAt(mPhotoId).getLayoutParams().height =
                        (int) (getResources().getDimension(R.dimen.details_photos_size_anchored)
                                * (1 - (slideOffset - anchoredOffset)));
            }
            photosLayout.requestLayout();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Map view
        MapView mapView = view.findViewById(R.id.quests_map);
        mapView.onCreate(null);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());

        map = googleMap;

        // Type
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Padding
        map.setPadding(0, getActionBarHeight(),
                0, 0);

        // Style
        updateMapStyle();

        // To Nuzhny Novgorod
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(56.326887, 44.005986),
                getResources().getDimension(R.dimen.map_zoom)));

        // Listen poi clicks
        googleMap.setOnPoiClickListener(this);
    }

    private void updateMapStyle() {
        // Get style
        String mapStyle = MainActivity.settings.getString(
                getResources().getString(R.string.settings_map_style_key),
                null
        );

        if (mapStyle != null) {
            // Add prefix
            mapStyle = "map_" + mapStyle;

            // Set style
            try {
                boolean styleValid = map.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                getActivity().getApplicationContext(),
                                getResources().getIdentifier(
                                        mapStyle,
                                        "raw",
                                        getActivity().getPackageName())
                        )
                );

                if (!styleValid)
                    Log.e(TAG, "Parse style failed");
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "Find style \"" + mapStyle + "\" failed", e);
            }
        } else {
            Log.d(TAG, "Map style by "
                    + getResources().getString(R.string.settings_map_style_key)
                    + " not found!\nUsing default map style...");
        }
    }

    @Override
    public void onPoiClick(PointOfInterest poi) {
        Log.d(TAG, "Poi " + poi.name + " clicked!");

        // Find place by id (from poi)
        Places.GeoDataApi.getPlaceById(MainActivity.getApiClient(), poi.placeId)
                .setResultCallback(places -> {
                    if (places.getStatus().isSuccess() &&
                            places.getCount() > 0) {
                        // Select place
                        onPlaceSelected(places.get(0), true);
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();

        // Hide quest panel if empty
        if (slidingLayout == null || slidingLayout.getChildCount() == 0)
            slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        // Update map
        if (map != null) {
            updateMapStyle();
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
                            view.findViewById(R.id.layout_details_header)
                                    .getHeight());

                    // Show if hidden
                    if (slidingPanel.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN)
                        slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }
        );

        // Set on click header
        view.findViewById(R.id.layout_details_header).setOnClickListener(
                v -> {
                    slidingPanel.setMinimumHeight(view.findViewById(R.id.layout_details_header)
                            .getHeight());

                    // Click header event
                    slidingLayout.callOnClick();
                }
        );

        // Set recycler is scrollable
        slidingPanel.setScrollableView(view.findViewById(R.id.details_description_scroll));

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
}
