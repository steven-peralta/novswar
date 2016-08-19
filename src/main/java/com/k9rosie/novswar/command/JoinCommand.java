package com.k9rosie.novswar.command;

import com.k9rosie.novswar.NovsWar;
import com.k9rosie.novswar.game.Game;
import com.k9rosie.novswar.model.NovsPlayer;
import com.k9rosie.novswar.util.ChatFormat;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinCommand extends NovsCommand {

    private Game game;

    public JoinCommand(NovsWar novsWar, CommandSender sender, String[] args) {
        super(novsWar, sender, args);
        game = getNovsWar().getGameHandler().getGame();
    }

    public void execute() {
        NovsPlayer player = getNovsWar().getNovsPlayerCache().getPlayers().get((Player) getSender());
        if(player.isSpectating()) {
        	ChatFormat.sendNotice(player, "You cannot join while spectating");
        } else {
        	game.joinGame(player);
        }
    }
}
