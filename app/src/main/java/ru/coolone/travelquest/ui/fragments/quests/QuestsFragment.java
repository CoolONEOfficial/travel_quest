package ru.coolone.travelquest.ui.fragments.quests;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
        PlaceSelectionListener,
        QuestDetailsFragment.OnCreateViewListener {

    static final String TAG = QuestsFragment.class.getSimpleName();

    // Map
    GoogleMap map;
    MapView mapView;
    View view;

    // Sliding layout
    FrameLayout slidingLayout;

    // Sliding panel
    SlidingUpPanelLayout slidingPanel;

    public QuestsFragment() {
        // Required empty public constructor
    }

    public static QuestsFragment newInstance() {
        // Create fragment
        return new QuestsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout
        view = inflater.inflate(R.layout.fragment_quests, container, false);

        // Create toolbar
        if (toolbarView != null) {
            ViewGroup parent = (ViewGroup) toolbarView.getParent();
            if (parent != null)
                parent.removeView(toolbarView);
        }
        try {
            Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
            toolbarView = getActivity().getLayoutInflater().inflate(R.layout.fragment_quests_toolbar,
                    toolbar.findViewById(R.id.toolbar_container),
                    false);
        } catch (InflateException e) {
            e.printStackTrace();
        }

        // Autocomplete fragment
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        // Autocomplete filter
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();
        autocompleteFragment.setFilter(typeFilter);
        autocompleteFragment.setOnPlaceSelectedListener(this);

        // Sliding layout
        slidingLayout = view.findViewById(R.id.sliding_container);

        // Sliding panel
        slidingPanel = view.findViewById(R.id.sliding_panel);
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
                        map.setPadding(0, 0,
                                0, 0);
                        break;
                    case COLLAPSED:
                        map.setPadding(0, 0,
                                0, panel.findViewById(R.id.layout_details_header)
                                        .getHeight());
                        break;
                }
            }
        });

        return view;
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
        mapView = view.findViewById(R.id.quests_map);
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
                        onPlaceSelected(places.get(0));
                    }
                });
    }

    @Override
    public void onPlaceSelected(Place place) {
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

        // Create details fragment
        QuestDetailsFragment detailsFragment = QuestDetailsFragment.newInstance(place);
        detailsFragment.setOnCreateViewListener(this);

        // Set
        FragmentTransaction fragTrans = getFragmentManager().beginTransaction();
        fragTrans.replace(R.id.sliding_container,
                detailsFragment);
        fragTrans.commit();
    }

    @Override
    public void onError(Status status) {
        Toast.makeText(getActivity(),
                "Error place select: " + status.getStatusMessage(),
                Toast.LENGTH_LONG).show();
    }

    // Toolbar view with search
    View toolbarView;

    @Override
    public void onResume() {
        super.onResume();

        // Get toolbar layout
        LinearLayout toolbarContainer = getActivity().findViewById(R.id.toolbar_container);

        // Delete old toolbar search
        ViewParent toolbarViewParent = toolbarView.getParent();
        if (toolbarViewParent != null)
            ((ViewGroup) toolbarViewParent).removeView(toolbarView);

        // Add toolbar search
        toolbarContainer.addView(toolbarView);

        // Update map
        if (map != null) {
            updateMapStyle();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // Get toolbar and toolbar view
        LinearLayout toolbarContainer = getActivity().findViewById(R.id.toolbar_container);

        // Remove toolbar search
        toolbarContainer.removeView(toolbarView);
    }

    static public float getPanelAnchoredOffset(Activity activity) {
        return activity.getResources().getDimension(R.dimen.details_photos_size_anchored)
                / MainActivity.getAppHeightWithoutBar(activity);
    }


    @Override
    public void onQuestDetailsCreateView(
            @NonNull View view,
            ViewGroup container, Bundle savedInstanceState) {
        view.post(() -> {
            // Get panel height
            RelativeLayout detailsHead = view.findViewById(R.id.layout_details_header);
            Log.d(TAG, "Sliding layout height:" + String.valueOf(detailsHead.getHeight()));

            // Set panel anchor point
            slidingPanel.setAnchorPoint(getPanelAnchoredOffset(getActivity()));

            // Set panel height
            slidingPanel.setPanelHeight(detailsHead.getHeight());

            // Show
            if (slidingPanel.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN)
                slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        });

        // Set recycler is scrollable
        slidingPanel.setScrollableView(view.findViewById(R.id.details_description_scroll));
    }
}
