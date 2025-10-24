package com.github.rohitnikamm.bedwarsprov1;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class DebugMapCommand implements ICommand {
    private static final Logger LOGGER = LogManager.getLogger("BedwarsProv");
    private final MapInfoManager manager;
    private final MapUpdateListener hudListener;
    private final List<String> aliases;

    // global enabled flag (command stays registered but can be toggled)
    private static boolean enabled = true;

    public DebugMapCommand(MapInfoManager manager, MapUpdateListener hudListener) {
        this.manager = manager;
        this.hudListener = hudListener;
        this.aliases = new ArrayList<>();
        // keep only the explicit /dbgmap alias so /dbg map is not available
        this.aliases.add("dbgmap");
    }

    @Override
    public String getCommandName() {
        return "dbgmap";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/dbgmap <MapName>  OR  /dbgmap toggle";
    }

    @Override
    public List<String> getCommandAliases() {
        return aliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args == null || args.length == 0) {
            sender.addChatMessage(new ChatComponentText("Usage: " + getCommandUsage(sender)));
            return;
        }

        String first = args[0].trim();
        // Only support toggle control; otherwise treat all args as the map name
        if (first.equalsIgnoreCase("toggle")) {
            enabled = !enabled;
            sender.addChatMessage(new ChatComponentText("[BedwarsProv] dbgmap " + (enabled ? "enabled" : "disabled")));
            LOGGER.info("[BedwarsProv] dbgmap toggled, now {}", enabled ? "enabled" : "disabled");
            // If disabled, clear HUD immediately
            if (!enabled && hudListener != null) {
                hudListener.onMapUpdated(null, null);
            }
            return;
        }

        if (!enabled) {
            sender.addChatMessage(new ChatComponentText("[BedwarsProv] dbgmap is disabled. Use /dbgmap toggle to enable."));
            return;
        }

        // normal map-handling: join all args into the map name
        String mapName = String.join(" ", args).trim();

        if (mapName.isEmpty()) {
            sender.addChatMessage(new ChatComponentText("Usage: " + getCommandUsage(sender)));
            return;
        }

        MapDetails details = manager.getDetailsForMap(mapName);
        // DO NOT send detection messages to chat; only log and update HUD
        if (details != null) {
            LOGGER.info("[BedwarsProv] Map detected (dbg): {} -> {}", mapName, details);
        } else {
            LOGGER.info("[BedwarsProv] Map detected (dbg): {} -> no details in maps.json", mapName);
        }

        if (hudListener != null) hudListener.onMapUpdated(mapName, details);
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true; // allow all (client-side)
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        List<String> all = manager.getAllMapNames();
        List<String> results = new ArrayList<>();
        if (args == null || args.length == 0) return results;
        String prefix = String.join(" ", args).trim().toLowerCase();
        if (prefix.length() == 0) return results;
        for (String name : all) {
            if (results.size() >= 25) break;
            if (name.toLowerCase().startsWith(prefix)) results.add(name);
        }
        return results;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public int compareTo(ICommand o) {
        if (o == null) return 0;
        return getCommandName().compareTo(o.getCommandName());
    }
}
