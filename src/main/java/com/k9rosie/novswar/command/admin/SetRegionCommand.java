package com.k9rosie.novswar.command.admin;

import com.k9rosie.novswar.NovsWar;
import com.k9rosie.novswar.command.NovsCommand;
import com.k9rosie.novswar.model.NovsPlayer;
import com.k9rosie.novswar.model.NovsWorld;
import com.k9rosie.novswar.util.ChatUtil;
import com.k9rosie.novswar.util.Messages;
import com.k9rosie.novswar.util.RegionType;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetRegionCommand extends NovsCommand {

    public SetRegionCommand(NovsWar novsWar, CommandSender sender, String[] args) {
        super(novsWar, sender, args);
    }

    public void execute() {
        //nw admin setregion regionOne battlefield
        if (getArgs().length != 4) {
            ChatUtil.sendError((Player) getSender(), Messages.INVALID_PARAMETERS.toString());
            return;
        } else {
            Player bukkitPlayer = (Player) getSender();
            NovsPlayer player = getNovsWar().getNovsPlayerCache().getPlayers().get(bukkitPlayer);
            World bukkitWorld = bukkitPlayer.getWorld();
            NovsWorld world = getNovsWar().getNovsWorldCache().getWorlds().get(bukkitWorld);

            if (world == null) {
                ChatUtil.sendError(bukkitPlayer, "The world you're in isn't enabled in NovsWar.");
                return;
            }

            String regionName = getArgs()[2];
            RegionType regionType = RegionType.parseString(getArgs()[3]);

            if (regionType == null) {
            	String regionTypeList = "";
            	for(RegionType region : RegionType.values()) {
            		regionTypeList += (region.toString().toLowerCase()+" ");
            	}
                ChatUtil.sendError(bukkitPlayer, "Invalid region type. Use "+regionTypeList);
                return;
            }

            player.setRegionNameBuffer(regionName);
            player.setRegionTypeBuffer(regionType);
            player.setSettingRegion(true);

            ChatUtil.sendNotice(player, "Setting corner one...");
        }


    }
}
