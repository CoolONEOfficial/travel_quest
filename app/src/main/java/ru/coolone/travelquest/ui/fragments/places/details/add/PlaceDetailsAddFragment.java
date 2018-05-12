package ru.coolone.travelquest.ui.fragments.places.details.add;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.google.firebase.firestore.FirebaseFirestore;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

import lombok.val;
import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.activities.MainActivity;
import ru.coolone.travelquest.ui.activities.MainActivity.SupportLang;
import ru.coolone.travelquest.ui.adapters.BaseSectionedAdapter;
import ru.coolone.travelquest.ui.adapters.BaseSectionedHeader;
import ru.coolone.travelquest.ui.fragments.places.details.items.BaseQuestDetailsItem;
import ru.coolone.travelquest.ui.fragments.places.details.items.QuestDetailsItemText;

import static ru.coolone.travelquest.ui.fragments.places.details.FirebaseMethods.initDetailsRecyclerView;
import static ru.coolone.travelquest.ui.fragments.places.details.FirebaseMethods.parseDetailsHeaders;

/**
 * @author coolone
 * @since 30.03.18
 */
@EFragment(R.layout.fragment_add_details_page)
public class PlaceDetailsAddFragment extends Fragment {
    private static final String TAG = PlaceDetailsAddFragment.class.getSimpleName();

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

    private ArrayList<Listener> listeners = new ArrayList<>();

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public interface Listener {
        void onSectionsLoaded();
    }

    public void restoreDetails() {
        recyclerAdapter.clear();
        refreshDetails();
    }

    @AfterViews
    void afterViews() {
        // Recycle view
        initDetailsRecyclerView(
                recycler,
                PlaceDetailsAddAdapter.class,
                getContext()
        );
        if(recyclerAdapter != null)
            ((BaseSectionedAdapter)recycler.getAdapter()).setSections(recyclerAdapter.getSections());
        recyclerAdapter = (PlaceDetailsAddAdapter) recycler.getAdapter();

        if (recyclerInstance == null)
            refreshDetails();
        else {
            recycler.getLayoutManager().onRestoreInstanceState(recyclerInstance);
        }
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
    }

    private void createTemplateDetails() {
        recyclerAdapter.addSection(
                new Pair<>(
                        new BaseSectionedHeader(),
                        new ArrayList<BaseQuestDetailsItem>() {{
                            add(new QuestDetailsItemText());
                        }}
                )
        );
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
            val bar = new ProgressBar(getContext());
            val params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.CENTER;
            bar.setLayoutParams(params);

            frameLayout.addView(bar);

            // Get doc
            collRef.get().addOnSuccessListener(
                    task -> {
                        // Parse details
                        if (!parseDetailsHeaders(
                                task,
                                (BaseSectionedAdapter) recycler.getAdapter(),
                                this.getContext()
                        )) {
                            createTemplateDetails();
                        }

                        for (val mListener : listeners)
                            if (mListener != null)
                                mListener.onSectionsLoaded();
                    })
                    .addOnFailureListener(
                            e -> createTemplateDetails()
                    )
                    .addOnCompleteListener(
                            task -> frameLayout.removeView(bar)
                    );
        }
    }
}
