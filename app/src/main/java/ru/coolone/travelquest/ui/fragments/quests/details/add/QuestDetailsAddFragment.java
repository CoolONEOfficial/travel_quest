package ru.coolone.travelquest.ui.fragments.quests.details.add;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.adapters.BaseSectionedAdapter;
import ru.coolone.travelquest.ui.adapters.BaseSectionedHeader;
import ru.coolone.travelquest.ui.fragments.quests.details.items.BaseQuestDetailsItem;
import ru.coolone.travelquest.ui.fragments.quests.details.items.QuestDetailsItemText;

import static ru.coolone.travelquest.ui.fragments.quests.details.QuestDetailsFragment.parseDetails;
import static ru.coolone.travelquest.ui.fragments.quests.details.QuestDetailsFragment.setDetailsRecyclerView;

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

    enum Lang {
        RUSSIAN("RU", R.string.add_place_tab_russian_title),
        ENGLISH("US", R.string.add_place_tab_english_title);

        public final String lang;
        public final int titleId;

        Lang(String lang, int titleId) {
            this.lang = lang;
            this.titleId = titleId;
        }

        @Override
        public String toString() {
            return lang;
        }
    }

    public Lang lang;
    public String placeId;

    // Description recycler view
    RecyclerView detailsRecyclerView;

    public static QuestDetailsAddFragment newInstance(Lang lang, String placeId) {
        Bundle args = new Bundle();
        args.putSerializable(ArgKeys.LANG.toString(), lang);
        args.putString(ArgKeys.PLACE_ID.toString(), placeId);
        QuestDetailsAddFragment fragment = new QuestDetailsAddFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            lang = (Lang) args.getSerializable(ArgKeys.LANG.toString());
            placeId = args.getString(ArgKeys.PLACE_ID.toString());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_place_page, container, false);

        // Recycle view
        detailsRecyclerView = view.findViewById(R.id.add_details_details_recycler);
        detailsRecyclerView.setNestedScrollingEnabled(false);
        setDetailsRecyclerView(
                detailsRecyclerView,
                QuestDetailsAddAdapter.class,
                getContext()
        );

        refreshDetails();

        return view;
    }

    private void createTemplateDetails() {
        QuestDetailsAddAdapter adapter = (QuestDetailsAddAdapter) detailsRecyclerView.getAdapter();

        adapter.addSection(
                new BaseSectionedHeader("TITLE"),
                new ArrayList<BaseQuestDetailsItem>() {{
                    add(new QuestDetailsItemText("TEXT"));
                }}
        );
    }

    private void refreshDetails() {
        if (placeId != null) {
            FirebaseFirestore db = FirebaseFirestore
                    .getInstance();

            CollectionReference collRef =
                    db
                            .collection(lang.lang)
                            .document("quests")
                            .collection(placeId);

            // Parse details

            // Get doc
            collRef.get().addOnCompleteListener(
                    task -> {
                        if (task.isSuccessful()) {
                            // Parse details
                            if(!parseDetails(task.getResult(),
                                    0,
                                    getView().findViewById(R.id.add_details_details_recycler),
                                    QuestDetailsAddAdapter.class,
                                    this.getContext()
                            ))
                                createTemplateDetails();
                        } else createTemplateDetails();
                    })
                    .addOnFailureListener(
                            e -> createTemplateDetails()
                    );
        }
    }
}
