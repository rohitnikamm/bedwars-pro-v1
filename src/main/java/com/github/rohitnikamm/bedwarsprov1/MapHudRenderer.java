package com.github.rohitnikamm.bedwarsprov1;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Simple HUD renderer that shows the current map and its details on the top-left of the screen.
 * This class registers itself on the client event bus when created.
 */
public class MapHudRenderer implements MapUpdateListener {
    private volatile String currentMap = null;
    private volatile MapDetails currentDetails = null;

    public MapHudRenderer() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onMapUpdated(String mapName, MapDetails details) {
        this.currentMap = mapName;
        this.currentDetails = details;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null) return;
        FontRenderer fr = mc.fontRendererObj;

        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        int x = 4;
        int y = 4;
        if (currentMap != null) {
            fr.drawStringWithShadow("Map: " + currentMap, x, y, 0xFFFFFF);
            y += 10;
            if (currentDetails != null) {
                fr.drawStringWithShadow("Rush: " + currentDetails.getRushType(), x, y, 0xFFFF55);
                y += 10;
                fr.drawStringWithShadow("Iron: " + currentDetails.getIronThreshold(), x, y, 0xAAAAFF);
                y += 10;
            } else {
                fr.drawStringWithShadow("No details in maps.json", x, y, 0xFF5555);
                y += 10;
            }
        }
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }
}
