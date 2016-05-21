package com.khasang.forecast.position;

/**
 * Created by Veda on 24.11.15.
 */

public class Position implements IPosition {
    private String name;
    private int cityID;
    private Coordinate coordinate;

    public Position() {
    }

    @Override
    public void setLocationName(String name) {
        this.name = name;
    }

    @Override
    public String getLocationName() {
        return name;
    }

    @Override
    public int getCityID() {
        return cityID;
    }

    @Override
    public void setCityID(int cityID) {
        this.cityID = cityID;
    }

    @Override
    public Coordinate getCoordinate() {
        return coordinate;
    }

    @Override
    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        Position position = (Position) o;
        if (cityID == position.cityID) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 7;
        result = 17 * result + (name == null ? 0 : name.hashCode());
        result = 17 * result + cityID;
        return result;
    }

    @Override public String toString() {
        return "Position{name=" + name + ", cityID=" + cityID + ", coordinate=" + coordinate + "}";
    }
}





