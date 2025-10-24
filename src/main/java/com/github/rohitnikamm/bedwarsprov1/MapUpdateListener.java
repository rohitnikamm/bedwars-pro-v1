package com.github.rohitnikamm.bedwarsprov1;

// Simple listener interface for HUD or other consumers to receive map updates
public interface MapUpdateListener {
    // mapName - the readable name, details may be null if not found in maps.json
    void onMapUpdated(String mapName, MapDetails details);
}

