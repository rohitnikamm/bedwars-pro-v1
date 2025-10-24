package com.github.rohitnikamm.bedwarsprov1;

/**
 * Lightweight test harness to validate maps.json loading and simulate scoreboard lines.
 * This class does not depend on Minecraft classes, so you can run it from the IDE's plain Java runner.
 */
public class MapTestHarness {
    public static void main(String[] args) {
        System.out.println("MapTestHarness starting...");
        MapInfoManager manager = new MapInfoManager("maps.json");

        MapUpdateListener listener = new MapUpdateListener() {
            @Override
            public void onMapUpdated(String mapName, MapDetails details) {
                System.out.println("HUD update -> " + mapName + " : " + (details == null ? "<no-details>" : details));
            }
        };

        String[] samples = new String[] {
            "\u00A7aMap: Airshow", // colored line with section sign
            "Map: Amazon",
            "\u00A7cMap: NonexistentMap",
            "Random line",
            null
        };

        for (String s : samples) {
            System.out.println("--- processing: '" + s + "'");
            MapDetails d = processLine(manager, listener, s);
            System.out.println("returned -> " + (d == null ? "null" : d));
        }

        System.out.println("MapTestHarness finished.");
    }

    // Standalone version of the processing logic used in ScoreboardMapReader.processLine
    public static MapDetails processLine(MapInfoManager manager, MapUpdateListener listener, String raw) {
        if (raw == null) return null;
        String clean = raw.replaceAll("(?i)\u00A7[0-9A-FK-OR]", "").trim();
        if (clean.startsWith("Map:")) {
            String mapName = clean.substring("Map:".length()).trim();
            MapDetails details = manager.getDetailsForMap(mapName);
            if (listener != null) listener.onMapUpdated(mapName, details);
            return details;
        }
        return null;
    }
}

