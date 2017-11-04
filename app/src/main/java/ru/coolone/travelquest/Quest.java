package ru.coolone.travelquest;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;

public class Quest implements Serializable {

    Quest(LatLng coord,
          String name,
          String description) {
        // Coord
        this.coord = coord;

        // Name
        this.name = name;

        // Description
        this.description = description;
    }

    // Name
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Description
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // - Coordinate -

    private LatLng coord;

    public LatLng getCoord() {
        return coord;
    }

    public void setCoord(LatLng coord) {
        this.coord = coord;
    }

    // Add self to map
    public void addToMap(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions()
                .position(coord)
                .title(name));
    }
}
