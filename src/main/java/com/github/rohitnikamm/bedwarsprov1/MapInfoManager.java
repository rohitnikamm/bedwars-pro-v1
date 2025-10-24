package com.github.rohitnikamm.bedwarsprov1;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapInfoManager {
    private static final Logger LOGGER = LogManager.getLogger("BedwarsProv");
    private final Map<String, MapDetails> maps;
    private final List<String> canonicalNames; // original names for tab completion

    public MapInfoManager(String resourcePath) {
        Map<String, MapDetails> loaded;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                loaded = Collections.emptyMap();
            } else {
                Type type = new TypeToken<Map<String, MapDetails>>() {}.getType();
                loaded = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load maps resource '{}': {}", resourcePath, e.toString());
            loaded = Collections.emptyMap();
        }
        // normalize keys to lower-case and stripped form for robust lookup
        Map<String, MapDetails> normalized = new HashMap<>();
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, MapDetails> e : loaded.entrySet()) {
            if (e.getKey() == null) continue;
            String key = normalizeKey(e.getKey());
            normalized.put(key, e.getValue());
            names.add(e.getKey());
        }
        this.maps = Collections.unmodifiableMap(normalized);
        // sort canonical names for nicer tab completion
        Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        this.canonicalNames = Collections.unmodifiableList(names);
    }

    public MapDetails getDetailsForMap(String mapName) {
        if (mapName == null) return null;
        return maps.get(normalizeKey(mapName));
    }

    public List<String> getAllMapNames() {
        return canonicalNames;
    }

    private String normalizeKey(String s) {
        if (s == null) return "";
        // lower-case, trim, remove non-alphanumeric characters (keep letters and digits)
        return s.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
