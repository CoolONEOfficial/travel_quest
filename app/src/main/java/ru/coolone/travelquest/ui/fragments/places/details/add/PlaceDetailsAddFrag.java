package ru.coolone.travelquest.ui.fragments.places.details.add;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.zagum.switchicon.SwitchIconView;
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
import ru.coolone.travelquest.ui.adapters.BaseSectionedAdapter;
import ru.coolone.travelquest.ui.adapters.BaseSectionedHeader;

import static ru.coolone.travelquest.ui.fragments.places.details.FirebaseMethods.initDetailsRecyclerView;
import static ru.coolone.travelquest.ui.fragments.places.details.FirebaseMethods.parseDetailsHeaders;

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
    PlaceDetailsAddAdapter recyclerAdapter;

    // Add section button
    @ViewById(R.id.add_details_page_add_section_button)
    public FloatingActionButton addSectionButton;

    // Root layout
    @ViewById(R.id.add_details_page_root_layout)
    FrameLayout frameLayout;

    // Translate license text
    @ViewById(R.id.add_details_page_translate_license)
    TextView translateLicenseText;

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

    public interface Listener {
        void onSectionsLoaded();

        void onSectionsChanged(SupportLang fragLang);

        FrameLayout getTranslateLayout(SupportLang fragLang);

        SwitchIconView getTranslateIcon(SupportLang fragLang);
    }

    public void restoreDetails() {
        recyclerAdapter.clear();
        refreshDetails();
        sectionsChanged();
    }

    ProgressBar progressBar;

    public void showProgressBar() {
        // Show bar
        if (progressBar == null) {
            progressBar = new ProgressBar(getContext());
            val params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.CENTER;
            progressBar.setLayoutParams(params);
        }

        frameLayout.addView(progressBar);
    }

    public void hideProgressBar() {
        // Hide bar
        frameLayout.removeView(progressBar);
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
                getContext()
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
    void onAddHeaderClick() {
        recyclerAdapter.addSection(
                new Pair<>(
                        new BaseSectionedHeader(),
                        new ArrayList<>()
                )
        );

        sectionsChanged();
    }

    private void refreshDetails() {
        if (placeId != null) {
            val db = FirebaseFirestore
                    .getInstance();

            val collRef =
                    db
                            .collection(lang.lang)
                            .document("quests")
                            .collection(placeId)
                            .document(
                                    MainActivity.firebaseUser.getUid() +
                                            '_' +
                                            MainActivity.firebaseUser.getDisplayName()
                            )
                            .collection("coll");

            // Parse details

            // Show bar
            showProgressBar();

            // Get doc
            collRef.get().addOnSuccessListener(
                    task -> {
                        // Parse details
                        parseDetailsHeaders(
                                task,
                                (BaseSectionedAdapter) recycler.getAdapter(),
                                false,
                                this.getContext()
                        );

                        if (listener != null)
                            listener.onSectionsLoaded();
                    })
                    .addOnFailureListener(
                            e -> Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show()
                    )
                    .addOnCompleteListener(
                            task -> hideProgressBar()
                    );
        }
    }
}
