package ru.coolone.travelquest.ui.fragments.quests.details.add;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.adapters.BaseSectionedAdapter;
import ru.coolone.travelquest.ui.adapters.BaseSectionedHeader;

/**
 * @author coolone
 * @since 30.03.18
 */
public class QuestDetailsAddFragment extends Fragment {
    private static final String TAG = QuestDetailsAddFragment.class.getSimpleName();

    // Arguments
    public enum ArgKeys {
        PAGE("page"),
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
        RU,
        EN
    }

    private Lang lang;

    // Description recycler view
    RecyclerView descriptionRecyclerView;

    public static QuestDetailsAddFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ArgKeys.PAGE.toString(), page);
        QuestDetailsAddFragment fragment = new QuestDetailsAddFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            lang = (Lang) getArguments().getSerializable(ArgKeys.LANG.toString());
        }
    }

    private void setDescriptionRecyclerView(RecyclerView recyclerView) {
        // Recycler view
        recyclerView.setHasFixedSize(true);

        // Layout manager
        RecyclerView.LayoutManager descriptionLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(descriptionLayoutManager);

        // Adapter
        QuestDetailsAddAdapter adapter = (recyclerView.getAdapter() == null
                ? new QuestDetailsAddAdapter()
                : (QuestDetailsAddAdapter) recyclerView.getAdapter());
        adapter.setHeaderClickListener(
                new BaseSectionedAdapter.OnClickListener
                        <BaseSectionedHeader, QuestDetailsAddAdapter.HeaderHolder>() {
                    @Override
                    public void onClick(BaseSectionedHeader i,
                                        QuestDetailsAddAdapter.HeaderHolder i2,
                                        int section) {
                        Log.d(TAG, "Toggle section expanded");
                        adapter.toggleSectionExpanded(section);
                    }

                    @Override
                    public boolean onLongClick(BaseSectionedHeader i, QuestDetailsAddAdapter.HeaderHolder i2, int section) {
                        return false;
                    }

                });
        adapter.shouldShowHeadersForEmptySections(true);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_place_page, container, false);

        // Recycle view
        descriptionRecyclerView = view.findViewById(R.id.add_details_description_recycler);
        descriptionRecyclerView.setNestedScrollingEnabled(false);
        setDescriptionRecyclerView(descriptionRecyclerView);

        return view;
    }
}
