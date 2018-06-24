package ru.coolone.travelquest.ui.fragments.places.details;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.places.Place;
import com.stfalcon.frescoimageviewer.ImageViewer;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.activities.AddDetailsActivity_;
import ru.coolone.travelquest.ui.activities.MainActivity;
import ru.coolone.travelquest.ui.fragments.places.PlacesFrag;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static ru.coolone.travelquest.ui.fragments.places.details.FirebaseMethods.initDetailsRecyclerView;
import static ru.coolone.travelquest.ui.fragments.places.details.FirebaseMethods.parseDetailsCards;

@EFragment
public class PlaceDetailsFrag extends Fragment {

    static final String TAG = PlaceDetailsFrag.class.getSimpleName();

    @Setter
    private FragmentListener parentListener;

    @ViewById(R.id.details_details_recycler)
    public RecyclerView detailsRecyclerView;

    @ViewById(R.id.details_details_add_button)
    FloatingActionButton detailsAddButton;

    // Scroll view
    @ViewById(R.id.details_details_scroll)
    public ScrollView rootScrollView;

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

    PhotoTask photoTask;

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
        photoTask = new PhotoTask(this);
        photoTask.execute(placeId);
    }

    @Override
    public void onDetach() {
        if (photoTask != null)
            photoTask.cancel(true);

        super.onDetach();
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
        if (parentListener != null)
            parentListener.onQuestDetailsCreateView(view, container, savedInstanceState);

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
        phoneView.setVisibility(visibility);
        urlView.setVisibility(visibility);
        bottomDelimiter.setVisibility(visibility);
    }

    private void setRatingVisibility(int visibility) {
        ratingView.setVisibility(visibility);
        ratingStarView.setVisibility(visibility);
    }

    private void setDescriptionVisibility(int visibility) {
        if(isAdded()) {
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
        } else getView().post(
                () -> setDescriptionVisibility(visibility)
        );
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

    private void setPhotoImageView(ImageView imageView) {
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
        if(detailsRecyclerView != null) {
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
                                        val sequence = new MaterialShowcaseSequence(getActivity(), TAG);

                                        if (!sequence.hasFired() && !introStarted) {
                                            introStarted = true;

                                            val skipButton = getString(R.string.add_details_intro_dismiss_button);

                                            val firstAdapter = detailsRecyclerView.findViewHolderForAdapterPosition(0);
                                            if (firstAdapter != null) {
                                                MainActivity.sequenceItems.clear();

                                                MainActivity.addIntroItem(
                                                        getActivity(),
                                                        firstAdapter.itemView,
                                                        getString(R.string.details_intro_details),
                                                        skipButton
                                                );

                                                val logon = !MainActivity.firebaseUser.isAnonymous();

                                                MainActivity.addIntroItem(
                                                        getActivity(),
                                                        firstAdapter.itemView
                                                                .findViewById(R.id.card_details_star),
                                                        getString(
                                                                logon
                                                                        ? R.string.details_intro_star_registered
                                                                        : R.string.details_intro_star_not_registered
                                                        ),
                                                        skipButton
                                                );

                                                MainActivity.addIntroItem(
                                                        getActivity(),
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

                                                // Intro
                                                val config = new ShowcaseConfig();
                                                config.setDelay(100);

                                                sequence.setConfig(config);

                                                for (val mItem : MainActivity.sequenceItems)
                                                    sequence.addSequenceItem(mItem);

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
        } else getView().post(
                () -> setPlaceId(placeId)
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
        if (parentListener == null) {
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

        PlacesFrag getPlacesFrag();
    }

    @RequiredArgsConstructor
    static private class PhotoTask extends AsyncTask<String, Void, Void> {

        @AllArgsConstructor
        static class PlacePhoto {
            @Getter
            String url;
            @Getter
            String description;
        }

        final PlaceDetailsFrag parentFrag;

        ArrayList<PlacePhoto> viewerPhotos = new ArrayList<>();

        RequestQueue queue;

        @Override
        protected void onCancelled() {
            if (queue != null)
                queue.cancelAll(TAG);
        }

        @Override
        protected Void doInBackground(String... params) {
            if (params.length != 1) {
                return null;
            }
            val placeId = params[0];

            queue = Volley.newRequestQueue(parentFrag.getContext());

            val key = parentFrag.getContext().getString(R.string.GOOGLE_MAPS_API_KEY);
            queue.add(new JsonObjectRequest(
                    new Uri.Builder()
                            .scheme("https")
                            .authority("maps.googleapis.com")
                            .appendPath("maps")
                            .appendPath("api")
                            .appendPath("place")
                            .appendPath("details")
                            .appendPath("json")
                            .appendQueryParameter("key", key)
                            .appendQueryParameter("placeid", placeId)
                            .toString(),
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        @SneakyThrows
                        public void onResponse(JSONObject response) {
                            if (response.getString("status").equals("OK")) {
                                val result = response.getJSONObject("result");
                                if (result.has("photos")) {
                                    val photosArray = result.getJSONArray("photos");
                                    val photosLinks = new String[photosArray.length()];

                                    val display = parentFrag.getActivity().getWindowManager().getDefaultDisplay();
                                    val screenResolution = new Point();
                                    display.getSize(screenResolution);

                                    parentFrag.parentListener.onPhotosLoadingStarted();

                                    for (int mPhotoId = 0; mPhotoId < photosArray.length(); mPhotoId++) {
                                        val mPhoto = photosArray.getJSONObject(mPhotoId);

                                        val mLink = new Uri.Builder()
                                                .scheme("https")
                                                .authority("maps.googleapis.com")
                                                .appendPath("maps")
                                                .appendPath("api")
                                                .appendPath("place")
                                                .appendPath("photo")
                                                .appendQueryParameter("key", key)
                                                .appendQueryParameter(
                                                        "photoreference",
                                                        mPhoto.getString("photo_reference")
                                                )
                                                .toString();

                                        val authorsJson = mPhoto.getJSONArray("html_attributions");
                                        val authors = new ArrayList<String>();
                                        for (int mAuthorId = 0; mAuthorId < authorsJson.length(); mAuthorId++) {
                                            authors.add(authorsJson.getString(mAuthorId));
                                        }

                                        viewerPhotos.add(new PlacePhoto(
                                                mLink + (
                                                        screenResolution.x < screenResolution.y
                                                                ? "&maxwidth=" + Integer.toString(Math.min(1600, screenResolution.x))
                                                                : "&maxheight=" + Integer.toString(Math.min(1600, screenResolution.y))
                                                ),
                                                TextUtils.join(
                                                        ", ", authors
                                                )
                                        ));

                                        photosLinks[mPhotoId] = mLink + "&maxheight=" + Integer.toString(
                                                (int) parentFrag.getContext().getResources()
                                                        .getDimension(R.dimen.details_photos_size_anchored)
                                        );
                                    }

                                    for (int mPhotoLinkId = 0; mPhotoLinkId < photosLinks.length; mPhotoLinkId++) {
                                        val mPhotoLinkIdFinal = mPhotoLinkId;
                                        val mPhotoLink = photosLinks[mPhotoLinkId];

                                        publishProgress();

                                        queue.add(
                                                new ImageRequest(
                                                        mPhotoLink,
                                                        imageResponse -> {
                                                            if (parentFrag.photosLayout != null &&
                                                                    parentFrag.photosLayout.getChildCount() > mPhotoLinkIdFinal)
                                                                (
                                                                        (ImageView) parentFrag.photosLayout
                                                                                .getChildAt(mPhotoLinkIdFinal)
                                                                ).setImageBitmap(imageResponse);
                                                        },
                                                        0, 0,
                                                        null, null,
                                                        error -> Log.e(TAG, "Error while get google maps photo bitmap")
                                                )
                                        );
                                    }
                                } else
                                    // Hide photos
                                    parentFrag.getActivity().runOnUiThread(
                                            () -> parentFrag.setPhotosVisibility(View.GONE)
                                    );
                            } else
                                Log.e(TAG, "Error in response while get google maps photo links." +
                                        "Status " + response.getString("status"));
                        }
                    },
                    error -> Log.e(TAG, "Error while get google maps photo links", error)
            ));

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            // Create holder image view
            val mPhotoView = new ImageView(parentFrag.getActivity());
            parentFrag.setPhotoImageView(mPhotoView);
            val imageCount = parentFrag.photosLayout.getChildCount();
            mPhotoView.setOnClickListener(
                    v -> {
                        if (viewerPhotos != null) {
                            val overlayView = new ImageOverlayView(parentFrag.getContext());

                            new ImageViewer.Builder<>(parentFrag.getContext(), viewerPhotos)
                                    .setFormatter(PlacePhoto::getUrl)
                                    .setImageChangeListener(
                                            position -> {
                                                val image = viewerPhotos.get(position);
                                                overlayView.setDescription(image.getDescription());
                                                parentFrag.photosScroll.scrollTo(
                                                        parentFrag.photosLayout.getChildAt(position).getLeft()
                                                                + parentFrag.photosLayout.getChildAt(position).getWidth() / 2
                                                                - parentFrag.photosScroll.getWidth() / 2,
                                                        0
                                                );
                                            }
                                    )
                                    .setOverlayView(overlayView)
                                    .setStartPosition(imageCount)
                                    .show();
                        } else Toast.makeText(
                                parentFrag.getContext(),
                                parentFrag.getContext().getString(R.string.details_photos_not_loaded),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
            );

            // Add view
            parentFrag.photosLayout.addView(mPhotoView);

            parentFrag.parentListener.getPlacesFrag()
                    .invalidateSlidingPanel();
        }

        static class ImageOverlayView extends RelativeLayout {
            private TextView description;

            public ImageOverlayView(Context context) {
                super(context);

                val view = inflate(getContext(), R.layout.frag_place_details_photo_overlay, this);
                description = view.findViewById(R.id.overlay_description);
                description.setMovementMethod(LinkMovementMethod.getInstance());
            }

            public void setDescription(String description) {
                this.description.setText(Html.fromHtml(description));
            }
        }
    }
}
