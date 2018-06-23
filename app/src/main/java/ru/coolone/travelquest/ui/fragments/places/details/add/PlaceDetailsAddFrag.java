package ru.coolone.travelquest.ui.fragments.places.details.add;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.zagum.switchicon.SwitchIconView;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.firestore.FirebaseFirestore;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.activities.MainActivity;
import ru.coolone.travelquest.ui.activities.MainActivity.SupportLang;
import ru.coolone.travelquest.ui.fragments.places.details.adapters.BaseSectionedAdapter;
import ru.coolone.travelquest.ui.fragments.places.details.adapters.BaseSectionedHeader;

import static android.app.Activity.RESULT_OK;
import static ru.coolone.travelquest.ui.fragments.places.details.FirebaseMethods.initDetailsRecyclerView;
import static ru.coolone.travelquest.ui.fragments.places.details.FirebaseMethods.parseDetailsHeaders;
import static ru.coolone.travelquest.ui.fragments.places.details.add.PlaceDetailsAddAdapter.PLACE_PICKER_REQUEST;

/**
 * @author coolone
 * @since 30.03.18
 */
@EFragment(R.layout.frag_add_details_page)
public class PlaceDetailsAddFrag extends Fragment implements PlaceDetailsAddAdapter.Listener {
    private static final String TAG = PlaceDetailsAddFrag.class.getSimpleName();

    private static final String KEY_RECYCLER_INSTANCE = "recyclerInstance";
    private static final String KEY_RECYCLER_ADAPTER = "recyclerAdapter";

    private Parcelable recyclerInstance;

    @FragmentArg
    public SupportLang lang;

    @FragmentArg
    public String placeId;

    // Description recycler view
    @ViewById(R.id.add_details_page_details_recycler)
    public RecyclerView recycler;
    public PlaceDetailsAddAdapter recyclerAdapter;

    // Add section button
    @ViewById(R.id.add_details_page_add_section_button)
    public FloatingActionButton addSectionButton;

    // Root layout
    @ViewById(R.id.add_details_page_root_layout)
    FrameLayout frameLayout;

    // Translate license text
    @ViewById(R.id.add_details_page_translate_license)
    TextView translateLicenseText;

    public boolean translateInProgress = false;
    public boolean translated = false;
    public boolean translatedChanged = false;

    @Setter
    @Getter
    private Listener listener;

    @Override
    public void sectionsChanged() {
        if (listener != null) {
            listener.onSectionsChanged(lang);
            listener.getTranslateIcon(lang).setIconEnabled(false);
        }

        if (translated)
            translatedChanged = true;
    }

    private void refreshDetails() {
        if (placeId != null) {
            val db = FirebaseFirestore
                    .getInstance();

            val docPath = new StringBuilder(MainActivity.firebaseUser.getUid());
            val userName = MainActivity.firebaseUser.getDisplayName();
            if (userName != null && !userName.isEmpty())
                docPath.append('_').append(userName);

            val collRef = db
                    .collection(lang.lang)
                    .document("quests")
                    .collection(placeId)
                    .document(docPath.toString())
                    .collection("coll");

            // Parse details

            // Show bar
            showProgressBar(getString(R.string.add_details_progress_load));

            // Get doc
            collRef.get().addOnSuccessListener(
                    task -> {
                        // Parse details
                        parseDetailsHeaders(
                                task,
                                (BaseSectionedAdapter) recycler.getAdapter(),
                                false,
                                PlaceDetailsAddFrag.this.getActivity()
                        );

                        if (listener != null)
                            listener.onSectionsLoaded(lang);
                    })
                    .addOnFailureListener(
                            e -> {
                                if (e != null && e.getLocalizedMessage() != null &&
                                        !e.getLocalizedMessage().trim().isEmpty())
                                    Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG)
                                            .show();
                            }
                    )
                    .addOnCompleteListener(
                            task -> {
                                hideProgressBar();

                                translatedChanged = false;
                            }
                    );
        }
    }

    public void restoreDetails() {
        recyclerAdapter.clear();
        recyclerAdapter.notifyDataSetChanged();
        refreshDetails();
    }

    CardView progressCard;
    TextView progressText;

    public void showProgressBar(String title) {
        if (progressText == null) {
            progressText = new TextView(getContext());
            val textParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            progressText.setLayoutParams(textParams);
            progressText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        progressText.setText(title);

        // Show bar
        if (progressCard == null) {
            val inset = (int) getResources().getDimension(R.dimen.content_inset);

            progressCard = new CardView(getContext());
            val cardParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            cardParams.gravity = Gravity.CENTER;
            progressCard.setLayoutParams(cardParams);

            val cardLayout = new LinearLayout(getContext());
            val layoutParams = new CardView.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(
                    inset, inset, inset, inset
            );
            cardLayout.setLayoutParams(layoutParams);
            cardLayout.setOrientation(LinearLayout.VERTICAL);

            val progressBar = new ProgressBar(getContext());
            val barParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            barParams.gravity = Gravity.CENTER;
            barParams.bottomMargin = inset;
            progressBar.setLayoutParams(barParams);

            cardLayout.addView(progressBar);
            cardLayout.addView(progressText);

            progressCard.addView(cardLayout);
        }

        frameLayout.addView(progressCard);
    }

    public void hideProgressBar() {
        // Hide bar
        frameLayout.removeView(progressCard);
    }

    @AfterViews
    void afterViews() {
        // Translate views
        val translateVisibility = lang == MainActivity.getLocale(getContext())
                ? View.GONE
                : View.VISIBLE;
        if (listener != null && listener.getTranslateLayout(lang) != null)
            listener.getTranslateLayout(lang)
                    .setVisibility(translateVisibility);
        translateLicenseText.setVisibility(translateVisibility);
        translateLicenseText.setMovementMethod(LinkMovementMethod.getInstance());

        // Recycle view
        initDetailsRecyclerView(
                recycler,
                PlaceDetailsAddAdapter.class,
                getActivity()
        );
        if (recyclerAdapter != null)
            ((BaseSectionedAdapter) recycler.getAdapter()).setSections(recyclerAdapter.getSections());
        recyclerAdapter = (PlaceDetailsAddAdapter) recycler.getAdapter();
        recyclerAdapter.setListener(this);

        if (recyclerInstance == null)
            refreshDetails();
        else
            recycler.getLayoutManager().onRestoreInstanceState(recyclerInstance);

        recycler.setNestedScrollingEnabled(false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_RECYCLER_INSTANCE, recycler.getLayoutManager().onSaveInstanceState());
        outState.putParcelable(KEY_RECYCLER_ADAPTER, recyclerAdapter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            recyclerInstance = savedInstanceState.getParcelable(KEY_RECYCLER_INSTANCE);
            recyclerAdapter = savedInstanceState.getParcelable(KEY_RECYCLER_ADAPTER);
        }
    }

    @Click(R.id.add_details_page_add_section_button)
    public void onAddHeaderClick() {
        recyclerAdapter.addSection(
                new Pair<>(
                        new BaseSectionedHeader(),
                        new ArrayList<>()
                )
        );

        sectionsChanged();
    }

    public interface Listener {
        void onSectionsLoaded(SupportLang fragLang);

        void onSectionsChanged(SupportLang fragLang);

        FrameLayout getTranslateLayout(SupportLang fragLang);

        SwitchIconView getTranslateIcon(SupportLang fragLang);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_PICKER_REQUEST &&
                resultCode == RESULT_OK) {
            for (int mChildId = 0; mChildId < recycler.getChildCount(); mChildId++) {
                val mChild = recycler.getChildAt(mChildId);
                val mChildHolder = recycler.getChildViewHolder(mChild);

                if (mChildHolder instanceof PlaceDetailsAddAdapter.ItemHolderText) {
                    val mChildText = (PlaceDetailsAddAdapter.ItemHolderText) mChildHolder;

                    if (mChildText.placeSelectedListener != null) {
                        mChildText.placeSelectedListener.onPlaceSelected(
                                PlacePicker.getPlace(getContext(), data)
                        );
                    }
                }
            }
        }
    }
}
