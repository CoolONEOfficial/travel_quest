package ru.coolone.travelquest.fragments.quests;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import ru.coolone.travelquest.R;
import ru.coolone.travelquest.activities.MainActivity;

public class QuestDetailsFragment extends Fragment {

    // Arguments
    enum ArgKeys {
        ARG_TITLE("title"),
        ARG_PHONE("phone"),
        ARG_URL("url"),
        ARG_RATING("rating"),
        ARG_TYPES("types"),
        ARG_PHOTOS("photos");

        private final String val;

        ArgKeys(String val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return val;
        }
    }

    // Title
    private String title;

    // Phone
    private String phone;

    // Url
    private Uri url;

    // Rating
    private float rating;

    // Types
    private ArrayList<Integer> types;

    // Photos
    private PlacePhotoMetadataResult photos;

    // Views
    private SparseArray<View> viewArr = new SparseArray<>();

    public QuestDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title  Title.
     * @param phone  Phone number.
     * @param url    Website url.
     * @param rating Rating.
     * @param types  Array of type ID's (Integers)
     * @return A new instance of fragment QuestDetailsFragment.
     */
    public static QuestDetailsFragment newInstance(
            Context parent,
            String title,
            String phone,
            Uri url,
            float rating,
            ArrayList<Integer> types,
            PlacePhotoMetadataResult photos) {
        // Create quest
        QuestDetailsFragment fragment = new QuestDetailsFragment();

        // Put arguments
        Bundle args = new Bundle();
        args.putString(ArgKeys.ARG_TITLE.toString(), title);
        args.putString(ArgKeys.ARG_PHONE.toString(), phone);
        args.putString(ArgKeys.ARG_URL.toString(),
                url != null
                        ? url.toString()
                        : parent.getResources().getString(R.string.url_unknown));
        args.putFloat(ArgKeys.ARG_RATING.toString(), rating);
        args.putIntegerArrayList(ArgKeys.ARG_TYPES.toString(), types);
        args.putParcelable(ArgKeys.ARG_PHOTOS.toString(), photos);
        fragment.setArguments(args);

        return fragment;
    }

    public static QuestDetailsFragment newInstance(Context parent,
                                                   Place place) {
        QuestDetailsFragment ret = newInstance(
                parent,
                place.getName().toString(),
                place.getPhoneNumber().toString(),
                place.getWebsiteUri(),
                place.getRating(),
                new ArrayList<Integer>(Arrays.asList(place.getPlaceTypes().toArray(new Integer[0]))),
                null
        );
        Places.GeoDataApi.getPlacePhotos(MainActivity.apiClient, place.getId())
                .setResultCallback(ret::setPhotos);
        return ret;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        if (args != null) {
            // Get arguments
            title = args.getString(ArgKeys.ARG_TITLE.toString());
            phone = args.getString(ArgKeys.ARG_PHONE.toString());
            url = Uri.parse(args.getString(ArgKeys.ARG_URL.toString()));
            rating = args.getFloat(ArgKeys.ARG_RATING.toString());
            types = args.getIntegerArrayList(ArgKeys.ARG_TYPES.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_quest_details,
                container,
                false);

        // Get views
        final int[] viewIdArr = new int[]{
                R.id.details_title,
                R.id.details_phone,
                R.id.details_url,
                R.id.details_types,
                R.id.details_rating
        };
        for (int mViewId : viewIdArr) {
            viewArr.put(mViewId,
                    view.findViewById(mViewId));
        }

        // Refresh views
        refresh();

        return view;
    }

    private void refreshTitle() {
        if(title != null) {
            ((TextView) viewArr.get(R.id.details_title)).setText(title);
        }
    }

    private void refreshPhone() {
        if(phone != null) {
            ((TextView) viewArr.get(R.id.details_phone)).setText(phone);
        }
    }

    private void refreshTypes() {
        if(types != null) {
            // Generate types string
            StringBuilder typesStrBuilder = new StringBuilder();
            for (Integer mType : types) {
                typesStrBuilder.append(placeTypeIdToString(mType))
                        .append(" ");
            }
            String typesStr = typesStrBuilder.toString();
            typesStr = typesStr.trim();

            ((TextView) viewArr.get(R.id.details_types)).setText(typesStr);
        }
    }

    private void refreshUrl() {
        if(url != null) {
            ((TextView) viewArr.get(R.id.details_url)).setText(url.toString());
        }
    }

    private void refreshRating() {
        ((TextView) viewArr.get(R.id.details_rating))
                .setText(new DecimalFormat("#.#").format(rating));
    }

    private void refreshPhotos() {
        if(photos != null) {
            // Get photos layout
            LinearLayout photoLayout = getActivity().findViewById(R.id.details_photos_layout);

            // Get photos scroll
            HorizontalScrollView photoScroll = getActivity().findViewById(R.id.details_photos_scroll);

            // Get photos buffer
            PlacePhotoMetadataBuffer photosBuffer = photos.getPhotoMetadata();

            // Get all from buffer
            for (int mPhotoId = 0; mPhotoId < photosBuffer.getCount(); mPhotoId++) {
                final int mPhotoIdFinal = mPhotoId;

                // Get metadata
                PlacePhotoMetadata mPhotoMeta = photosBuffer.get(mPhotoId);

                // Get photo
                mPhotoMeta.getScaledPhoto(MainActivity.apiClient,
                        Integer.MAX_VALUE,
                        photoScroll.getHeight()).setResultCallback(
                        (PlacePhotoResult placePhotoResult) -> {
                            // Get bitmap
                            Bitmap mPhoto = placePhotoResult.getBitmap();

                            // Create image view
                            ImageView mPhotoView = new ImageView(getActivity());
                            mPhotoView.setId(mPhotoIdFinal);
                            mPhotoView.setPadding(5, 5,
                                    5, 5);
                            mPhotoView.setImageBitmap(mPhoto);

                            // Add view
                            photoLayout.addView(mPhotoView);
                        }
                );
            }
        }
    }

    private void refresh() {
        // Refresh all
        refreshTitle();
        refreshPhone();
        refreshUrl();
        refreshRating();
        refreshTypes();
        refreshPhotos();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        refreshTitle();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        refreshPhone();
    }

    public Uri getUrl() {
        return url;
    }

    public void setUrl(Uri url) {
        this.url = url;
        refreshUrl();
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
        refreshRating();
    }

    public ArrayList<Integer> getTypes() {
        return types;
    }

    public void setTypes(ArrayList<Integer> types) {
        this.types = types;
        refreshTypes();
    }

    public PlacePhotoMetadataResult getPhotos() {
        return photos;
    }

    public void setPhotos(PlacePhotoMetadataResult photos) {
        this.photos = photos;
        refreshPhotos();
    }

    private static String placeTypeIdToString(Integer placeTypeId) {


        return null;
    }
}
