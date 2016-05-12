package com.khasang.forecast.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.khasang.forecast.R;
import com.khasang.forecast.adapters.WeatherAdapter;
import com.khasang.forecast.adapters.etc.WeatherScrollListener;
import com.khasang.forecast.position.Weather;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by aleksandrlihovidov on 05.12.15.
 * Родительский класс для фрагментов
 * DailyForecastFragment && HourlyForecastFragment
 */
public abstract class CommonForecastFragment extends Fragment {

    protected Map<Calendar, Weather> forecasts;
    protected RecyclerView recyclerView;
    protected TextView tvEmptyList;
    protected WeatherAdapter adapter;
    protected ArrayList<String> sDate;
    protected ArrayList<Weather> weathers;
    protected WeatherScrollListener scrollListener;
    protected LinearLayoutManager layoutManager;

    protected abstract void updateForecasts();

    public void setDatasAndAnimate(Map<Calendar, Weather> forecasts) {
        if (null == forecasts) {
            tvEmptyList.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyList.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            // создаем отсортированную по ключам Map
            this.forecasts = new TreeMap<>();
            this.forecasts.putAll(forecasts);

            sDate.clear();
            weathers.clear();
            updateForecasts();
            adapter.notifyDataSetChanged();
        }
    }

    public Map<Calendar, Weather> getForecasts() {
        return forecasts;
    }

    public void scroll(boolean appbarVisible) {
        if (appbarVisible) {
            layoutManager.scrollToPosition(1);
        } else {
            layoutManager.scrollToPosition(adapter.getItemCount() - 2);
        }
    }

    public void scrollToCenter() {
        int position = adapter.getItemCount() / 2;
        layoutManager.scrollToPosition(position);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sDate = new ArrayList<>();
        weathers = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_forecast_list, container, false);

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();

        layoutManager = new LinearLayoutManager(getActivity());

        int headerHeight = (int) getContext().getResources().getDimension(R.dimen.appbar_height);
        int footerHeight = (int) getContext().getResources().getDimension(R.dimen.chart_height);

        adapter = new WeatherAdapter(sDate, weathers);
        adapter.setHeaderHeight(headerHeight);
        adapter.setFooterHeight(footerHeight);

        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        recyclerView.setItemAnimator(itemAnimator);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        if (scrollListener != null) {
            recyclerView.addOnScrollListener(scrollListener);
        }

        tvEmptyList = (TextView) v.findViewById(R.id.tvEmptyList);

        return v;
    }

    public void addScrollListener(WeatherScrollListener scrollListener) {
        this.scrollListener = scrollListener;
        if (recyclerView != null) {
            recyclerView.addOnScrollListener(scrollListener);
        }
    }
}
