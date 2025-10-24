package com.github.rohitnikamm.bedwarsprov1;

import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.Logger;

public class ScoreboardMapReader {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final MapInfoManager manager;
    private final Logger logger;
    private final MapUpdateListener hudListener;
    private String lastMap = null;

    // debounce fields
    private String candidateMap = null;
    private int candidateTicks = 0;
    private static final int REQUIRED_STABLE_TICKS = 2; // require two ticks to avoid fast toggles

    // debug logging
    private final boolean debug;
    private int debugCooldown = 0; // ticks until next debug dump
    private static final int DEBUG_COOLDOWN_TICKS = 100; // rate-limit debug dumps

    public ScoreboardMapReader(MapInfoManager manager, Logger logger, MapUpdateListener hudListener, boolean debug) {
        this.manager = manager;
        this.logger = logger;
        this.hudListener = hudListener;
        this.debug = debug;
        MinecraftForge.EVENT_BUS.register(this);
    }

    // helper to safely log info when logger may be null (silences static analyzer warnings)
    private void logInfo(String fmt, Object... args) {
        if (logger != null) logger.info(fmt, args);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        // Respect global debug toggle: if disabled, do nothing
        if (!DebugControl.isEnabled()) return;
        if (mc == null || mc.theWorld == null) return;
        Scoreboard sb = mc.theWorld.getScoreboard();
        // try display slot 1 first (sidebar), but also scan all objectives as a fallback
        ScoreObjective sidebar = sb.getObjectiveInDisplaySlot(1);

        String bestRaw = null;

        if (sidebar != null) {
            for (Score score : sb.getSortedScores(sidebar)) {
                String raw = score.getPlayerName();
                String clean = stripFormatting(raw).trim();
                if (debug && debugCooldown == 0) {
                    logInfo("[BedwarsProv-debug] sidebar line: '{}' -> cleaned: '{}'", raw, clean);
                }
                if (clean.startsWith("Map:")) bestRaw = raw;
            }
        }

        // If none found in the displayed sidebar objective, scan all objectives to be thorough
        if (bestRaw == null) {
            for (ScoreObjective obj : sb.getScoreObjectives()) {
                try {
                    for (Score score : sb.getSortedScores(obj)) {
                        String raw = score.getPlayerName();
                        String clean = stripFormatting(raw).trim();
                        if (debug && debugCooldown == 0) {
                            logInfo("[BedwarsProv-debug] obj='{}' line: '{}' -> cleaned: '{}'", obj.getName(), raw, clean);
                        }
                        if (clean.startsWith("Map:")) bestRaw = raw; // last seen wins
                    }
                } catch (Throwable t) {
                    if (logger != null) logger.debug("[BedwarsProv] ignored error scanning objective {}: {}", obj.getName(), t.toString());
                }
                if (bestRaw != null) break; // stop early if found
            }
        }

        if (debug && debugCooldown == 0) debugCooldown = DEBUG_COOLDOWN_TICKS;
        if (debugCooldown > 0) debugCooldown--;

        if (bestRaw != null) {
            String clean = stripFormatting(bestRaw).trim();
            String mapName = clean.substring("Map:".length()).trim();
            // debounce logic
            if (candidateMap == null || !candidateMap.equalsIgnoreCase(mapName)) {
                candidateMap = mapName;
                candidateTicks = 1;
            } else {
                candidateTicks++;
            }

            if (candidateTicks >= REQUIRED_STABLE_TICKS) {
                if (!mapName.equalsIgnoreCase(lastMap)) {
                    lastMap = mapName;
                    MapDetails details = manager.getDetailsForMap(mapName);
                    if (details != null) {
                        logInfo("[BedwarsProv] Map detected: {} -> {}", mapName, details);
                    } else {
                        logInfo("[BedwarsProv] Map detected: {} -> no details in maps.json", mapName);
                    }
                    if (hudListener != null) hudListener.onMapUpdated(mapName, details);
                }
                candidateTicks = 0;
                candidateMap = null;
            }
        } else {
            candidateMap = null;
            candidateTicks = 0;
        }
    }

    // Listen to client chat messages so we can detect map info Hypixel sometimes sends as chat
    @SubscribeEvent
    public void onClientChat(ClientChatReceivedEvent event) {
        // Respect global debug toggle: if disabled, ignore chat detections
        if (!DebugControl.isEnabled()) return;
        try {
            if (event == null || event.message == null) return;
            String raw = event.message.getUnformattedText().trim();
            if (raw.isEmpty()) return;
            if (debug) {
                logInfo("[BedwarsProv-debug] chat: '{}'", raw);
            }

            // Common Hypixel message: "You are currently playing on <MapName>"
            String lower = raw.toLowerCase();
            String token = "you are currently playing on ";
            if (lower.contains(token)) {
                int idx = lower.indexOf(token);
                String mapName = raw.substring(idx + token.length()).trim();
                if (!mapName.isEmpty()) {
                    // Immediately update (bypass debounce) since chat is authoritative for this game
                    if (!mapName.equalsIgnoreCase(lastMap)) {
                        lastMap = mapName;
                        MapDetails details = manager.getDetailsForMap(mapName);
                        if (details != null) {
                            logInfo("[BedwarsProv] Map detected (chat): {} -> {}", mapName, details);
                        } else {
                            logInfo("[BedwarsProv] Map detected (chat): {} -> no details in maps.json", mapName);
                        }
                        if (hudListener != null) hudListener.onMapUpdated(mapName, details);
                    }
                }
                return;
            }

            // Also handle other chat outputs from /map or similar that contain the map name
            // Example patterns: "The map is: <MapName>", "Map: <MapName>", etc.
            if (raw.startsWith("Map:") || raw.toLowerCase().startsWith("the map is") || raw.toLowerCase().startsWith("map:")) {
                // Fix: escape backslash for Java string so regex '\\s' is valid at runtime
                String mapName = raw.replaceFirst("(?i)^(the map is[:\\s]*|map[:\\s]*)", "").trim();
                if (!mapName.isEmpty()) {
                    if (!mapName.equalsIgnoreCase(lastMap)) {
                        lastMap = mapName;
                        MapDetails details = manager.getDetailsForMap(mapName);
                        if (details != null) {
                            logInfo("[BedwarsProv] Map detected (chat): {} -> {}", mapName, details);
                        } else {
                            logInfo("[BedwarsProv] Map detected (chat): {} -> no details in maps.json", mapName);
                        }
                        if (hudListener != null) hudListener.onMapUpdated(mapName, details);
                    }
                }
                // fall through
            }
        } catch (Throwable t) {
            if (logger != null) logger.debug("[BedwarsProv] error parsing chat: {}", t.toString());
        }
    }

    private String stripFormatting(String s) {
        if (s == null) return "";
        return s.replaceAll("(?i)\\u00A7[0-9A-FK-OR]", "");
    }
}
