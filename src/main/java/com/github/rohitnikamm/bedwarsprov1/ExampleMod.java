package com.github.rohitnikamm.bedwarsprov1;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Client-only imports
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;

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
        LOGGER.info("BedwarsProv initialized, maps loaded (debug mode enabled). Abstracting command registration to client-side.");

        // register client-side debug command when on client side
        if (event.getSide() == Side.CLIENT) {
            try {
                ClientCommandHandler.instance.registerCommand(new DebugMapCommand(mapInfoManager, hud));
                LOGGER.info("BedwarsProv: registered client dbgmap command");
            } catch (Throwable t) {
                LOGGER.debug("Failed to register client dbgmap command: {}", t.toString());
            }
        }
    }
}
