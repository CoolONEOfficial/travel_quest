package ru.coolone.travelquest.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.PointOfInterest;

import ru.coolone.travelquest.R;
import ru.coolone.travelquest.activities.MainActivity;

public class QuestsFragment extends Fragment
        implements OnMapReadyCallback, GoogleMap.OnPoiClickListener{

    static final String TAG = QuestsFragment.class.getSimpleName();

    // Map
    GoogleMap map;
    MapView mapView;
    View view;

    // TODO: interface
//    private OnFragmentInteractionListener mListener;

    public QuestsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();

        // Update map
        if(map != null) {
            updateMapStyle();
        }
    }

    public static QuestsFragment newInstance() {
        // Create fragment
        return new QuestsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            // Map style
//            mapStyle = getArguments().getString(ARG_MAP_STYLE);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout
        view = inflater.inflate(R.layout.fragment_quests, container, false);
        return  view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Map view
        mapView = view.findViewById(R.id.quests_map);
        if(mapView != null)
        {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // TODO: interface
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
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
                14.0f));
    }

    @Override
    public void onPoiClick(PointOfInterest poi) {
        Toast.makeText(getActivity().getApplicationContext(),
                        "Clicked: " + poi.name +
                        "\nPlace ID:" + poi.placeId +
                        "\nLatitude:" + poi.latLng.latitude +
                        " Longitude:" + poi.latLng.longitude,
                Toast.LENGTH_SHORT).show();
    }

    private void updateMapStyle() {
        String mapStyle = "map_" + MainActivity.settings.getString(
                getResources().getString(R.string.settings_map_style_key),
                "black"
        );

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
            Log.e(TAG, "Find style failed. ", e);
        }
    }

    // TODO: interface
//    public interface OnFragmentInteractionListener {
//        void onFragmentInteraction(Uri uri);
//    }
}
