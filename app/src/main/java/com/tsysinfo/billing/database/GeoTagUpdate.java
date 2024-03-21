package com.tsysinfo.billing.database;

public class GeoTagUpdate {
    String CName,GPSLatitude,GPSLongitude,UpdatedTStmp;

    public String getCName() {
        return CName;
    }

    public void setCName(String CName) {
        this.CName = CName;
    }

    public String getGPSLatitude() {
        return GPSLatitude;
    }

    public void setGPSLatitude(String GPSLatitude) {
        this.GPSLatitude = GPSLatitude;
    }

    public String getGPSLongitude() {
        return GPSLongitude;
    }

    public void setGPSLongitude(String GPSLongitude) {
        this.GPSLongitude = GPSLongitude;
    }

    public String getUpdatedTStmp() {
        return UpdatedTStmp;
    }

    public void setUpdatedTStmp(String updatedTStmp) {
        UpdatedTStmp = updatedTStmp;
    }
}
