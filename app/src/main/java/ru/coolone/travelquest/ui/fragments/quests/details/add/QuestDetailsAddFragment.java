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

import ru.coolone.travelquest.R;

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
        RU("RU"),
        US("US");

        private final String val;

        Lang(String val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return val;
        }
    }

    private Lang lang;
    private String placeId;

    // Description recycler view
    RecyclerView descriptionRecyclerView;

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
        descriptionRecyclerView = view.findViewById(R.id.add_details_description_recycler);
        descriptionRecyclerView.setNestedScrollingEnabled(false);
        setDetailsRecyclerView(
                descriptionRecyclerView,
                QuestDetailsAddAdapter.class,
                getContext()
        );

        refreshDetails();

        return view;
    }

    private void createTemplateDetails() {

    }

    private void refreshDetails() {
        if (placeId != null) {
            FirebaseFirestore db = FirebaseFirestore
                    .getInstance();

            CollectionReference collRef =
                    db
                            .collection(lang.val)
                            .document("quests")
                            .collection(placeId);

            // Parse description

            // Get doc
            collRef.get().addOnCompleteListener(
                    task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot coll = task.getResult();

                            // Parse description
                            parseDetails(coll,
                                    0,
                                    getView().findViewById(R.id.add_details_description_recycler),
                                    QuestDetailsAddAdapter.class,
                                    this.getContext()
                            );
                        } else createTemplateDetails();
                    })
                    .addOnFailureListener(
                            e -> createTemplateDetails()
                    );
        }
    }
}
