package com.github.rohitnikamm.bedwarsprov1;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = "bedwarsprov", useMetadata=true)
public class ExampleMod {
    private static final Logger LOGGER = LogManager.getLogger("BedwarsProv");

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MapInfoManager mapInfoManager = new MapInfoManager("maps.json");
        // create HUD renderer which will draw the detected map/details on screen
        MapHudRenderer hud = new MapHudRenderer();
        // enable debug=true to log cleaned sidebar lines periodically
        ScoreboardMapReader reader = new ScoreboardMapReader(mapInfoManager, LOGGER, hud, true);
        LOGGER.info("BedwarsProv initialized, maps loaded (debug mode enabled).");
    }
}
