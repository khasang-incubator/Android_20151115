package com.khasang.forecast.location;


/**
 * Created by novoselov on 24.11.2015.
 */
public class LocationManager{
    private final static String TAG = LocationManager.class.getSimpleName();


    //получение координат через GPS
    public static Coordinate getGPSCoordinate (double latitude, double longitude) {
        Coordinate coordinate = new Coordinate();
        coordinate.setLatitude(latitude);
        coordinate.setLongitude(longitude);
        return coordinate;
    }

    // По названию города через геокодер возвращает георгафические координаты
    public static Coordinate getLocationCoordinate (String location) {
        return new Coordinate();
    }


}
