package com.example.alex.motoproject.firebase;

class LocationModel {
    private double lat;
    private double lng;

    LocationModel(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public LocationModel() {

    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
