package com.github.rohitnikamm.bedwarsprov1;

public class MapDetails {
    private String rushType;
    private int ironThreshold;

    public MapDetails() {}

    // Values for mapDetails
    public MapDetails(String rushType, int ironThreshold) {
        this.rushType = rushType;
        this.ironThreshold = ironThreshold;
    }

    // Methods
    public String getRushType() {
        return rushType;
    }

    public int getIronThreshold() {
        return ironThreshold;
    }

    @Override
    public String toString() {
        return "MapDetails{rushType='" + rushType + '\'' + ", ironThreshold=" + ironThreshold + '}';
    }


}
