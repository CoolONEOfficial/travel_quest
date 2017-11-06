package ru.coolone.travelquest.fragments.quests;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;

import java.util.ArrayList;
import java.util.Arrays;

import ru.coolone.travelquest.R;

public class QuestDetailsFragment extends Fragment {

    // Arguments
    enum ArgKeys {
        ARG_TITLE("title"),
        ARG_PHONE("phone"),
        ARG_URL("url"),
        ARG_RATING("rating"),
        ARG_TYPES("types");

        private final String val;

        ArgKeys(String val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return val;
        }
    }

    // Title
    private String title;

    // Phone
    private String phone;

    // Url
    private Uri url;

    // Rating
    private float rating;

    // Types
    private ArrayList<Integer> types;

    // Views
    private SparseArray<View> viewArr = new SparseArray<>();

    public QuestDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title  Title.
     * @param phone  Phone number.
     * @param url    Website url.
     * @param rating Rating.
     * @param types  Array of type ID's (Integers)
     * @return A new instance of fragment QuestDetailsFragment.
     */
    public static QuestDetailsFragment newInstance(
            Context parent,
            String title,
            String phone,
            Uri url,
            float rating,
            ArrayList<Integer> types) {
        // Create quest
        QuestDetailsFragment fragment = new QuestDetailsFragment();

        // Put arguments
        Bundle args = new Bundle();
        args.putString(ArgKeys.ARG_TITLE.toString(), title);
        args.putString(ArgKeys.ARG_PHONE.toString(), phone);
        args.putString(ArgKeys.ARG_URL.toString(),
                url != null
                ? url.toString()
                : parent.getResources().getString(R.string.url_unknown));
        args.putFloat(ArgKeys.ARG_RATING.toString(), rating);
        args.putIntegerArrayList(ArgKeys.ARG_TYPES.toString(), types);
        fragment.setArguments(args);

        return fragment;
    }

    public static QuestDetailsFragment newInstance(Context parent,
            Place place) {
        return newInstance(
                parent,
                place.getName().toString(),
                place.getPhoneNumber().toString(),
                place.getWebsiteUri(),
                place.getRating(),
                new ArrayList<>(Arrays.asList(place.getPlaceTypes().toArray(new Integer[0])))
        );
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        if (args != null) {
            // Get arguments
            title = args.getString(ArgKeys.ARG_TITLE.toString());
            phone = args.getString(ArgKeys.ARG_PHONE.toString());
            url = Uri.parse(args.getString(ArgKeys.ARG_URL.toString()));
            rating = args.getFloat(ArgKeys.ARG_RATING.toString());
            types = args.getIntegerArrayList(ArgKeys.ARG_TYPES.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_quest_details,
                container,
                false);

        // Get views
        final int[] viewIdArr = new int[]{
                R.id.details_title,
                R.id.details_phone,
                R.id.details_url,
                R.id.details_types,
                R.id.details_rating
        };
        for (int mViewId : viewIdArr) {
            viewArr.put(mViewId,
                    view.findViewById(mViewId));
        }

        // Refresh views
        refreshViews();

        return view;
    }

    private void refreshViews() {
        ((TextView) viewArr.get(R.id.details_title)).setText(title);
        ((TextView) viewArr.get(R.id.details_phone)).setText(phone);
        ((TextView) viewArr.get(R.id.details_url)).setText(url.toString());
        ((TextView) viewArr.get(R.id.details_rating))
                .setText(String.valueOf(rating).substring(0, 2));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        refreshViews();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        refreshViews();
    }

    public Uri getUrl() {
        return url;
    }

    public void setUrl(Uri url) {
        this.url = url;
        refreshViews();
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
        refreshViews();
    }

    public ArrayList<Integer> getTypes() {
        return types;
    }

    public void setTypes(ArrayList<Integer> types) {
        this.types = types;
        refreshViews();
    }
}
