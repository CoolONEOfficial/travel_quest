package ru.coolone.travelquest.ui.fragments.quests.details.add;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.FirebaseFirestore;

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
public class QuestDetailsAddFragment extends Fragment {
    private static final String TAG = QuestDetailsAddFragment.class.getSimpleName();

    // Arguments
    public enum ArgKeys {
        PLACE_ID("place_id"),
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

    public SupportLang lang;
    public String placeId;

    // Description recycler view
    public RecyclerView recycler;
    QuestDetailsAddAdapter recyclerAdapter;

    // Add section button
    FloatingActionButton addSectionButton;

    public static QuestDetailsAddFragment newInstance(SupportLang lang, String placeId) {
        val args = new Bundle();
        args.putSerializable(ArgKeys.LANG.toString(), lang);
        args.putString(ArgKeys.PLACE_ID.toString(), placeId);
        val fragment = new QuestDetailsAddFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        val args = getArguments();
        if (args != null) {
            lang = (SupportLang) args.getSerializable(ArgKeys.LANG.toString());
            placeId = args.getString(ArgKeys.PLACE_ID.toString());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        val view = inflater.inflate(R.layout.activity_add_place_page, container, false);

        // Recycle view
        recycler = view.findViewById(R.id.add_details_details_recycler);
        recycler.setNestedScrollingEnabled(false);
        setDetailsRecyclerView(
                recycler,
                QuestDetailsAddAdapter.class,
                getContext()
        );
        recyclerAdapter = (QuestDetailsAddAdapter) recycler.getAdapter();

        // Add section button
        addSectionButton = view.findViewById(R.id.add_details_add_section_button);
        addSectionButton.setOnClickListener(
                v -> recyclerAdapter.addSection(
                        new Pair<>(
                                new BaseSectionedHeader(""),
                                new ArrayList<>()
                        )
                )
        );

        refreshDetails();

        return view;
    }

    private void createTemplateDetails() {
        recyclerAdapter.addSection(
                new Pair<>(
                        new BaseSectionedHeader(""),
                        new ArrayList<BaseQuestDetailsItem>() {{
                            add(new QuestDetailsItemText(""));
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
                    );
        }
    }
}
