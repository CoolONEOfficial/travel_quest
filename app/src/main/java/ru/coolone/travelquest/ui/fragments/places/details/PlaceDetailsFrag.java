package ru.coolone.travelquest.ui.fragments.places.details;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.activities.AddDetailsActivity_;
import ru.coolone.travelquest.ui.activities.MainActivity;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static ru.coolone.travelquest.ui.fragments.places.details.FirebaseMethods.initDetailsRecyclerView;
import static ru.coolone.travelquest.ui.fragments.places.details.FirebaseMethods.parseDetailsCards;

@EFragment
public class PlaceDetailsFrag extends Fragment {

    static final String TAG = PlaceDetailsFrag.class.getSimpleName();

    @Setter
    private FragmentListener fragmentListener;

    @ViewById(R.id.details_details_recycler)
    public RecyclerView detailsRecyclerView;

    @ViewById(R.id.details_details_add_button)
    FloatingActionButton detailsAddButton;

    @FragmentArg
    @Getter
    String title;
    @ViewById(R.id.details_title)
    TextView titleView;

    @FragmentArg
    @Getter
    String placeId;

    @FragmentArg
    @Getter
    String phone;
    @ViewById(R.id.details_phone)
    TextView phoneView;

    @FragmentArg
    @Getter
    String url;
    @ViewById(R.id.details_url)
    TextView urlView;

    @FragmentArg
    @Getter
    float rating;
    @ViewById(R.id.details_rating)
    TextView ratingView;
    @ViewById(R.id.details_rating_star)
    TextView ratingStarView;

    @FragmentArg
    @Getter
    String[] types;
    @ViewById(R.id.details_types)
    TextView typesView;

    @ViewById(R.id.details_bottom)
    LinearLayout bottom;
    @ViewById(R.id.details_bottom_delimiter)
    View bottomDelimiter;

    @ViewById(R.id.details_details_unknown_text)
    TextView detailsUnknownText;

    @ViewById(R.id.details_details_unknown_text_primary)
    TextView detailsUnknownTextPrimary;

    @ViewById(R.id.details_details_unknown_text_smile)
    ImageView detailsUnknownTextSmile;

    @ViewById(R.id.details_photos_layout)
    LinearLayout photosLayout;

    @ViewById(R.id.details_photos_scroll)
    HorizontalScrollView photosScroll;

    @ViewById(R.id.details_layout_body)
    RelativeLayout bodyLayout;

    @AfterViews
    void afterViews() {
        titleView.setSelected(true);

        // Recycler view
        initDetailsRecyclerView(detailsRecyclerView, PlaceCardDetailsAdapter.class, getActivity());

        // Add details button
        detailsAddButton.setVisibility(
                (MainActivity.firebaseUser != null) ?
                        MainActivity.firebaseUser.isAnonymous() ?
                                View.GONE
                                : View.VISIBLE
                        : View.GONE
        );

        setTitle(title);
        setPhone(phone);
        setUrl(url);
        setRating(rating);
        setTypes(types);

        // Photos
        new PhotoTask(this).execute(placeId);
    }

    @Override
    public void onResume() {
        detailsAddButton.setEnabled(true);

        super.onResume();
    }

    @Click(R.id.details_details_add_button)
    void onAddButtonClicked() {
        detailsAddButton.setEnabled(false);

        // To add place activity
        AddDetailsActivity_
                .intent(getContext())
                .placeId(placeId)
                .startForResult(REQUEST_CODE_ADD_DETAILS);
    }

    /**
     * Activities request codes
     */
    public static final int REQUEST_CODE_ADD_DETAILS = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag_place_details,
                container,
                false);

        // Call listener
        if (fragmentListener != null)
            fragmentListener.onQuestDetailsCreateView(view, container, savedInstanceState);

        return view;
    }

    /**
     * @param place   Google maps place with details data
     * @param context @{@link Context}
     * @return @{@link PlaceDetailsFrag}
     */
    public static PlaceDetailsFrag newInstance(com.google.android.gms.location.places.Place place, Context context) {
        // Convert List<Place types ids> to List<Place types>
        List<Integer> placeTypeIds = place.getPlaceTypes();
        ArrayList<String> placeTypes = new ArrayList<>();
        for (int mPlaceTypeIdId = 0; mPlaceTypeIdId < placeTypeIds.size(); mPlaceTypeIdId++) {
            Integer mPlaceId = place.getPlaceTypes().get(mPlaceTypeIdId);
            String mPlaceType = placeTypeIdToString(context, mPlaceId);
            if (mPlaceType != null)
                placeTypes.add(mPlaceType);
        }

        return PlaceDetailsFrag_.builder()
                .title(place.getName().toString())
                .phone(place.getPhoneNumber().toString())
                .url(place.getWebsiteUri() != null
                        ? place.getWebsiteUri().toString()
                        : null)
                .rating(place.getRating())
                .types(placeTypes.toArray(new String[0]))
                .placeId(place.getId())
                .build();
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

    private void refreshBottomInfoVisibility() {
        setBottomInfoVisibility(
                (urlView.getText().length() > 0 ||
                        phoneView.getText().length() > 0)
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    private void setBottomInfoVisibility(int visibility) {
        bottom.setVisibility(visibility);
        bottomDelimiter.setVisibility(visibility);
    }

    private void setRatingVisibility(int visibility) {
        ratingView.setVisibility(visibility);
        ratingStarView.setVisibility(visibility);
    }

    private void setDescriptionVisibility(int visibility) {
        val errorVisibility = (visibility == View.VISIBLE
                ? View.GONE
                : View.VISIBLE);

        // Description visibility
        detailsRecyclerView.setVisibility(visibility);

        // Error visibility
        detailsUnknownText.setVisibility(errorVisibility);
        detailsUnknownTextPrimary.setVisibility(errorVisibility);
        detailsUnknownTextPrimary.setText("");
        detailsUnknownTextSmile.setVisibility(errorVisibility);
    }

    private void detailsError(String errStr) {
        // Hide details and show errors
        setDescriptionVisibility(View.GONE);

        // Set error text
        detailsUnknownTextPrimary.setText(errStr);
    }

    private void detailsError(Exception e) {
        detailsError(e.getLocalizedMessage());
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
        photosLayout.setVisibility(visibility);
        photosScroll.setVisibility(visibility);
    }

    public void setTitle(String title) {
        this.title = title;
        titleView.setText(title);
    }

    public void setPhone(String phone) {
        this.phone = phone;
        phoneView.setText(phone);

        // Set visibility
        boolean visibility = (phone != null);
        phoneView.setVisibility(
                visibility
                        ? View.VISIBLE
                        : View.GONE
        );

        // Set delimiter visibility
        refreshBottomInfoVisibility();
    }

    public void setUrl(String url) {
        this.url = url;
        urlView.setText(url);

        // Set url visibility
        boolean visibility = (url != null);
        urlView.setVisibility(
                visibility
                        ? View.VISIBLE
                        : View.GONE
        );

        // Set delimiter visibility
        refreshBottomInfoVisibility();
    }

    public void setRating(float rating) {
        this.rating = rating;
        ratingView.setText(new DecimalFormat("#.#").format(rating));

        // Set rating visibility
        int visibility = (rating == -1
                ? View.GONE
                : View.VISIBLE
        );
        setRatingVisibility(visibility);
    }

    static boolean introStarted = false;

    public void setPlaceId(String placeId) {
        this.placeId = placeId;

        // Clear cards
        val adapter = (PlaceCardDetailsAdapter) detailsRecyclerView.getAdapter();
        adapter.dataset.clear();

        val collRef =
                MainActivity.getQuestsRoot(MainActivity.getLocale(getContext()).lang)
                        .collection(placeId);

        // Parse details

        // Show progress bar
        val bar = new ProgressBar(getContext());
        val params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.addRule(
                RelativeLayout.CENTER_IN_PARENT
        );
        bar.setLayoutParams(params);
        bodyLayout.addView(bar);

        // Get doc
        collRef.get().addOnSuccessListener(
                task -> {
                    // Show details
                    setDescriptionVisibility(View.VISIBLE);

                    // Parse details
                    parseDetailsCards(
                            task,
                            adapter,
                            getActivity(),
                            new FirebaseMethods.TaskListener() {
                                @Override
                                public void onTaskSuccess() {
                                    if (!introStarted) {
                                        // Intro
                                        val config = new ShowcaseConfig();
                                        config.setDelay(100);

                                        val sequence = new MaterialShowcaseSequence(getActivity(), TAG);
                                        sequence.setConfig(config);

                                        val skipButton = getString(R.string.add_details_intro_dismiss_button);
                                        val logon = !MainActivity.firebaseUser.isAnonymous();

                                        val firstAdapter = detailsRecyclerView.findViewHolderForAdapterPosition(0);
                                        if (firstAdapter != null) {
                                            sequence.addSequenceItem(
                                                    firstAdapter.itemView,
                                                    getString(R.string.details_intro_details),
                                                    skipButton
                                            );

                                            sequence.addSequenceItem(
                                                    firstAdapter.itemView
                                                            .findViewById(R.id.card_details_star),
                                                    getString(
                                                            logon
                                                                    ? R.string.details_intro_star_registered
                                                                    : R.string.details_intro_star_not_registered
                                                    ),
                                                    skipButton
                                            );

                                            sequence.addSequenceItem(
                                                    logon
                                                            ? detailsAddButton
                                                            : new View(getContext()),
                                                    getString(
                                                            logon
                                                                    ? R.string.details_intro_add_registered
                                                                    : R.string.details_intro_add_not_registered
                                                    ),
                                                    skipButton
                                            );

                                            introStarted = true;
                                            sequence.start();
                                        }
                                    }
                                }

                                @Override
                                public void onTaskError(Exception e) {
                                    detailsError(e);
                                }

                                @Override
                                public void onTaskCompleted() {
                                }
                            }
                    );
                })
                .addOnFailureListener(this::detailsError)
                .addOnCompleteListener(
                        task -> bodyLayout.removeView(bar)
                );
    }

    public void setTypes(String[] types) {
        this.types = types;

        // - Get type string -
        String typeStr = TextUtils.join(", ", types);

        // - Set type string -

        // Set visibility
        int visibility = (typeStr == null || typeStr.isEmpty())
                ? View.GONE
                : View.VISIBLE;
        typesView.setVisibility(visibility);

        // Set text
        typesView.setText(typeStr);
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

    public interface FragmentListener {
        void onQuestDetailsCreateView(
                @NonNull View view,
                ViewGroup container,
                Bundle savedInstanceState
        );

        void onPhotosLoadingStarted();
    }

    static private class PhotoTask extends AsyncTask<String, Integer, Void> {

        PlaceDetailsFrag parent;

        PhotoTask(PlaceDetailsFrag parent) {
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
                        val mAttributedPhotoIdFinal = mAttributedPhotoId;

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
                                            if (parent.photosLayout != null)
                                                ((ImageView)
                                                        parent.photosLayout.getChildAt(mAttributedPhotoIdFinal)
                                                ).setImageBitmap(placePhotoResult.getBitmap());
                                        }
                                );
                    }
                } else {
                    // Hide photos
                    parent.getActivity().runOnUiThread(
                            () -> parent.setPhotosVisibility(View.GONE)
                    );
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
                parent.photosLayout.addView(mPhotoView);
            }
        }
    }
}
