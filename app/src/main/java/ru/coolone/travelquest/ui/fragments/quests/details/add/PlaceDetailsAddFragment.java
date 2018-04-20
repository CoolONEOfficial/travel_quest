package ru.coolone.travelquest.ui.fragments.quests.details.add;

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
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

import lombok.val;
import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.activities.MainActivity.SupportLang;
import ru.coolone.travelquest.ui.adapters.BaseSectionedHeader;
import ru.coolone.travelquest.ui.fragments.quests.details.items.BaseQuestDetailsItem;
import ru.coolone.travelquest.ui.fragments.quests.details.items.QuestDetailsItemText;

import static ru.coolone.travelquest.ui.fragments.quests.details.FirebaseMethods.parseDetails;
import static ru.coolone.travelquest.ui.fragments.quests.details.FirebaseMethods.setDetailsRecyclerView;

/**
 * @author coolone
 * @since 30.03.18
 */
@EFragment(R.layout.fragment_add_details_page)
public class PlaceDetailsAddFragment extends Fragment {
    private static final String TAG = PlaceDetailsAddFragment.class.getSimpleName();

    // Arguments
    public enum ArgKeys {
        PLACE_ID("placeId"),
        LANG("lang");

        private final String val;

        ArgKeys(String val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return val;
        }
    }

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
    FloatingActionButton addSectionButton;

    // Root layout
    @ViewById(R.id.add_details_page_root_layout)
    FrameLayout frameLayout;

    @AfterViews
    void afterViews() {
        // Recycle view
        setDetailsRecyclerView(
                recycler,
                PlaceDetailsAddAdapter.class,
                getContext()
        );
        recyclerAdapter = (PlaceDetailsAddAdapter) recycler.getAdapter();

        // Add section button
        addSectionButton.setOnClickListener(
                v -> recyclerAdapter.addSection(
                        new Pair<>(
                                new BaseSectionedHeader(),
                                new ArrayList<>()
                        )
                )
        );

        refreshDetails();
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
                            .collection(placeId);

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
                        if (!parseDetails(
                                task,
                                recycler,
                                this.getContext()
                        ))
                            createTemplateDetails();

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
