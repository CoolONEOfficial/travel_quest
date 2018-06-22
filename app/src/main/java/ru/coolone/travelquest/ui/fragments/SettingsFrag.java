package ru.coolone.travelquest.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import org.androidannotations.annotations.EFragment;

import lombok.SneakyThrows;
import lombok.val;
import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.activities.MainActivity;

import static android.app.Activity.RESULT_OK;

@EFragment
public class SettingsFrag extends PreferenceFragmentCompat {
    private static final String TAG = SettingsFrag.class.getSimpleName();

    public static int PLACE_PICKER_REQUEST = 1;

    public static String SELECT_IMAGE_TAG = "selectImage";

    public enum SupportCity {
        NN(
                R.drawable.city_nn, R.string.city_nn,
                new LatLng(56.326887, 44.005986)
        ),
        MOSCOW(
                R.drawable.city_moscow, R.string.city_moscow,
                new LatLng(55.755814, 37.617635)
        ),
        KRASNOYARSK(
                R.drawable.city_krasnoyarsk, R.string.city_krasnoyarsk,
                new LatLng(56.010563, 92.852572)
        ),
        NOVOSIBIRSK(
                R.drawable.city_novosibirsk, R.string.city_novosibirsk,
                new LatLng(55.030199, 82.920430)
        ),
        TYUMEN(
                R.drawable.city_tyumen, R.string.city_tyumen,
                new LatLng(57.153033, 65.534328)
        ),
        ROSTOV_ON_DON(
                R.drawable.city_rostov_on_don, R.string.city_rostov_on_don,
                new LatLng(47.222543, 39.718732)
        ),
        SAMARA(
                R.drawable.city_samara, R.string.city_samara,
                new LatLng(53.195538, 50.101783)
        ),
        SAINT_PETERBURG(
                R.drawable.city_saint_peterburg, R.string.city_saint_peterburg,
                new LatLng(59.939095, 30.315868)
        ),
        PENZA(
                R.drawable.city_penza, R.string.city_penza,
                new LatLng(53.195063, 45.018316)
        ),
        SAVESTOPOL(
                R.drawable.city_sevastopol, R.string.city_sevastopol,
                new LatLng(44.616687, 33.525432)
        ),
        NOVOKUZNETSK(
                R.drawable.city_novokuznetsk, R.string.city_novokuznetsk,
                new LatLng(53.757547, 87.136044)
        ),
        BARNAUL(
                R.drawable.city_barnaul, R.string.city_barnaul,
                new LatLng(53.355084, 83.769948)
        );

        public final int drawableId;
        public final int nameId;
        public final LatLng coord;

        SupportCity(
                @DrawableRes int drawableId,
                @StringRes int nameId,
                LatLng coord
        ) {
            this.drawableId = drawableId;
            this.nameId = nameId;
            this.coord = coord;
        }
    }

    SupportCity selectedCity;

    static private void unselectAllCities(
            GridLayout cityLayout
    ) {
        // Remove all selectImage's
        for (int mImageContainerId = 0; mImageContainerId < cityLayout.getChildCount(); mImageContainerId++) {
            val mImageContainer = (FrameLayout) cityLayout.getChildAt(mImageContainerId);

            val selectImage = mImageContainer.findViewWithTag(SELECT_IMAGE_TAG);
            if (selectImage != null)
                mImageContainer.removeView(selectImage);
        }
    }

    static public void selectCitySelect(
            Context context,
            GridLayout cityLayout,
            int cityId
    ) {
        unselectAllCities(cityLayout);

        // Add new selectImage
        val selectImage = new ImageView(context);
        selectImage.setImageDrawable(
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_check, null)
        );
        val selectImageParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        selectImageParams.gravity = Gravity.CENTER;
        selectImage.setPadding(
                10, 10, 10, 10
        );
        selectImage.setLayoutParams(selectImageParams);
        selectImage.setBackgroundColor(Color.BLACK);
        selectImage.getBackground().setAlpha(128);
        selectImage.setTag(SELECT_IMAGE_TAG);

        ((FrameLayout) cityLayout.getChildAt(cityId))
                .addView(selectImage);
    }

    public interface OnClickCityListener {
        void onClickCity(View v, SupportCity city);
    }

    static public void refreshCitiesLayout(
            Context context,
            GridLayout layout
    ) {
        val selectedCityId = MainActivity.settings.getInt(
                context.getString(R.string.settings_key_city_id),
                -1
        );
        if (selectedCityId != -1) {
            selectCitySelect(
                    context,
                    layout,
                    selectedCityId
            );
        }
    }

    public enum CityType {
        SETTINGS(300),
        INTRO;

        public int size;

        CityType(int size) {
            this.size = size;
        }

        CityType() {
        }
    }

    public static void saveCityInPreferences(
            Context context,
            SupportCity city
    ) {
        val settingsEdit = MainActivity.settings.edit();
        settingsEdit.putInt(
                context.getString(R.string.settings_key_city_id),
                city.ordinal()
        );
        MainActivity.putSettingsLatLng(
                settingsEdit,
                context.getString(R.string.settings_key_city_coord),
                city.coord
        );
        settingsEdit.apply();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    static int imagesLoading = 0;

    static public void initCitiesLayout(
            Activity context,
            GridLayout layout,
            OnClickCityListener listener,
            int rowsCount,
            CityType cityType
    ) {
        val pattern = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(
                        context.getResources(),
                        R.drawable.pattern
                ),
                cityType.size, cityType.size,
                false
        );

        context.runOnUiThread(
                () -> {
                    layout.setRowCount(rowsCount);

                    for (val mCity : SupportCity.values()) {
                        val mCityImageContainer = new FrameLayout(context);

                        val mCityImage = new ImageView(context);
                        val mCityImageParams = new FrameLayout.LayoutParams(
                                cityType.size,
                                cityType.size
                        );
                        mCityImage.setLayoutParams(mCityImageParams);
                        mCityImage.setImageBitmap(
                                pattern
                        );
                        mCityImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

                        val mCityLabel = new TextView(context);
                        mCityLabel.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        mCityLabel.setText(mCity.nameId);
                        mCityLabel.setTextSize(
                                (float) ((22. / 400.) * cityType.size)
                        );
                        mCityLabel.setTextColor(Color.WHITE);
                        mCityLabel.setBackgroundColor(Color.BLACK);
                        mCityLabel.getBackground().setAlpha(128);
                        val mCityLabelParams = new FrameLayout.LayoutParams(
                                cityType.size,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        mCityLabelParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                        mCityLabel.setLayoutParams(mCityLabelParams);

                        mCityImageContainer.addView(mCityImage);
                        mCityImageContainer.addView(mCityLabel);

                        mCityImageContainer.setOnClickListener(
                                (v) -> listener.onClickCity(v, mCity)
                        );

                        AsyncTask.execute(
                                () -> mCityImage.setImageBitmap(
                                        Bitmap.createScaledBitmap(
                                                BitmapFactory.decodeResource(
                                                        context.getResources(),
                                                        mCity.drawableId
                                                ),
                                                cityType.size, cityType.size,
                                                false
                                        )
                                )
                        );

                        layout.addView(mCityImageContainer);
                    }
                }
        );
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings);

        val dialogView = getLayoutInflater()
                .inflate(R.layout.dialog_city_picker, null);
        val imagesLayout = (GridLayout) dialogView.findViewById(R.id.dialog_city_images);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            imagesLayout.setPadding(
                    0, 50, 0, 0
            );
        }

        val cityPickerDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_city_title)
                .setView(dialogView)
                .setPositiveButton(
                        getString(android.R.string.ok),
                        (dialog1, which) -> {
                            if (selectedCity != null)
                                saveCityInPreferences(getContext(), selectedCity);
                        }
                )
                .setNegativeButton(
                        getString(android.R.string.cancel),
                        (dialog2, which) -> {
                            dialog2.dismiss();

                            if (selectedCity != null) {
                                unselectAllCities(imagesLayout);
                                selectedCity = null;
                            }
                        }
                )
                .setNeutralButton(
                        getString(R.string.dialog_city_button_map),
                        new DialogInterface.OnClickListener() {
                            @Override
                            @SneakyThrows
                            public void onClick(DialogInterface dialog, int which) {
                                startActivityForResult(
                                        new PlacePicker.IntentBuilder().build(getActivity()),
                                        PLACE_PICKER_REQUEST
                                );
                            }
                        }
                )
                .create();

        findPreference(getString(R.string.settings_key_city_pref))
                .setOnPreferenceClickListener(
                        preference -> {
                            if (imagesLayout.getChildCount() == 0) {
                                initCitiesLayout(
                                        SettingsFrag.this.getActivity(),
                                        imagesLayout,
                                        (v, city) -> {
                                            selectCitySelect(getContext(), imagesLayout, city.ordinal());
                                            selectedCity = city;
                                        },
                                        2,
                                        CityType.SETTINGS
                                );
                            }
                            refreshCitiesLayout(getContext(), imagesLayout);
                            cityPickerDialog.show();

                            return true;
                        }
                );
    }

    public static void onPlacePickerSuccess(
            Context context,
            Intent data
    ) {
        val place = PlacePicker.getPlace(context, data);

        val settingsEdit = MainActivity.settings.edit();
        MainActivity.putSettingsLatLng(
                settingsEdit,
                context.getString(R.string.settings_key_city_coord),
                place.getLatLng()
        );
        settingsEdit.remove(context.getString(R.string.settings_key_city_id));
        settingsEdit.apply();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_PICKER_REQUEST &&
                resultCode == RESULT_OK)
            onPlacePickerSuccess(getContext(), data);
    }
}
