package ru.coolone.travelquest.ui.fragments.quests.details.add;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.coolone.travelquest.R;

/**
 * @author coolone
 * @since 30.03.18
 */
public class QuestDetailsAddFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    public static final String ARG_LANG = "ARG_LANG";

    enum Lang {
        RU,
        EN
    }

    private Lang lang;

    public static QuestDetailsAddFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        QuestDetailsAddFragment fragment = new QuestDetailsAddFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            lang = (Lang) getArguments().getSerializable(ARG_LANG);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_add_place_page, container, false);

        return view;
    }
}
