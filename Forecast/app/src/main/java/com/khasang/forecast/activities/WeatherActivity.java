package com.khasang.forecast.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.khasang.forecast.AppUtils;
import com.khasang.forecast.Logger;
import com.khasang.forecast.MyApplication;
import com.khasang.forecast.PermissionChecker;
import com.khasang.forecast.R;
import com.khasang.forecast.fragments.DailyForecastFragment;
import com.khasang.forecast.fragments.HourlyForecastFragment;
import com.khasang.forecast.interfaces.IMessageProvider;
import com.khasang.forecast.interfaces.IPermissionCallback;
import com.khasang.forecast.interfaces.IWeatherReceiver;
import com.khasang.forecast.position.PositionManager;
import com.khasang.forecast.position.Weather;
import com.khasang.forecast.stations.WeatherStation;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.ionicons_typeface_library.Ionicons;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * Данные которые необходимо отображать в WeatherActivity (для первого релиза):
 * город, температура, давление, влажность, ветер, временная метка.
 */

public class WeatherActivity extends AppCompatActivity implements View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener,
        IWeatherReceiver, IPermissionCallback, IMessageProvider {
    private static final int CHOOSE_CITY = 1;
    private static final String TAG = WeatherActivity.class.getSimpleName();

    private TextView temperature;
    private TextView description;
    private TextView wind;
    private TextView humidity;
    private ImageView currWeather;
    private ImageView syncBtn;

    private Animation animationRotateCenter;
    private Animation animationGrow;

    private HourlyForecastFragment hourlyForecastFragment;
    private DailyForecastFragment dailyForecastFragment;
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private ProgressBar progressbar;

    private Drawer result = null;
    private PrimaryDrawerItem favorites;
    private boolean opened = false;
    private final int subItemIndex = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            hourlyForecastFragment = new HourlyForecastFragment();
            dailyForecastFragment = new DailyForecastFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, hourlyForecastFragment)
                    .add(R.id.fragment_container, dailyForecastFragment)
                    .hide(dailyForecastFragment)
                    .commit();
        }
        initFields();
        setAnimationForWidgets();
        startAnimations();
        checkPermissions();
        initNavigationDrawer();
    }

    private void initNavigationDrawer() {
        Logger.println("drawer", "init");
        /** Инициализация элементов меню */
        final DividerDrawerItem divider = new DividerDrawerItem();
        final PrimaryDrawerItem currentPlace = new PrimaryDrawerItem().withName(R.string.drawer_item_current_place).withIcon(Ionicons.Icon.ion_navigate).withIdentifier(0);
        final PrimaryDrawerItem cityList = new PrimaryDrawerItem().withName(R.string.drawer_item_city_list).withIcon(CommunityMaterial.Icon.cmd_city).withIdentifier(1);
        favorites = new PrimaryDrawerItem().withName(R.string.drawer_item_favorites).withIcon(MaterialDesignIconic.Icon.gmi_star)/*.withBadge(String.valueOf(PositionManager.getInstance().getFavouritesList().size()))*/.withIdentifier(2);
        final SecondaryDrawerItem settings = new SecondaryDrawerItem().withName(R.string.drawer_item_settings).withIcon(FontAwesome.Icon.faw_cog).withIdentifier(3);
        final SecondaryDrawerItem feedBack = new SecondaryDrawerItem().withName(R.string.drawer_item_feedback).withIcon(GoogleMaterial.Icon.gmd_feedback).withIdentifier(4);
        final PrimaryDrawerItem footer = new PrimaryDrawerItem().withName(R.string.app_name).withEnabled(false).withIdentifier(5);

        /** Создание Navigation Drawer */
        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withSelectedItem(-1)
                .withActionBarDrawerToggle(true)
                .withHeader(R.layout.drawer_header)
                .addDrawerItems(
                        currentPlace,
                        cityList,
                        favorites,
                        divider,
                        settings,
                        feedBack
                )
                .addStickyDrawerItems(footer)
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {

                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {

                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {

                    }
                })
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View v, int position, IDrawerItem drawerItem) {
                        switch (drawerItem.getIdentifier()) {
                            case 0:
                                changeDisplayedCity("");
                                result.closeDrawer();
                                //TODO add unselect item
                                break;
                            case 1:
                                startCityPickerActivity();
                                result.closeDrawer();
                                //TODO add unselect item
                                break;
                            case 2:
                                if (opened) {
                                    for (int i = PositionManager.getInstance().getFavouritesList().size() - 1; i >= 0; i--) {
                                        result.removeItems(subItemIndex + i);

                                    }
                                } else {
                                    int curPos = result.getPosition(drawerItem);
                                    if (!PositionManager.getInstance().getFavouritesList().isEmpty()) {
                                        for (int i = PositionManager.getInstance().getFavouritesList().size() - 1; i >= 0; i--) {
                                            String city = PositionManager.getInstance().getFavouritesList().get(i).split(",")[0];
                                            result.addItemsAtPosition(
                                                    curPos,
                                                    new SecondaryDrawerItem().withLevel(2).withName(city).withIdentifier(subItemIndex + i)
                                            );
                                        }
                                    } else {
                                        Logger.println(TAG, "favCityList is empty");
                                    }
                                }

                                opened = !opened;
                                break;
                            case 3:
                                startSettingsActivity();
                                result.closeDrawer();
                                break;
                            case 4:
                                //TODO add unselect item
                                // FIXME: 31.01.16
                                Intent feedbackIntent = null;
                                switch (Locale.getDefault().getLanguage()) {
                                    case "ru":
                                        feedbackIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MyApplication.getAppContext().getString(R.string.google_form_ru)));
                                        break;
                                    default:
                                        feedbackIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MyApplication.getAppContext().getString(R.string.google_form_en)));
                                        break;
                                }
                                startActivity(feedbackIntent);
                                result.closeDrawer();
                                break;
                            case 5:
                                //result.setSelection(-1);
                                break;
                            default:
                                String newCity = PositionManager.getInstance().getFavouritesList().get(drawerItem.getIdentifier() - subItemIndex);
                                changeDisplayedCity(newCity);
                                result.closeDrawer();
                                break;
                        }
                        return true;
                    }
                })
                .build();
    }

    /**
     * Обновление Drawer badges
     */
    public void updateBadges() {
        // TODO Дизейблить пункт "Текущее местоположение" в дровере, если нет прав на получение координат
//        PermissionChecker permissionChecker = new PermissionChecker();
//        boolean isLocationPermissionGranted = permissionChecker.isPermissionGranted(this, PermissionChecker.RuntimePermissions.PERMISSION_REQUEST_FINE_LOCATION);
//         дизейблить пункт на основании переменной isLocationPermissionGranted

        if (PositionManager.getInstance().getFavouritesList().isEmpty()) {
            favorites.withBadge("").withEnabled(false);
            result.updateItem(favorites);
            return;
        }
        favorites.withEnabled(true);
        result.updateItem(favorites);
        result.updateBadge(2, new StringHolder(String.valueOf(PositionManager.getInstance().getFavouritesList().size())));
    }

    /**
     * Закрывает открытые Drawer SubItems
     */
    public void closeSubItems() {
        for (int i = PositionManager.getInstance().getFavouritesList().size() - 1; i >= 0; i--) {
            result.removeItems(subItemIndex + i);
        }
        if (opened) {
            opened = false;
        }
        //FIXME add unselect item
        favorites.withSelectable(false);
        result.updateItem(favorites);
    }

    @Override
    public void onBackPressed() {
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PositionManager.getInstance().setReceiver(this);
        PositionManager.getInstance().setMessageProvider(this);
        PositionManager.getInstance().updateWeatherFromDB();
        Logger.println(TAG, "OnResume");
        updateBadges();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        PositionManager.getInstance().setUseGpsModule(sp.getBoolean(getString(R.string.pref_gps_key), true));
        if (sp.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_celsius)).equals(getString(R.string.pref_units_celsius))) {
            PositionManager.getInstance().setTemperatureMetric(AppUtils.TemperatureMetrics.CELSIUS);
        } else if (sp.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_celsius)).equals(getString(R.string.pref_units_kelvin))) {
            PositionManager.getInstance().setTemperatureMetric(AppUtils.TemperatureMetrics.KELVIN);
        } else if (sp.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_celsius)).equals(getString(R.string.pref_units_fahrenheit))) {
            PositionManager.getInstance().setTemperatureMetric(AppUtils.TemperatureMetrics.FAHRENHEIT);
        } else {
            PositionManager.getInstance().setTemperatureMetric(AppUtils.TemperatureMetrics.CELSIUS);
        }
        if (sp.getString(getString(R.string.pref_speed_key), getString(R.string.pref_speed_meter_sec)).equals(getString(R.string.pref_speed_meter_sec))) {
            PositionManager.getInstance().setSpeedMetric(AppUtils.SpeedMetrics.METER_PER_SECOND);
        } else if (sp.getString(getString(R.string.pref_speed_key), getString(R.string.pref_speed_meter_sec)).equals(getString(R.string.pref_speed_foot_sec))) {
            PositionManager.getInstance().setSpeedMetric(AppUtils.SpeedMetrics.FOOT_PER_SECOND);
        } else if (sp.getString(getString(R.string.pref_speed_key), getString(R.string.pref_speed_meter_sec)).equals(getString(R.string.pref_speed_km_hour))) {
            PositionManager.getInstance().setSpeedMetric(AppUtils.SpeedMetrics.KM_PER_HOURS);
        } else if (sp.getString(getString(R.string.pref_speed_key), getString(R.string.pref_speed_meter_sec)).equals(getString(R.string.pref_speed_mile_hour))) {
            PositionManager.getInstance().setSpeedMetric(AppUtils.SpeedMetrics.MILES_PER_HOURS);
        } else {
            PositionManager.getInstance().setSpeedMetric(AppUtils.SpeedMetrics.METER_PER_SECOND);
        }
        onRefresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeSubItems();
        PositionManager.getInstance().setMessageProvider(null);
        PositionManager.getInstance().setReceiver(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean saveCurrentLocation = sp.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_current)).equals(getString(R.string.pref_location_current));
        PositionManager.getInstance().saveSettings(saveCurrentLocation);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PositionManager.getInstance().removeInstance();
    }

    private void checkCoordinatesServices() {
        if (!PositionManager.getInstance().isSomeLocationProviderAvailable()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.location_manager);
            builder.setMessage(R.string.activate_geographical_service);
            builder.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(i);
                }
            });
            builder.setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.create().show();
        }
    }

    private void checkPermissions() {
        PermissionChecker permissionChecker = new PermissionChecker();
        permissionChecker.checkForPermissions(this, PermissionChecker.RuntimePermissions.PERMISSION_REQUEST_FINE_LOCATION, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionChecker.RuntimePermissions.PERMISSION_REQUEST_FINE_LOCATION.VALUE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                PositionManager.getInstance().setReceiver(null);
                PositionManager.getInstance().removeInstance();
                PositionManager.getInstance().setReceiver(this);
                PositionManager.getInstance().updateWeatherFromDB();
                PositionManager.getInstance().updateWeather();
                permissionGranted(PermissionChecker.RuntimePermissions.PERMISSION_REQUEST_FINE_LOCATION);
            } else {
                permissionDenied(PermissionChecker.RuntimePermissions.PERMISSION_REQUEST_FINE_LOCATION);
            }
        }
    }

    @Override
    public void permissionGranted(PermissionChecker.RuntimePermissions permission) {
        checkCoordinatesServices();
    }

    @Override
    public void permissionDenied(PermissionChecker.RuntimePermissions permission) {

    }

    private void switchDisplayMode() {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (dailyForecastFragment.isHidden()) {
            ft.show(dailyForecastFragment)
                    .hide(hourlyForecastFragment)
                    .commit();
            fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_by_hour));
        } else {
            ft.show(hourlyForecastFragment)
                    .hide(dailyForecastFragment)
                    .commit();
            fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_by_day));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_weather, menu);
        final MenuItem item = menu.findItem(R.id.menu_item_refresh);
        item.setActionView(R.layout.iv_action_refresh);
        syncBtn = (ImageView) item.getActionView().findViewById(R.id.refreshButton);
        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(item);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_refresh:
                startAnimation();
                onRefresh();
                return true;
        }
        return false;
    }

    public void startCityPickerActivity() {
        Intent intent = new Intent(this, CityPickerActivity.class);
        Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
                .toBundle();
        ActivityCompat.startActivityForResult(this, intent, CHOOSE_CITY, bundle);
    }

    public void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
                .toBundle();
        ActivityCompat.startActivity(this, intent, bundle);
    }

    private void initFields() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_material);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        currWeather = (ImageView) findViewById(R.id.iv_curr_weather);
        temperature = (TextView) findViewById(R.id.temperature);
        description = (TextView) findViewById(R.id.precipitation);
        wind = (TextView) findViewById(R.id.wind);
        humidity = (TextView) findViewById(R.id.humidity);
        progressbar = (ProgressBar) findViewById(R.id.progressbar);
        progressbar.setIndeterminate(true);

        /** Слушатели нажатий объектов */
        fab.setOnClickListener(this);
        temperature.setOnClickListener(this);
        setSupportActionBar(toolbar);
    }

    public void showProgress(boolean loading) {
        progressbar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void startAnimation() {
        syncBtn.startAnimation(animationRotateCenter);
    }

    private void setAnimationForWidgets() {
        /** Анимация объектов */
        animationRotateCenter = AnimationUtils.loadAnimation(this, R.anim.rotate_center);
        animationGrow = AnimationUtils.loadAnimation(this, R.anim.simple_grow);
    }

    /**
     * Обновление интерфейса Activity при получении новых данных
     */

    @Override
    public void updateInterface(WeatherStation.ResponseType responseType, Map<Calendar, Weather> forecast) {
        stopRefresh();
        toolbar.setTitle(PositionManager.getInstance().getCurrentPositionName().split(",")[0]);
        if (forecast == null || forecast.size() == 0) {
            Logger.println(TAG, "Weather is null!");
            return;
        }
        switch (responseType) {
            case CURRENT:
                Logger.println(TAG, "Принят CURRENT прогноз");
                HashMap.Entry<Calendar, Weather> firstEntry = forecast.entrySet().iterator().next();
                updateCurrentWeather(firstEntry.getKey(), firstEntry.getValue());
                break;
            case HOURLY:
                Logger.println(TAG, "Принят HOURLY прогноз");
                hourlyForecastFragment.setDatasAndAnimate(forecast);
                break;
            case DAILY:
                Logger.println(TAG, "Принят DAILY прогноз");
                dailyForecastFragment.setDatasAndAnimate(forecast);
                break;
            default:
                Logger.println(TAG, "Принят необрабатываемый прогноз");
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showProgress(false);
            }
        }, 1250);
    }

    /**
     * Обработчик нажатия объектов
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                switchDisplayMode();
                break;
            case R.id.temperature:
                PositionManager.getInstance().changeTemperatureMetric();
                PositionManager.getInstance().updateWeatherFromDB();
                break;
        }
    }

    public void updateCurrentWeather(Calendar date, Weather wCurent) {

        if (wCurent == null) {
            Log.i(TAG, "Weather is null!");
            return;
        }

        toolbar.setTitle(PositionManager.getInstance().getCurrentPositionName().split(",")[0]);
        temperature.setText(String.format("%.0f%s", wCurent.getTemperature(),
                PositionManager.getInstance().getTemperatureMetric().toStringValue()));
        description.setText(String.format("%s", wCurent.getDescription()
                .substring(0, 1)
                .toUpperCase() + wCurent
                .getDescription()
                .substring(1)));
        int iconId = wCurent.getPrecipitation().getIconResId(
                AppUtils.isDayFromString(
                        String.format(Locale.getDefault(), "%tR", date)));
        currWeather.setImageResource(iconId == 0 ? R.mipmap.ic_launcher : iconId);

        wind.setText(Html.fromHtml(String.format("%s %.0f%s",
                wCurent.getWindDirection().getDirectionString(),
                wCurent.getWindPower(),
                PositionManager.getInstance().getSpeedMetric().toStringValue())));

        humidity.setText(String.format("%s%%",
                wCurent.getHumidity()));
    }

    /**
     * Изменяет отображаемый город WeatherActivity
     */
    public void changeDisplayedCity(String newCity) {
        PositionManager.getInstance().setCurrentPosition(newCity);
        onRefresh();
    }


    /**
     * Получаем город из CityPickActivity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_CITY) {
            if (resultCode == RESULT_OK) {
                String newCity = data.getStringExtra(CityPickerActivity.CITY_PICKER_TAG);
                toolbar.setTitle(newCity.split(",")[0]);
                Logger.println(TAG, newCity);
                PositionManager.getInstance().setCurrentPosition(newCity);
            } else {
                if (!PositionManager.getInstance().positionIsPresent(PositionManager.getInstance().getCurrentPositionName())) {
                    stopRefresh();
                    showProgress(false);
                }
            }
        }
    }

    /**
     * Анимация обновления
     */
    @Override
    public void onRefresh() {
        showProgress(true);
        if (!PositionManager.getInstance().positionIsPresent(PositionManager.getInstance().getCurrentPositionName())) {
            Logger.println(TAG, "There is nothing to refresh");
            showMessageToUser(getString(R.string.msg_no_city), Snackbar.LENGTH_LONG);
            showProgress(false);
            return;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Logger.println(TAG, "Start animation");
                PositionManager.getInstance().updateWeather();
            }
        }, 500);
    }

    /**
     * Проигрываение анимации всех объектов activity
     */
    private void startAnimations() {
        fab.startAnimation(animationGrow);
    }

    //TODO DELETE

    /**
     * Останавливаем анимацию
     */
    public void stopRefresh() {
    }


    @Override
    public boolean receiveHourlyWeatherFirst() {
        return dailyForecastFragment.isHidden();
    }

    @Override
    public void showMessageToUser(CharSequence string, int length) {
        AppUtils.showSnackBar(this, findViewById(R.id.coordinatorLayout), string, length);
    }

    @Override
    public void showMessageToUser(int stringId, int length) {
        showMessageToUser(getString(stringId), length);
    }

    @Override
    public void showToast(int stringId) {
        AppUtils.showInfoMessage(this, getString(stringId)).show();
    }
}

