package ru.coolone.travelquest.fragments.quests;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import ru.coolone.travelquest.R;
import ru.coolone.travelquest.activities.MainActivity;

public class QuestDetailsFragment extends Fragment {

    static final String TAG = QuestDetailsFragment.class.getSimpleName();

    private View view;

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
                        : null);
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
                new ArrayList<>(Arrays.asList(place.getPlaceTypes().toArray(new Integer[0]))),
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
            String urlStr = args.getString(ArgKeys.ARG_URL.toString());
            if (urlStr != null)
                url = Uri.parse(urlStr);
            rating = args.getFloat(ArgKeys.ARG_RATING.toString());
            types = args.getIntegerArrayList(ArgKeys.ARG_TYPES.toString());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_quest_details,
                container,
                false);

        // Get views
        final int[] viewIdArr = new int[]{
                R.id.details_title,
                R.id.details_phone,
                R.id.details_url,
                R.id.details_types,
                R.id.details_rating,
                R.id.details_rating_star,
                R.id.details_photos_layout,
                R.id.details_photos_scroll
        };
        for (int mViewId : viewIdArr) {
            viewArr.put(mViewId,
                    view.findViewById(mViewId));
        }

        // Refresh views
        refresh();

        // Call listener
        onCreateViewListener.onQuestDetailsCreateView(view, container, savedInstanceState);

        return view;
    }

    private void refreshTitle() {
        if (title != null) {
            ((TextView) viewArr.get(R.id.details_title)).setText(title);
        }
    }

    private void refreshPhone() {
        // Set phone
        if (phone != null) {
            ((TextView) viewArr.get(R.id.details_phone)).setText(phone);
        }

        // Set visibility
        boolean visibility = (phone != null);
        viewArr.get(R.id.details_phone).setVisibility(
                visibility
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    private void refreshTypes() {
        if (types != null) {
            // - Get types string -

            // Generate types string
            StringBuilder typesStrBuilder = new StringBuilder();
            for (Integer mType : types) {
                String placeTypeStr = placeTypeIdToString(getActivity(), mType);

                if (placeTypeStr != null && !placeTypeStr.trim().isEmpty())
                    typesStrBuilder.append(placeTypeIdToString(getActivity(), mType))
                            .append(", ");
            }

            // Get types string
            String typesStr = typesStrBuilder.toString();
            if (typesStr.endsWith(", "))
                typesStr = typesStr.substring(0, typesStr.length() - 2);

            // - Set types string -

            // Set visibility
            int visibility = typesStr.isEmpty()
                    ? View.GONE
                    : View.VISIBLE;
            viewArr.get(R.id.details_types).setVisibility(visibility);

            ((TextView) viewArr.get(R.id.details_types)).setText(typesStr);
        }
    }

    private void refreshUrl() {
        // Set url
        if (url != null) {
            ((TextView) viewArr.get(R.id.details_url)).setText(url.toString());
        }

        // Set url visibility
        boolean visibility = (url != null);
        viewArr.get(R.id.details_url).setVisibility(
                visibility
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    private void refreshRating() {
        // Set rating
        if (rating != -1) {
            ((TextView) viewArr.get(R.id.details_rating))
                    .setText(new DecimalFormat("#.#").format(rating));
        }

        // Set rating visibility
        int visibility = (rating == -1
                ? View.GONE
                : View.VISIBLE
        );
        setRatingVisibility(visibility);
    }

    private void setRatingVisibility(int visibility) {
        viewArr.get(R.id.details_rating).setVisibility(visibility);
        viewArr.get(R.id.details_rating_star).setVisibility(visibility);
    }

    private void refreshPhotos() {
        boolean visibility;

        // Set photos
        if (photos != null) {
            // Get photos layout
            LinearLayout photoLayout = (LinearLayout) viewArr.get(R.id.details_photos_layout);

            // Get photos buffer
            PlacePhotoMetadataBuffer photosBuffer = photos.getPhotoMetadata();
            Log.d(TAG, "Photo buffer created");

            // Set visibility bool
            visibility = (photosBuffer.getCount() != 0);

            // Get all from buffer
            for (int mPhotoId = 0; mPhotoId < photosBuffer.getCount(); mPhotoId++) {
                final int mPhotoIdFinal = mPhotoId;

                // Get metadata
                PlacePhotoMetadata mPhotoMeta = photosBuffer.get(mPhotoIdFinal);

                // Get photos height
                TypedValue photosHeightValue = new TypedValue();
                getResources().getValue(R.dimen.details_photos_height_float,
                        photosHeightValue,
                        true);


                // Get photo
                int photoHeight = ((int) (photosHeightValue.getFloat() *
                        getResources().getDisplayMetrics().density));
                Log.d(TAG, "Photo height:" + String.valueOf(photoHeight));
                Log.d(TAG, "Density:" +
                        String.valueOf(getResources().getDisplayMetrics().density));
                Log.d(TAG, "Photo height float:" + String.valueOf(photosHeightValue.getFloat()));

                mPhotoMeta.getScaledPhoto(MainActivity.apiClient,
                        Integer.MAX_VALUE,
                        photoHeight
                ).setResultCallback(
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

            // Delete photos buffer
            Log.d(TAG, "Photo buffer released!");
            photosBuffer.release();
        } else visibility = false;

        // Set photos visibility
        setPhotosVisibility(visibility
                ? View.VISIBLE
                : View.GONE
        );
    }

    private void setPhotosVisibility(int visibility) {
        viewArr.get(R.id.details_photos_layout).setVisibility(visibility);
        viewArr.get(R.id.details_photos_scroll).setVisibility(visibility);
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

    private static String placeTypeIdToString(Context parent, Integer placeTypeId) {
        // Get place type resource id
        int placeTypeResId = -1;
        switch (placeTypeId) {
            case Place.TYPE_MUSEUM:
                placeTypeResId = R.string.TYPE_MUSEUM;
                break;
            case Place.TYPE_PARK:
                placeTypeResId = R.string.TYPE_PARK;
                break;
            case Place.TYPE_RESTAURANT:
                placeTypeResId = R.string.TYPE_RESTAURANT;
                break;
            case Place.TYPE_SCHOOL:
                placeTypeResId = R.string.TYPE_SCHOOL;
                break;
            case Place.TYPE_STADIUM:
                placeTypeResId = R.string.TYPE_STADIUM;
                break;
            case Place.TYPE_STORE:
                placeTypeResId = R.string.TYPE_STORE;
                break;
            case Place.TYPE_TRAIN_STATION:
                placeTypeResId = R.string.TYPE_TRAIN_STATION;
                break;
            case Place.TYPE_UNIVERSITY:
                placeTypeResId = R.string.TYPE_UNIVERSITY;
                break;
            case Place.TYPE_ZOO:
                placeTypeResId = R.string.TYPE_ZOO;
                break;
            case Place.TYPE_PLACE_OF_WORSHIP:
                placeTypeResId = R.string.TYPE_PLACE_OF_WORSHIP;
                break;
            case Place.TYPE_GYM:
                placeTypeResId = R.string.TYPE_GYM;
                break;
            case Place.TYPE_CASINO:
                placeTypeResId = R.string.TYPE_CASINO;
                break;
            case Place.TYPE_ART_GALLERY:
                placeTypeResId = R.string.TYPE_ART_GALLERY;
                break;
            case Place.TYPE_ADMINISTRATIVE_AREA_LEVEL_1:
            case Place.TYPE_ADMINISTRATIVE_AREA_LEVEL_2:
            case Place.TYPE_ADMINISTRATIVE_AREA_LEVEL_3:
                placeTypeResId = R.string.TYPE_ADMINISTRATIVE_AREA;
                break;
            case Place.TYPE_AMUSEMENT_PARK:
                placeTypeResId = R.string.TYPE_AMUSEMENT_PARK;
                break;
            case Place.TYPE_AQUARIUM:
                placeTypeResId = R.string.TYPE_AQUARIUM;
                break;
        }

        // Get resource string
        String placeTypeStr;
        if (placeTypeResId != -1)
            placeTypeStr = parent.getResources().getString(placeTypeResId);
        else
            placeTypeStr = null;

        return placeTypeStr;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(onCreateViewListener == null) {
            throw new ClassCastException(
                    getActivity().toString() + " must implements OnCreateViewListener"
            );
        }
    }

    public interface OnCreateViewListener {
        void onQuestDetailsCreateView(@NonNull View view, ViewGroup container,
                                      Bundle savedInstanceState);
    }

    private OnCreateViewListener onCreateViewListener;

    public void setOnCreateViewListener(OnCreateViewListener listener) {
        onCreateViewListener = listener;
    }

    public OnCreateViewListener getOnCreateViewListener() {
        return onCreateViewListener;
    }
}
