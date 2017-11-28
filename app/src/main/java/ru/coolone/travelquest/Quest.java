package ru.coolone.travelquest;

import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;
import java.util.List;

public class Quest implements Serializable {

    // Types
    private List<Integer> types;
    // Name
    private String name;
    // Description
    private String description;
    private LatLng coord;
    // Photos
    private PlacePhotoMetadataResult photos;

    Quest(LatLng coord,
          String name,
          List<Integer> types,
          String description) {
        // Coord
        this.coord = coord;

        // Name
        this.name = name;

        // Description
        this.description = description;
    }

    public List<Integer> getTypes() {
        return types;
    }

    public void setTypes(List<Integer> types) {
        this.types = types;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Coordinate

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LatLng getCoord() {
        return coord;
    }

    public void setCoord(LatLng coord) {
        this.coord = coord;
    }

    public PlacePhotoMetadataResult getPhotos() {
        return photos;
    }

    public void setPhotos(PlacePhotoMetadataResult photos) {
        this.photos = photos;
    }

    // Add self to map
    public void addToMap(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions()
                .position(coord)
                .title(name));
    }
}
