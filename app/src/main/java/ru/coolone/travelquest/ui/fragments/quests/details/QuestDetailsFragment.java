package ru.coolone.travelquest.ui.fragments.quests.details;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.activities.MainActivity;
import ru.coolone.travelquest.ui.fragments.quests.details.items.BaseQuestDetailsItem;
import ru.coolone.travelquest.ui.fragments.quests.details.items.QuestDetailsItemRecycler;
import ru.coolone.travelquest.ui.fragments.quests.details.items.QuestDetailsItemText;
import ru.coolone.travelquest.ui.views.adapters.BaseSectionedAdapter;
import ru.coolone.travelquest.ui.views.adapters.BaseSectionedHeader;

public class QuestDetailsFragment extends Fragment {

    static final String TAG = QuestDetailsFragment.class.getSimpleName();

    // Arguments
    enum ArgKeys {
        TITLE("title"),
        PHONE("phone"),
        URL("url"),
        RATING("rating"),
        TYPES("types"),
        DESCRIPTION_PLACE_ID("description_place_id"),
        PHOTOS("photos");

        private final String val;

        ArgKeys(String val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return val;
        }
    }

    private String title;
    private String descriptionPlaceId;
    RecyclerView descriptionRecyclerView;
    private String phone;
    private Uri url;
    private float rating;
    private ArrayList<Integer> types;
    private PlacePhotoMetadataResult photos;

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
     * @param types  Array of type ID's
     * @return A new instance of fragment QuestDetailsFragment.
     */
    public static QuestDetailsFragment newInstance(
            Context parent,
            String title,
            String phone,
            Uri url,
            float rating,
            ArrayList<Integer> types,
            PlacePhotoMetadataResult photos,
            String descriptionPlaceId) {
        // Create quest
        QuestDetailsFragment fragment = new QuestDetailsFragment();

        // Put arguments
        Bundle args = new Bundle();
        args.putString(ArgKeys.TITLE.toString(), title);
        args.putString(ArgKeys.DESCRIPTION_PLACE_ID.toString(), descriptionPlaceId);
        args.putString(ArgKeys.PHONE.toString(), phone);
        args.putString(ArgKeys.URL.toString(),
                url != null
                        ? url.toString()
                        : null);
        args.putFloat(ArgKeys.RATING.toString(), rating);
        args.putIntegerArrayList(ArgKeys.TYPES.toString(), types);
        args.putParcelable(ArgKeys.PHOTOS.toString(), photos);
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
                null,
                place.getId()
        );
        Places.GeoDataApi.getPlacePhotos(MainActivity.getApiClient(), place.getId())
                .setResultCallback(ret::setPhotos);
        return ret;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        if (args != null) {
            // Get arguments
            title = args.getString(ArgKeys.TITLE.toString());
            descriptionPlaceId = args.getString(ArgKeys.DESCRIPTION_PLACE_ID.toString());
            phone = args.getString(ArgKeys.PHONE.toString());
            String urlStr = args.getString(ArgKeys.URL.toString());
            if (urlStr != null)
                url = Uri.parse(urlStr);
            rating = args.getFloat(ArgKeys.RATING.toString());
            types = args.getIntegerArrayList(ArgKeys.TYPES.toString());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_quest_details,
                container,
                false);

        // Get views
        final int[] viewIdArr = new int[]{
                R.id.layout_details_header,
                R.id.layout_details_body,
                R.id.layout_details,
                R.id.details_title,
                R.id.details_description_recycler,
                R.id.details_description_unknown_text,
                R.id.details_description_unknown_text_primary,
                R.id.details_description_unknown_text_smile,
                R.id.details_phone,
                R.id.details_url,
                R.id.details_types,
                R.id.details_rating,
                R.id.details_rating_star,
                R.id.details_photos_layout,
                R.id.details_photos_scroll,
                R.id.details_delimiter
        };
        for (int mViewId : viewIdArr) {
            viewArr.put(mViewId,
                    view.findViewById(mViewId));
        }

        // Recycle view
        descriptionRecyclerView = (RecyclerView) viewArr.get(R.id.details_description_recycler);
        setDescriptionRecyclerView(descriptionRecyclerView);

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

        // Set delimiter visibility
        boolean delimVisibility = visibility ||
                viewArr.get(R.id.details_url).getVisibility() == View.VISIBLE;
        viewArr.get(R.id.details_delimiter).setVisibility(
                delimVisibility
                        ? View.VISIBLE
                        : View.GONE
        );
        if (visibility)
            ((RelativeLayout.LayoutParams) viewArr.get(R.id.details_delimiter).getLayoutParams())
                    .addRule(RelativeLayout.ABOVE,
                            R.id.details_phone);
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

        // Set delimiter visibility
        boolean delimVisibility = visibility ||
                viewArr.get(R.id.details_phone).getVisibility() == View.VISIBLE;
        viewArr.get(R.id.details_delimiter).setVisibility(
                delimVisibility
                        ? View.VISIBLE
                        : View.GONE
        );
        if (visibility)
            ((RelativeLayout.LayoutParams) viewArr.get(R.id.details_delimiter).getLayoutParams())
                    .addRule(RelativeLayout.ABOVE,
                            R.id.details_url);
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

    private int getResourcesColor(int id) {
        // Get theme
        Resources.Theme theme = null;
        try {
            theme = getActivity().getTheme();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        // Get color from resources
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                theme != null)
            return getResources().getColor(id, theme);
        else
            return getResources().getColor(id);
    }

    private void setDescriptionRecyclerView(RecyclerView recyclerView) {
        // Recycler view
        recyclerView.setHasFixedSize(true);

        // Layout manager
        RecyclerView.LayoutManager descriptionLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(descriptionLayoutManager);

        // Adapter
        QuestDetailsAdapter adapter = (recyclerView.getAdapter() == null
                ? new QuestDetailsAdapter()
                : (QuestDetailsAdapter) recyclerView.getAdapter());
        adapter.setHeaderClickListener(
                new BaseSectionedAdapter.OnClickListener
                        <BaseSectionedHeader, QuestDetailsAdapter.HeaderHolder>() {
                    @Override
                    public void onClick(BaseSectionedHeader i,
                                        QuestDetailsAdapter.HeaderHolder i2,
                                        int section) {
                        Log.d(TAG, "Toggle section expanded");
                        adapter.toggleSectionExpanded(section);
                    }

                    @Override
                    public boolean onLongClick(BaseSectionedHeader i,
                                               QuestDetailsAdapter.HeaderHolder i2,
                                               int section) {
                        return false;
                    }
                });
        adapter.shouldShowHeadersForEmptySections(true);
        recyclerView.setAdapter(adapter);
    }

    private void refreshDescription() {

        if (descriptionPlaceId != null) {

            FirebaseFirestore db = FirebaseFirestore
                    .getInstance();

            DocumentReference docRef =
                    db
                            .collection(MainActivity.getLocaleStr(getContext()))
                            .document("quests")
                            .collection(descriptionPlaceId)
                            .document("doc");

            // Parse description

            // Get doc
            docRef
                    .get()
                    .addOnCompleteListener(task -> {
                        DocumentSnapshot doc = task.getResult();

                        // Show description
                        setDescriptionVisibility(View.VISIBLE);

                        // Parse description
                        parse(doc, 0, (RecyclerView) viewArr.get(R.id.details_description_recycler));
                    })
                    .addOnFailureListener(this::descriptionError);
        }
    }

    private void setDescriptionVisibility(int visibility) {
        int errorVisibility = (visibility == View.VISIBLE
                ? View.GONE
                : View.VISIBLE);

        // Description visibility
        viewArr.get(R.id.details_description_recycler)
                .setVisibility(visibility);

        // Error visibility
        viewArr.get(R.id.details_description_unknown_text)
                .setVisibility(errorVisibility);
        viewArr.get(R.id.details_description_unknown_text_primary)
                .setVisibility(errorVisibility);
        ((TextView) viewArr.get(R.id.details_description_unknown_text_primary)).setText("");
        viewArr.get(R.id.details_description_unknown_text_smile)
                .setVisibility(errorVisibility);
    }

    private void parse(DocumentSnapshot doc, int step, RecyclerView recyclerView) {
        Log.d(TAG, "Parse step: " + step);

        QuestDetailsAdapter adapter = (QuestDetailsAdapter) recyclerView.getAdapter();

        // Get sub collections count
        if (doc.exists()) {
            for (int mCollectionId = 0; mCollectionId < doc.getLong("count"); mCollectionId++) {
                // Get collection (wait get task)
                doc
                        .getReference()
                        .collection(String.valueOf(mCollectionId))
                        .get()
                        .addOnSuccessListener(collection -> {

                            Log.d(TAG, "Collection:"
                                    + "\n\tsize: " + collection.size());

                            // Parse doc
                            for (int mmNextDocId = 0; mmNextDocId < collection.size(); mmNextDocId++) {
                                DocumentSnapshot mNextDoc = collection
                                        .getDocuments()
                                        .get(mmNextDocId);

                                Log.d(TAG, "Next doc:"
                                        + "\n\ttitle: " +
                                        (mNextDoc.contains("title")
                                                ? mNextDoc.get("title")
                                                : "unknown")
                                        + "\n\ttext: " +
                                        (mNextDoc.contains("text")
                                                ? mNextDoc.get("text")
                                                : "unknown"));

                                // Header
                                BaseSectionedHeader header = new BaseSectionedHeader() {{
                                    setTitle(mNextDoc.contains("title")
                                            ? (String) mNextDoc.get("title")
                                            : getResources().getString(R.string.details_title_unknown));
                                }};

                                // Item
                                BaseQuestDetailsItem item;

                                if (mNextDoc.contains("text")) {
                                    // Text
                                    item = new QuestDetailsItemText() {{
                                        setText((String) mNextDoc.get("text"));
                                    }};
                                } else {
                                    // Recycler
                                    item = new QuestDetailsItemRecycler() {{
                                        // Create recycler view
                                        RecyclerView itemRecyclerView = new RecyclerView(getActivity());
                                        setDescriptionRecyclerView(itemRecyclerView);

                                        // Parse recycler view
                                        parse(mNextDoc, step + 1, itemRecyclerView);

                                        // Set recycler view
                                        setRecyclerView(itemRecyclerView);
                                    }};
                                }

                                // Add section
                                adapter.addSection(
                                        header,
                                        new ArrayList<BaseQuestDetailsItem>() {{
                                            add(item);
                                        }}
                                );
                                adapter.notifyDataSetChanged();
                            }
                        }).addOnFailureListener(this::descriptionError);
            }
        } else setDescriptionVisibility(View.GONE);

        // Collapse all
        if (step != 0)
            adapter.collapseAllSections();

        // Update adapter
        recyclerView.setAdapter(adapter);
    }

    private void descriptionError(String errStr) {
        // Hide description and show errors
        setDescriptionVisibility(View.GONE);

        // Set error text
        ((TextView) viewArr.get(R.id.details_description_unknown_text_primary))
                .setText(errStr);
    }

    private void descriptionError(Exception e) {
        descriptionError(e.getLocalizedMessage());
    }

    private Rect getSizeWithoutStatusbar() {
        View content = getActivity().getWindow().findViewById(Window.ID_ANDROID_CONTENT);
        return new Rect(
                0,
                0,
                content.getWidth(),
                content.getHeight()
        );
    }

    private void refreshPhotos() {
        boolean visibility;

        // Set photos
        if (photos != null) {
            // Get photos layout
            LinearLayout photoLayout = (LinearLayout) viewArr.get(R.id.details_photos_layout);
            ViewGroup.LayoutParams params = photoLayout.getLayoutParams();
            params.height = getSizeWithoutStatusbar().height() / 3;
            photoLayout.setLayoutParams(params);

            // Get photos buffer
            PlacePhotoMetadataBuffer photosBuffer = photos.getPhotoMetadata();
            Log.d(TAG, "Photo buffer created");

            // Set visibility bool
            visibility = (photosBuffer.getCount() != 0);

            if (photosBuffer.getCount() != 0) {

                // Create metadata photos array
                PlacePhotoMetadata[] photosMetadata = new PlacePhotoMetadata[photosBuffer.getCount()];

                // Get metadata photos
                for (int mPhotoId = 0; mPhotoId < photosBuffer.getCount(); mPhotoId++) {
                    photosMetadata[mPhotoId] = photosBuffer.get(mPhotoId);
                }

                // Get all from buffer
                for (int mPhotoId = 0; mPhotoId < photosBuffer.getCount(); mPhotoId++) {
                    final int mPhotoIdFinal = mPhotoId;

                    // Get metadata
                    PlacePhotoMetadata mPhotoMeta = photosMetadata[mPhotoIdFinal];

                    // Get photos height
                    TypedValue photosHeightValue = new TypedValue();
                    getResources().getValue(R.dimen.details_photos_height_float,
                            photosHeightValue,
                            true);

                    mPhotoMeta.getPhoto(MainActivity.getApiClient())
                            .setResultCallback(
                                    (PlacePhotoResult placePhotoResult) -> {
                                        // Get bitmap
                                        Bitmap mPhoto = placePhotoResult.getBitmap();

                                        // Create image view
                                        ImageView mPhotoView = new ImageView(getActivity());
                                        mPhotoView.setId(mPhotoIdFinal);
                                        mPhotoView.setPadding(5, 5,
                                                5, 5);
                                        mPhotoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                        mPhotoView.setAdjustViewBounds(true);
                                        mPhotoView.setImageBitmap(mPhoto);

                                        // Add view
                                        photoLayout.addView(mPhotoView);
                                    }
                            );
                }
            }

            // Release photos buffer
            photosBuffer.release();
            Log.d(TAG, "Photos buffer released");
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
        refreshDescription();
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

    public String getDescriptionPlaceId() {
        return descriptionPlaceId;
    }

    public void setDescriptionPlaceId(String descriptionPlaceId) {
        this.descriptionPlaceId = descriptionPlaceId;
        refreshDescription();
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
        if (onCreateViewListener == null) {
            throw new ClassCastException(
                    "Parent activity must implements OnCreateViewListener"
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
