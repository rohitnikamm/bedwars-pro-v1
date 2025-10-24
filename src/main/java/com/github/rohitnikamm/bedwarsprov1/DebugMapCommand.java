package com.github.rohitnikamm.bedwarsprov1;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DebugMapCommand implements ICommand {
    private static final Logger LOGGER = LogManager.getLogger("BedwarsProv");
    private final MapInfoManager manager;
    private final MapUpdateListener hudListener;
    private final List<String> aliases;

    public DebugMapCommand(MapInfoManager manager, MapUpdateListener hudListener) {
        this.manager = manager;
        this.hudListener = hudListener;
        this.aliases = new ArrayList<>();
        this.aliases.add("dbg");
        this.aliases.add("dbgmap");
    }

    @Override
    public String getCommandName() {
        return "dbgmap";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/dbgmap <MapName> or /dbg map <MapName>";
    }

    @Override
    public List<String> getCommandAliases() {
        return aliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args == null || args.length == 0) {
            sender.addChatMessage(new ChatComponentText("Usage: " + getCommandUsage(sender)));
            return;
        }

        String mapName;
        if (args[0].equalsIgnoreCase("map")) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText("Usage: " + getCommandUsage(sender)));
                return;
            }
            mapName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        } else {
            mapName = String.join(" ", args);
        }

        if (mapName.trim().isEmpty()) {
            sender.addChatMessage(new ChatComponentText("Usage: " + getCommandUsage(sender)));
            return;
        }

        MapDetails details = manager.getDetailsForMap(mapName);
        if (details != null) {
            sender.addChatMessage(new ChatComponentText("[BedwarsProv] Map detected (dbg): " + mapName + " -> " + details));
            LOGGER.info("[BedwarsProv] Map detected (dbg): {} -> {}", mapName, details);
        } else {
            sender.addChatMessage(new ChatComponentText("[BedwarsProv] Map detected (dbg): " + mapName + " -> no details in maps.json"));
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
        return new ArrayList<>();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        if (o != null) {
            return getCommandName().compareTo(o.getCommandName());
        }
        return 0;
    }
}
