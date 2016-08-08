package com.k9rosie.novswar.manager;

import com.k9rosie.novswar.NovsWar;
import com.k9rosie.novswar.game.Game;
import com.k9rosie.novswar.model.NovsPlayer;
import com.k9rosie.novswar.model.NovsTeam;
import com.k9rosie.novswar.util.SendTitle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class TeamManager {

    private NovsWar novswar;
    private ArrayList<NovsTeam> teams;
    private NovsTeam defaultTeam;
    private int messageTime;
    private int messageTask;

    public TeamManager(NovsWar novswar) {
        this.novswar = novswar;
        teams = new ArrayList<NovsTeam>();
        messageTime = 0;
        messageTask = 0;
    }

    public void initialize() {
        teams.add(new NovsTeam("default", ChatColor.GRAY, false, false, false));
        loadTeams(); // load teams' data from config
    }

    public ArrayList<NovsTeam> getTeams() {
        return teams;
    }

    public NovsTeam getDefaultTeam() {
        return teams.get(0);
    }

    public NovsTeam getTeam(String teamName) {
        for (NovsTeam team : teams) {
            if (teamName.equalsIgnoreCase(team.getTeamName())) {
                return team;
            }
        }
        return null;
    }

    public void loadTeams() {
        FileConfiguration teamsConfig = novswar.getConfigurationCache().getConfig("teams");
        List<Map<?, ?>> teamInfoMaps = teamsConfig.getMapList("teams");


        for(Map<?, ?> teamInfoMap: teamInfoMaps) {
            HashMap<String, Object> teamInfo = (HashMap<String, Object>) teamInfoMap;
            String teamName = (String) teamInfo.get("name");
            ChatColor color = (ChatColor) teamInfo.get("color");
            boolean canBeDamaged = (boolean) teamInfo.get("can_be_damaged");
            boolean canAttack = (boolean) teamInfo.get("can_attack");
            boolean friendlyFire = (boolean) teamInfo.get("can_attack");
            NovsTeam team = new NovsTeam(teamName, color, canBeDamaged, canAttack, friendlyFire);
            teams.add(team);
        }
    }
    
    /**
     * Sends all in-game players to their spawns and balances the teams
     */
    public void balanceTeams() {
    	Game game = novswar.getGameHandler().getGame();
    	game.pauseGame();
    	messageTime = 5;
    	messageTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(novswar.getPlugin(), new Runnable() {
    		public void run() {
    			ArrayList<NovsPlayer> autobalancePlayers = new ArrayList<NovsPlayer>();
    			for(NovsPlayer player : novswar.getPlayerDataCache().getInGamePlayers()) {
    				SendTitle.sendTitle(player.getBukkitPlayer(), 0, 2000, 0, " ", "Team Auto-Balance in "+messageTime+"...");
    				autobalancePlayers.add(player);
            	}
    			messageTime--;
    			if(messageTime <= 0) {
    				Bukkit.getScheduler().cancelTask(messageTask);
    				//Set every player's team to default
    		    	for(NovsPlayer player : autobalancePlayers) {
    		    		SendTitle.sendTitle(player.getBukkitPlayer(), 0, 0, 0, " ", "");
    		    		player.setTeam(novswar.getTeamManager().getDefaultTeam());
    		        }
    		    	//re-do the team sorting algorithm
    		    	for(NovsPlayer player : autobalancePlayers) {
    		    		game.assignPlayerTeam(player);
    		        }
    		    	game.unpauseGame();
    			}
    		}
    	}, 0, 20);
    }
    
    /**
     * Assigns all in-game players to the next team index in the NovsTeam array list
     */
    public void rotateTeams() {
    	HashMap<NovsTeam, NovsTeam> rotationMap = new HashMap<NovsTeam, NovsTeam>(); //key = source, value = target
    	int targetIndex = 0;
    	//Generate map for team switching
    	for(int sourceIndex = 0; sourceIndex < teams.size(); sourceIndex++) {
    		targetIndex = sourceIndex + 1;
    		if(targetIndex >= teams.size()) {
    			targetIndex = 0;
    		}
    		rotationMap.put(teams.get(sourceIndex), teams.get(targetIndex));
    	}
    	//Switch teams for each player in-game
    	for(NovsPlayer player : novswar.getPlayerDataCache().getInGamePlayers()) {
    		NovsTeam newTeam = rotationMap.get(player.getTeam());
    		player.setTeam(newTeam);
    		player.getBukkitPlayer().teleport(novswar.getGameHandler().getGame().getWorld().getTeamSpawns().get(newTeam));
    		player.getBukkitPlayer().setHealth(player.getBukkitPlayer().getMaxHealth());
            player.getBukkitPlayer().setFoodLevel(20);
    	}
    }
}