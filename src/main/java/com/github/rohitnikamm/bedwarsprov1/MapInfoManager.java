package com.github.rohitnikamm.bedwarsprov1;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MapInfoManager {
    private final Map<String, MapDetails> maps;

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
            e.printStackTrace();
            loaded = Collections.emptyMap();
        }
        // normalize keys to lower-case for case-insensitive lookup
        Map<String, MapDetails> normalized = new HashMap<>();
        for (Map.Entry<String, MapDetails> e : loaded.entrySet()) {
            if (e.getKey() == null) continue;
            normalized.put(e.getKey().trim().toLowerCase(Locale.ROOT), e.getValue());
        }
        this.maps = Collections.unmodifiableMap(normalized);
    }

    public MapDetails getDetailsForMap(String mapName) {
        if (mapName == null) return null;
        return maps.get(mapName.trim().toLowerCase(Locale.ROOT));
    }
}
