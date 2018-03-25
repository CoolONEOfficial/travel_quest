package ru.coolone.travelquest.ui.fragments.quests.details;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.ArrayList;

import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.activities.MainActivity;
import ru.coolone.travelquest.ui.fragments.quests.details.items.BaseQuestDetailsItem;
import ru.coolone.travelquest.ui.fragments.quests.details.items.QuestDetailsItemRecycler;
import ru.coolone.travelquest.ui.fragments.quests.details.items.QuestDetailsItemText;
import ru.coolone.travelquest.ui.views.adapters.BaseSectionedAdapter;
import ru.coolone.travelquest.ui.views.adapters.BaseSectionedHeader;

public class QuestDetailsFragment extends Fragment {

    static final String TAG = QuestDetailsFragment.class.getSimpleName();
    RecyclerView descriptionRecyclerView;
    private OnCreateViewListener onCreateViewListener;

    private String title;
    private String placeId;
    private String phone;
    private Uri url;
    private float rating;
    private int typeId;
    private SparseArray<View> viewArr = new SparseArray<>();

    public QuestDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title   Title.
     * @param phone   Phone number.
     * @param url     Website url.
     * @param rating  Rating.
     * @param typeId  Type id
     * @param placeId Place id
     * @return A new instance of fragment QuestDetailsFragment.
     */
    public static QuestDetailsFragment newInstance(
            String title,
            String phone,
            Uri url,
            float rating,
            int typeId,
            String placeId) {
        // Create quest
        QuestDetailsFragment fragment = new QuestDetailsFragment();

        // Put arguments
        Bundle args = new Bundle();
        args.putString(ArgKeys.TITLE.toString(), title);
        args.putString(ArgKeys.PLACE_ID.toString(), placeId);
        args.putString(ArgKeys.PHONE.toString(), phone);
        args.putString(ArgKeys.URL.toString(),
                url != null
                        ? url.toString()
                        : null);
        args.putFloat(ArgKeys.RATING.toString(), rating);
        args.putInt(ArgKeys.TYPE_ID.toString(), typeId);
        fragment.setArguments(args);

        return fragment;
    }

    public static QuestDetailsFragment newInstance(Place place) {
        return newInstance(
                place.getName().toString(),
                place.getPhoneNumber().toString(),
                place.getWebsiteUri(),
                place.getRating(),
                place.getPlaceTypes().get(0),
                place.getId()
        );
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        if (args != null) {
            // Get arguments
            title = args.getString(ArgKeys.TITLE.toString());
            placeId = args.getString(ArgKeys.PLACE_ID.toString());
            phone = args.getString(ArgKeys.PHONE.toString());
            String urlStr = args.getString(ArgKeys.URL.toString());
            if (urlStr != null)
                url = Uri.parse(urlStr);
            rating = args.getFloat(ArgKeys.RATING.toString());
            typeId = args.getInt(ArgKeys.TYPE_ID.toString());
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
        descriptionRecyclerView.setNestedScrollingEnabled(false);
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
        // - Get type string -
        String typeStr = placeTypeIdToString(getActivity(), typeId);

        // - Set type string -

        // Set visibility
        int visibility = (typeStr == null || typeStr.isEmpty())
                ? View.GONE
                : View.VISIBLE;
        viewArr.get(R.id.details_types).setVisibility(visibility);

        // Set text
        ((TextView) viewArr.get(R.id.details_types)).setText(typeStr);
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
        if (placeId != null) {
            FirebaseFirestore db = FirebaseFirestore
                    .getInstance();

            DocumentReference docRef =
                    db
                            .collection(MainActivity.getLocaleStr(getContext()))
                            .document("quests")
                            .collection(placeId)
                            .document("doc");

            // Parse description

            // Get doc
            docRef.get().addOnCompleteListener(
                    task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();

                            // Show description
                            setDescriptionVisibility(View.VISIBLE);

                            // Parse description
                            parseDescription(doc,
                                    0,
                                    (RecyclerView) viewArr.get(R.id.details_description_recycler));
                        } else descriptionError("Get document task not successful");
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

    private void parseDescription(DocumentSnapshot doc, int step, RecyclerView recyclerView) {
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
                            for (int mNextDocId = 0; mNextDocId < collection.size(); mNextDocId++) {
                                DocumentSnapshot mNextDoc = collection
                                        .getDocuments()
                                        .get(mNextDocId);

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
                                    // Create recycler view
                                    RecyclerView itemRecyclerView = new RecyclerView(getActivity());
                                    recyclerView.setNestedScrollingEnabled(true);
                                    setDescriptionRecyclerView(itemRecyclerView);

                                    // Parse recycler view
                                    parseDescription(mNextDoc, step + 1, itemRecyclerView);

                                    // Recycler
                                    item = new QuestDetailsItemRecycler() {{
                                        setRecyclerView(itemRecyclerView);
                                    }};
                                }

                                // Add section
                                if (mNextDoc.contains("first") &&
                                        mNextDoc.getBoolean("first").equals(Boolean.TRUE)) {
                                    Log.d(TAG, "Adding at first");
                                    adapter.addSection(
                                            0,
                                            header,
                                            new ArrayList<BaseQuestDetailsItem>() {{
                                                add(item);
                                            }}
                                    );
                                } else
                                    adapter.addSection(
                                            header,
                                            new ArrayList<BaseQuestDetailsItem>() {{
                                                add(item);
                                            }}
                                    );
                            }
                            if (step != 0)
                                adapter.collapseAllSections();
                            adapter.notifyDataSetChanged();
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

    private void refreshPhotos() {
        new PhotoTask(this).execute(placeId);
    }

    private void setDescriptionPhotoImageView(ImageView imageView) {
        // Set photo image view
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.details_photos_size_anchored),
                (int) getResources().getDimension(R.dimen.details_photos_size_anchored)
        );
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
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

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
        refreshDescription();
        refreshPhotos();
    }

    public int getType() {
        return typeId;
    }

    public void setTypes(int typeId) {
        this.typeId = typeId;
        refreshTypes();
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

    public void setOnCreateViewListener(OnCreateViewListener onCreateViewListener) {
        this.onCreateViewListener = onCreateViewListener;
    }

    // Arguments
    enum ArgKeys {
        TITLE("title"),
        PHONE("phone"),
        URL("url"),
        RATING("rating"),
        TYPE_ID("type_id"),
        PLACE_ID("place_id");

        private final String val;

        ArgKeys(String val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return val;
        }
    }

    public interface OnCreateViewListener {
        void onQuestDetailsCreateView(@NonNull View view, ViewGroup container,
                                      Bundle savedInstanceState);
    }

    static private class PhotoTask extends AsyncTask<String, Void, Bitmap[]> {

        QuestDetailsFragment parent;

        PhotoTask(QuestDetailsFragment parent) {
            this.parent = parent;
        }

        @Override
        protected Bitmap[] doInBackground(String... params) {
            if (params.length != 1) {
                return null;
            }
            final String placeId = params[0];
            Bitmap[] attributedPhotoArr = null;

            // Get photos result
            PlacePhotoMetadataResult result = Places.GeoDataApi
                    .getPlacePhotos(MainActivity.getApiClient(), placeId)
                    .await();

            // Parse photos result
            if (result.getStatus().isSuccess()) {
                // Get photos buffer
                PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();

                // Parse photos buffer
                if (photoMetadataBuffer.getCount() > 0 && !isCancelled()) {
                    attributedPhotoArr = new Bitmap[photoMetadataBuffer.getCount()];

                    for (int mAttributedPhotoId = 0;
                         mAttributedPhotoId < photoMetadataBuffer.getCount();
                         mAttributedPhotoId++) {

                        // Get the first bitmap and its attributions
                        PlacePhotoMetadata photo = photoMetadataBuffer
                                .get(mAttributedPhotoId)
                                .freeze();

                        // Load a scaled bitmap for this photo
                        attributedPhotoArr[mAttributedPhotoId] = photo
                                .freeze()
                                .getPhoto(MainActivity.getApiClient())
                                .await()
                                .getBitmap();
                    }
                }

                // Release the photos buffer
                photoMetadataBuffer.release();
            }
            return attributedPhotoArr;
        }

        @Override
        protected void onPostExecute(Bitmap[] attributedPhotoArr) {
            if (attributedPhotoArr != null && attributedPhotoArr.length != 0) {
                for (Bitmap mAttributedPhoto : attributedPhotoArr) {
                    // Create image view
                    ImageView mPhotoView = new ImageView(parent.getActivity());
                    parent.setDescriptionPhotoImageView(mPhotoView);
                    mPhotoView.setImageBitmap(mAttributedPhoto);

                    // Add view
                    ((LinearLayout) parent.viewArr.get(R.id.details_photos_layout))
                            .addView(mPhotoView);
                }

            } else {
                // Hide photos
                parent.setPhotosVisibility(View.GONE);
            }
        }
    }
}
