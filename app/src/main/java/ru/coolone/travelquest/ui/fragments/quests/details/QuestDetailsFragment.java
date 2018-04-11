package ru.coolone.travelquest.ui.fragments.quests.details;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.Arrays;
import java.util.List;

import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.activities.AddPlaceActivity;
import ru.coolone.travelquest.ui.activities.MainActivity;
import ru.coolone.travelquest.ui.adapters.BaseSectionedAdapter;
import ru.coolone.travelquest.ui.adapters.BaseSectionedHeader;
import ru.coolone.travelquest.ui.fragments.quests.details.items.BaseQuestDetailsItem;
import ru.coolone.travelquest.ui.fragments.quests.details.items.QuestDetailsItemRecycler;
import ru.coolone.travelquest.ui.fragments.quests.details.items.QuestDetailsItemText;

public class QuestDetailsFragment extends Fragment {

    static final String TAG = QuestDetailsFragment.class.getSimpleName();
    RecyclerView descriptionRecyclerView;
    Button descriptionAddButton;
    private FragmentListener fragmentListener;

    private String title;
    private String placeId;
    private String phone;
    private String url;
    private float rating;
    private String[] types;
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
     * @param types   Types array
     * @param placeId Place id
     * @return A new instance of fragment QuestDetailsFragment.
     */
    public static QuestDetailsFragment newInstance(
            String title,
            String phone,
            String url,
            float rating,
            String[] types,
            String placeId) {
        // Create quest
        QuestDetailsFragment fragment = new QuestDetailsFragment();

        // Put arguments
        Bundle args = new Bundle();
        args.putString(ArgKeys.TITLE.toString(), title);
        args.putString(ArgKeys.PLACE_ID.toString(), placeId);
        args.putString(ArgKeys.PHONE.toString(), phone);
        args.putString(ArgKeys.URL.toString(), url);
        args.putFloat(ArgKeys.RATING.toString(), rating);
        args.putStringArray(ArgKeys.TYPES.toString(), types);
        fragment.setArguments(args);

        return fragment;
    }

    public static QuestDetailsFragment newInstance(com.google.android.gms.location.places.Place place, Context context) {
        // Convert List<Place types ids> to List<Place types>
        List<Integer> placeTypeIds = place.getPlaceTypes();
        ArrayList<String> placeTypes = new ArrayList<>();
        for (int mPlaceTypeIdId = 0; mPlaceTypeIdId < placeTypeIds.size(); mPlaceTypeIdId++) {
            Integer mPlaceId = place.getPlaceTypes().get(mPlaceTypeIdId);
            String mPlaceType = placeTypeIdToString(context, mPlaceId);
            if(mPlaceType != null)
                placeTypes.add(mPlaceType);
        }

        return newInstance(
                place.getName().toString(),
                place.getPhoneNumber().toString(),
                place.getWebsiteUri() != null
                        ? place.getWebsiteUri().toString()
                        : null,
                place.getRating(),
                placeTypes.toArray(new String[0]),
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
            url = args.getString(ArgKeys.URL.toString());
            rating = args.getFloat(ArgKeys.RATING.toString());
            types = args.getStringArray(ArgKeys.TYPES.toString());
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
        for (int mViewId : new int[]{
                R.id.layout_details_header,
                R.id.layout_details_body,
                R.id.layout_details,
                R.id.details_title,
                R.id.details_description_recycler,
                R.id.details_description_add_button,
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
                R.id.details_bottom,
                R.id.details_bottom_delimiter
        }) {
            viewArr.put(mViewId,
                    view.findViewById(mViewId));
        }

        // Recycle view
        descriptionRecyclerView = (RecyclerView) viewArr.get(R.id.details_description_recycler);
        descriptionRecyclerView.setNestedScrollingEnabled(false);
        setDescriptionRecyclerView(descriptionRecyclerView);

        // Add description button
        descriptionAddButton = (Button) viewArr.get(R.id.details_description_add_button);
        descriptionAddButton.setOnClickListener(
                v -> {
                    Intent intent = new Intent(getActivity(), AddPlaceActivity.class);
                    intent.putExtra(AddPlaceActivity.ArgKeys.PLACE_ID.toString(), placeId);
                    startActivity(intent);
                }
        );

        // Refresh views
        refresh();

        // Call listener
        fragmentListener.onQuestDetailsCreateView(view, container, savedInstanceState);

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
        refreshBottomInfoVisibility();
    }

    private void refreshBottomInfoVisibility() {
        setBottomInfoVisibility(
                (((TextView) viewArr.get(R.id.details_url)).getText().length() > 0 ||
                        ((TextView) viewArr.get(R.id.details_phone)).getText().length() > 0)
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    private void setBottomInfoVisibility(int visibility) {
        viewArr.get(R.id.details_bottom).setVisibility(visibility);
        viewArr.get(R.id.details_bottom_delimiter).setVisibility(visibility);
    }

    private void refreshTypes() {
        // - Get type string -
        String typeStr = TextUtils.join(", ", types);

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
        if (url != null)
            ((TextView) viewArr.get(R.id.details_url)).setText(url);

        // Set url visibility
        boolean visibility = (url != null);
        viewArr.get(R.id.details_url).setVisibility(
                visibility
                        ? View.VISIBLE
                        : View.GONE
        );

        // Set delimiter visibility
        refreshBottomInfoVisibility();
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
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

    public String[] getTypes() {
        return types;
    }

    public void setTypes(String[] types) {
        this.types = types;
        refreshTypes();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (fragmentListener == null) {
            throw new ClassCastException(
                    "Parent activity must implements FragmentListener"
            );
        }
    }

    public void setFragmentListener(FragmentListener fragmentListener) {
        this.fragmentListener = fragmentListener;
    }

    // Arguments
    public enum ArgKeys {
        TITLE("title"),
        PHONE("phone"),
        URL("url"),
        RATING("rating"),
        TYPES("types"),
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

    public interface FragmentListener {
        void onQuestDetailsCreateView(@NonNull View view, ViewGroup container,
                                      Bundle savedInstanceState);

        void onPhotosLoadingStarted();
    }

    static private class PhotoTask extends AsyncTask<String, Integer, Void> {

        QuestDetailsFragment parent;

        PhotoTask(QuestDetailsFragment parent) {
            this.parent = parent;
        }

        @Override
        protected Void doInBackground(String... params) {
            if (params.length != 1) {
                return null;
            }
            final String placeId = params[0];

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
                    parent.fragmentListener.onPhotosLoadingStarted();

                    publishProgress(photoMetadataBuffer.getCount());

                    for (int mAttributedPhotoId = 0;
                         mAttributedPhotoId < photoMetadataBuffer.getCount();
                         mAttributedPhotoId++) {
                        final int mAttributedPhotoIdFinal = mAttributedPhotoId;

                        // Get the first bitmap
                        PlacePhotoMetadata photo = photoMetadataBuffer
                                .get(mAttributedPhotoId)
                                .freeze();

                        // Load a scaled bitmap for this photo
                        photo
                                .freeze()
                                .getPhoto(MainActivity.getApiClient())
                                .setResultCallback(
                                        placePhotoResult -> {
                                            ((ImageView)
                                                    ((LinearLayout)
                                                            parent.viewArr.get(R.id.details_photos_layout)
                                                    ).getChildAt(mAttributedPhotoIdFinal)
                                            ).setImageBitmap(placePhotoResult.getBitmap());
                                        }
                                );
                    }
                } else {
                    // Hide photos
                    parent.setPhotosVisibility(View.GONE);
                }

                // Release the photos buffer
                photoMetadataBuffer.release();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // Create holders image views
            for (int mAttributedPhotoId = 0;
                 mAttributedPhotoId < values[0];
                 mAttributedPhotoId++) {
                // Create image view
                ImageView mPhotoView = new ImageView(parent.getActivity());
                parent.setDescriptionPhotoImageView(mPhotoView);
                mPhotoView.setImageBitmap(
                        BitmapFactory.decodeResource(
                                parent.getResources(),
                                R.drawable.pattern
                        )
                );

                // Add view
                ((LinearLayout) parent.viewArr.get(R.id.details_photos_layout))
                        .addView(mPhotoView);
            }
        }
    }
}
