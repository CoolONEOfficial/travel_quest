package ru.coolone.travelquest.ui.fragments.intro;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.location.places.ui.PlacePicker;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import lombok.SneakyThrows;
import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.fragments.SettingsFrag;

import static android.app.Activity.RESULT_OK;
import static ru.coolone.travelquest.ui.fragments.SettingsFrag.PLACE_PICKER_REQUEST;
import static ru.coolone.travelquest.ui.fragments.SettingsFrag.onPlacePickerSuccess;

@EFragment(R.layout.frag_city_picker)
public class CityPickerFrag extends Fragment {

    @ViewById(R.id.dialog_city_container)
    LinearLayout container;

    @ViewById(R.id.dialog_city_picker_include)
    View include;

    @ViewById(R.id.dialog_city_images)
    LinearLayout cityImages;

    @AfterViews
    void afterViews() {
        container.post(
                () -> SettingsFrag.initCitiesLayout(
                        getContext(),
                        cityImages,
                        (v, city) -> {
                            SettingsFrag.selectCitySelect(
                                    getContext(),
                                    cityImages,
                                    city.ordinal()
                            );
                            SettingsFrag.saveCityInPreferences(
                                    getContext(),
                                    city
                            );
                        },
                        container.getHeight() / 3
                )
        );

    }

    @Click(R.id.city_picker_map_select)
    @SneakyThrows
    void onClickMapSelect() {
        startActivityForResult(
                new PlacePicker.IntentBuilder().build(getActivity()),
                PLACE_PICKER_REQUEST
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_PICKER_REQUEST &&
                resultCode == RESULT_OK)
            onPlacePickerSuccess(getContext(), data);
    }
}
