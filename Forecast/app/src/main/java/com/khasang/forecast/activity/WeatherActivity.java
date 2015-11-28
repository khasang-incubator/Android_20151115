package com.khasang.forecast.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.khasang.forecast.Precipitation;
import com.khasang.forecast.R;
import com.khasang.forecast.adapter.ForecastPageAdapter;

import java.awt.font.TextAttribute;

/**
 * Данные которые необходимо отображать в WeatherActivity (для первого релиза):
 * город, температура, давление, влажность, ветер, временная метка.
 */

public class WeatherActivity extends FragmentActivity implements View.OnClickListener{
    /**
     * ViewPager для отображения нижних вкладок прогноза: по часам и по дням
     */
    private ViewPager pager;

    private TextView city;
    private TextView temperature;
    private TextView precipitation;
    private TextView pressure;
    private TextView wind;
    private TextView humidity;
    private TextView timeStamp;
    private ImageButton syncBtn;

    // Для тестирования
    private String current_city = "Berlin";
    private int current_temperature = 1;
    //private Precipitation current_precipitation;
    private String current_precipitation = "Солнечно";
    private int current_pressure = 40;
    private int current_wind = 25;
    private int current_humidity = 12;
    private String current_timeStamp = "10:15";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        city = (TextView) findViewById(R.id.city);
        temperature = (TextView) findViewById(R.id.temperature);
        precipitation = (TextView) findViewById(R.id.precipitation);
        pressure = (TextView) findViewById(R.id.pressure);
        wind = (TextView) findViewById(R.id.wind);
        humidity = (TextView) findViewById(R.id.humidity);
        timeStamp = (TextView) findViewById(R.id.timeStamp);
        syncBtn = (ImageButton) findViewById(R.id.syncBtn);

        syncBtn.setOnClickListener(this);


        /**
         * Код для фрагментов
         */
        pager = (ViewPager) findViewById(R.id.pager);
        ForecastPageAdapter adapter = new ForecastPageAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
    }

    /**
     * Обработчик нажатия кнопки
     */
    @Override
    public void onClick(View view) {
        updateInterface(current_city, current_temperature, current_precipitation,
                current_pressure, current_wind, current_humidity, current_timeStamp);
    }

    /**
     * Обновление интерфейса Activity
     */
    public void updateInterface(String current_city, int current_temperature,
                                String current_precipitation, int current_presure,
                                int current_wind, int current_humidity, String current_timeStamp) {
        city.setText(String.valueOf(current_city));
        temperature.setText(String.valueOf(current_temperature) + "°C");
        precipitation.setText(String.valueOf(current_precipitation));
        pressure.setText(getString(R.string.pressure) + " " + String.valueOf(current_presure) + getString(R.string.pressure_measure));
        wind.setText(getString(R.string.wind) + " " + String.valueOf(current_wind) + getString(R.string.wind_measure));
        humidity.setText(getString(R.string.humidity) + " " + String.valueOf(current_humidity) + "%");
        timeStamp.setText(getString(R.string.timeStamp) + " " +String.valueOf(current_timeStamp));
    }
}
