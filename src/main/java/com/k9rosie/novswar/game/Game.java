package com.k9rosie.novswar.game;


import com.k9rosie.novswar.NovsWar;
import com.k9rosie.novswar.event.NovsWarEndGameEvent;
import com.k9rosie.novswar.event.NovsWarJoinGameEvent;
import com.k9rosie.novswar.event.NovsWarPlayerKillEvent;
import com.k9rosie.novswar.event.NovsWarTeamVictoryEvent;
import com.k9rosie.novswar.gamemode.Gamemode;
import com.k9rosie.novswar.model.NovsPlayer;
import com.k9rosie.novswar.model.NovsTeam;
import com.k9rosie.novswar.model.NovsWorld;
import com.k9rosie.novswar.util.Messages;
import com.k9rosie.novswar.util.SendTitle;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class Game {
    private GameHandler gameHandler;
    private NovsWorld world;
    private Gamemode gamemode;
    private GameState gameState;
    private ArrayList<NovsTeam> enabledTeams;
    private HashMap<NovsPlayer, DeathTimer> deathTimers;
    private NovsWar novsWar;
    private GameTimer gameTimer;
    private GameScoreboard scoreboard;
    private BallotBox ballotBox;

    public Game(GameHandler gameHandler, NovsWorld world, Gamemode gamemode) {
        this.gameHandler = gameHandler;
        this.world = world;
        this.gamemode = gamemode;
        enabledTeams = new ArrayList<NovsTeam>();
        deathTimers = new HashMap<NovsPlayer, DeathTimer>();
        gameState = GameState.WAITING_FOR_PLAYERS;
        novsWar = gameHandler.getNovsWarInstance();
        gameTimer = new GameTimer(this);
        scoreboard = new GameScoreboard(this);
        ballotBox = new BallotBox(novsWar);
    }

    public void initialize() {
    	//Create default team
        NovsTeam defaultTeam = novsWar.getTeamManager().getDefaultTeam();
        Team defaultScoreboardTeam = scoreboard.createScoreboardTeam(defaultTeam);
        defaultTeam.setScoreboardTeam(defaultScoreboardTeam);

        //Populate the list 'enabledTeams' with valid NovsTeam objects
        List<String> enabledTeamNames = novsWar.getConfigurationCache().getConfig("worlds").getStringList("worlds."+world.getBukkitWorld().getName()+".enabled_teams");
        for (String validTeam : enabledTeamNames) {
            for (NovsTeam team : novsWar.getTeamManager().getTeams()) {
                if (validTeam.equalsIgnoreCase(team.getTeamName())) {
                	team.setScoreboardTeam(scoreboard.createScoreboardTeam(team));
                	enabledTeams.add(team);
                }
            }
        }

        for (NovsTeam team : enabledTeams) {
        	team.getNovsScore().setScore(0);	//Resets all team's scores
        }
        
        for (NovsPlayer player : novsWar.getPlayerManager().getPlayers().values()) {
        	player.getBukkitPlayer().setHealth(player.getBukkitPlayer().getMaxHealth());
        	player.getBukkitPlayer().setFoodLevel(20);
        	player.setTeam(defaultTeam); // NovsPlayer now has private NovsTeam var
            player.getBukkitPlayer().teleport(novsWar.getWorldManager().getLobbyWorld().getTeamSpawns().get(defaultTeam));
        }


        scoreboard.initialize(); //maybe try putting this before line 69?

        waitForPlayers();
    }

    /**
     * endTimer()
     * Controls the next state of the game when the timer ends
     */
    public void endTimer() {
    	switch(gameState) {
    	case PRE_GAME :
    		startGame();
    		break;
    	case DURING_GAME :
    		endGame();
    		break;
    	case POST_GAME :
    		NovsWorld nextMap;
    		if(novsWar.getConfigurationCache().getConfig("core").getBoolean("core.voting.enabled") == true) {
    			nextMap = ballotBox.tallyResults();
            }
    		else {
    			nextMap = ballotBox.nextWorld(world);
    		}
    		if(nextMap == null) {
    			nextMap = world;
    			System.out.println("There was a problem getting the next NovsWorld. Using previous world.");
    		}
    		gameHandler.newGame(nextMap);
    		break;
    	default :
    		break;
    	}
    }

    public void waitForPlayers() {
        gameState = GameState.WAITING_FOR_PLAYERS;
        gameTimer.stopTimer();
        scoreboard.setSidebarTitle("Waiting for players");
    }

    public void preGame() {
        gameState = GameState.PRE_GAME;
        world.respawnBattlefields();
        int gameTime = novsWar.getConfigurationCache().getConfig("core").getInt("core.game.pre_game_timer");
        gameTimer.stopTimer();
        gameTimer.setTime(gameTime);
        gameTimer.startTimer();
    }

    public void startGame() {
        gameState = GameState.DURING_GAME;
        world.openIntermissionGates();
        int gameTime = gamemode.getGameTime();
        gameTimer.stopTimer();
        gameTimer.setTime(gameTime);
        gameTimer.startTimer();
        Bukkit.broadcastMessage("Starting Round");
    }

    public void pauseGame() {
        gameState = GameState.PAUSED;
        Bukkit.broadcastMessage("Pausing Round");
        world.closeIntermissionGates();
        for(NovsPlayer player : novsWar.getPlayerManager().getPlayers().values()) {
            if (!player.getTeam().equals(novsWar.getTeamManager().getDefaultTeam())) {
                player.getBukkitPlayer().teleport(world.getTeamSpawns().get(player.getTeam()));
            }
        }
        gameTimer.pauseTimer();
    }

    public void unpauseGame() {
        if (!gameState.equals(GameState.PAUSED)) {
            return;
        }
        gameTimer.startTimer();
        gameState = GameState.DURING_GAME;
        Bukkit.broadcastMessage("Resuming Round");
        world.openIntermissionGates();
    }

    /**
     * endGame()
     * Controls the team victory message and end-game stats. Respawns dead players.
     */
    public void endGame() {
        NovsWarEndGameEvent event = new NovsWarEndGameEvent(this);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            gameState = GameState.POST_GAME;
            
            //Determine winning teams and invoke events
            ArrayList<NovsTeam> winners = getWinners();
            System.out.println(winners.size());
            if (winners.size() == 1) {
                NovsTeam winner = winners.get(0);
                //Display victory message for all players, given single victor
                for(NovsPlayer player : novsWar.getPlayerManager().getPlayers().values()) {
                	SendTitle.sendTitle(player.getBukkitPlayer(), 0, 2000, 0, " ", winner.getColor()+winner.getTeamName()+" §fwins!");
                }
                //Bukkit.broadcastMessage(winner.getColor()+winner.getTeamName()+" §fwins!");
            } else if (winners.size() > 1) {
                StringBuilder teamList = new StringBuilder();
                for (int i = 0; i < winners.toArray().length; i++) {
                    NovsTeam team = (NovsTeam) winners.toArray()[i];
                    teamList.append(team.getColor()+team.getTeamName());
                    if (i != winners.toArray().length-1) {
                        teamList.append(ChatColor.GRAY+", ");
                    }
                }
              //Display victory message for all players, given multiple victors
                for(NovsPlayer player : novsWar.getPlayerManager().getPlayers().values()) {
                	SendTitle.sendTitle(player.getBukkitPlayer(), 0, 2000, 0, " ", teamList.toString() + " §fwin!");
                }
                //Bukkit.broadcastMessage(teamList.toString() + " §fwin!");
            }
            for(NovsTeam winner : winners) {
            	NovsWarTeamVictoryEvent invokeEvent = new NovsWarTeamVictoryEvent(winner, this);
                Bukkit.getPluginManager().callEvent(invokeEvent);
            }
            
            //Respawns all dead players and tp's alive players to their team spawns
            for(NovsPlayer player : novsWar.getPlayerManager().getPlayers().values()) {
            	if(player.isDead()) {
            		respawn(player);
            	} else {
            		player.getBukkitPlayer().teleport(world.getTeamSpawns().get(player.getTeam()));
            	}
            }
            
            //Stats generation
            for (NovsTeam team : winners) {
                for (NovsPlayer player : team.getPlayers()) {
                    player.getStats().incrementWins();
                }
            }
            for (NovsTeam team : enabledTeams) {
                for (NovsPlayer player : team.getPlayers()) {
                    player.getStats().incrementGamesPlayed();
                }
            }

            world.closeIntermissionGates();
            world.respawnBattlefields();

            int gameTime = novsWar.getConfigurationCache().getConfig("core").getInt("core.game.post_game_timer");
            gameTimer.stopTimer();
            gameTimer.setTime(gameTime);
            gameTimer.startTimer();

            // start voting if enabled
            // delay voting screen for 4 seconds
            Bukkit.getScheduler().scheduleSyncDelayedTask(novsWar.getPlugin(), new Runnable() {
                @Override
                public void run() {
                	//Remove victory message
                	for(NovsPlayer player : novsWar.getPlayerManager().getPlayers().values()) {
                		SendTitle.sendTitle(player.getBukkitPlayer(), 0, 0, 0, " ", "");
                	}
                    if(novsWar.getConfigurationCache().getConfig("core").getBoolean("core.voting.enabled") == true) {
                        ballotBox.castVotes();
                    }
                }
            }, 20*4);
        }
    }

    /**
     * Determines the team(s) with the highest score
     * @return ArrayList of NovsTeams with highest score
     */
    public ArrayList<NovsTeam> getWinners() {
        ArrayList<NovsTeam> winningTeams = new ArrayList<NovsTeam>();
        int topScore = 0;
        NovsTeam topTeam = enabledTeams.get(0); //arbitrarily initialize topTeam as team 0
        //Find the team with the highest score
        for (NovsTeam team : enabledTeams) {
            if (team.getNovsScore().getScore() > topScore) {
            	topScore = team.getNovsScore().getScore();
            	topTeam = team;
            }
        }
        winningTeams.add(topTeam);
        //Find other teams that are tied with the top team
        for (NovsTeam team : enabledTeams) {
        	if(team.equals(topTeam) == false && team.getNovsScore().getScore() == topScore) {
        		winningTeams.add(team);
        	}
        }
        return winningTeams;
    }

    public void clockTick() {
        String secondsString = Integer.toString(gameTimer.getSeconds());
        String minutesString = Integer.toString(gameTimer.getMinutes());
        String gameStateString = "";

        switch (gameState) {
        case PRE_GAME :
        	gameStateString = ChatColor.GRAY + "Setting up: ";
        	break;
        case DURING_GAME :
        	gameStateString = "";
        	break;
        case POST_GAME :
        	gameStateString = ChatColor.GRAY + "Post game: ";
        	break;
        case PAUSED :
        	gameStateString = ChatColor.GRAY + "Game Paused ";
        	break;
    	default :
    		gameStateString = "";
    		break;
        }

        if (gameTimer.getSeconds() < 10) {
            secondsString = "0" + Integer.toString(gameTimer.getSeconds());
        } else if (gameTimer.getSeconds() <= 0) {
            secondsString = "00";
        }
        if (gameTimer.getMinutes() < 10) {
            minutesString = "0" + Integer.toString(gameTimer.getMinutes());
        } else if (gameTimer.getMinutes() <= 0) {
            minutesString = "00";
        }
        scoreboard.setSidebarTitle(gameStateString + ChatColor.GREEN + minutesString + ":" + secondsString);
    }

    /**
     * Checks player count on teams
     * @return True if there are the minimum required players in-game, else false
     */
    public boolean checkPlayerCount() {
        int numPlayers = 0;
        int required = novsWar.getConfigurationCache().getConfig("core").getInt("core.game.minimum_players");
        for (NovsTeam team : enabledTeams) {
            numPlayers += team.getPlayers().size();
        }
        if (numPlayers >= required) {
            return true;
        } else {
            return false;
        }
    }

    public void scheduleDeath(NovsPlayer player, int seconds) {
        Player bukkitPlayer = player.getBukkitPlayer();
        player.setDeath(true);
        bukkitPlayer.setGameMode(GameMode.SPECTATOR);
        bukkitPlayer.setHealth(player.getBukkitPlayer().getMaxHealth());
        bukkitPlayer.setFoodLevel(20);
        bukkitPlayer.getWorld().playEffect(player.getBukkitPlayer().getLocation(), Effect.SMOKE, 31);
        bukkitPlayer.getWorld().playSound(player.getBukkitPlayer().getLocation(), Sound.BLOCK_LAVA_POP, 10, 1);

        if (bukkitPlayer.getKiller() != null) {
            bukkitPlayer.setSpectatorTarget(bukkitPlayer.getKiller());
        }

        DeathTimer timer = new DeathTimer(this, seconds, player);
        timer.startTimer();
        deathTimers.put(player, timer);
    }

    public void deathTick(NovsPlayer player) {
        DeathTimer timer = deathTimers.get(player);
        SendTitle.sendTitle(player.getBukkitPlayer(), 0, 2000, 0, " ", "Respawn in " + Integer.toString(timer.getSeconds()) + "...");
    }

    public void respawn(NovsPlayer player) {
        SendTitle.sendTitle(player.getBukkitPlayer(), 0, 0, 0, " ", "");
        DeathTimer timer = deathTimers.get(player);
        timer.stopTimer();
        deathTimers.remove(player);
        player.getBukkitPlayer().getScoreboard().getObjective(DisplaySlot.SIDEBAR).setDisplayName(scoreboard.getSidebarTitle());

        if (player.isDead()) {
            NovsTeam team = player.getTeam();

            player.setDeath(false);
            player.getBukkitPlayer().teleport(world.getTeamSpawns().get(team));
            player.getBukkitPlayer().setGameMode(GameMode.SURVIVAL);
        }
    }

    public void joinGame(NovsPlayer player) {
        NovsWarJoinGameEvent event = new NovsWarJoinGameEvent(this, player);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            boolean canJoinInProgress = novsWar.getConfigurationCache().getConfig("core").getBoolean("core.game.join_in_progress");

            if (!canJoinInProgress && gameState.equals(GameState.DURING_GAME)) {
                player.getBukkitPlayer().sendMessage(Messages.CANNOT_JOIN_GAME.toString());
                return;
            }

            // novsloadout has its own way of sorting players, only run this code if it isnt enabled
            if (!Bukkit.getPluginManager().isPluginEnabled("NovsLoadout")) {
            	//Determine which team has fewer players
            	NovsTeam smallestTeam = enabledTeams.get(0);
            	int smallest = smallestTeam.getPlayers().size();
                for (NovsTeam team : enabledTeams) {
                	if(team.getPlayers().size() <= smallest) {
                		smallest = team.getPlayers().size();
                		smallestTeam = team;
                	}
                }

                player.setTeam(smallestTeam);

                Location teamSpawn = world.getTeamSpawns().get(smallestTeam);
                player.getBukkitPlayer().teleport(teamSpawn);
                String message = Messages.JOIN_TEAM.toString().replace("%team_color%", smallestTeam.getColor().toString()).replace("%team%", smallestTeam.getTeamName());
                player.getBukkitPlayer().sendMessage(message);
            }

            player.getBukkitPlayer().setHealth(player.getBukkitPlayer().getMaxHealth());
            player.getBukkitPlayer().setFoodLevel(20);

            if (checkPlayerCount()) {
            	switch (gameState) {
            	case WAITING_FOR_PLAYERS :
            		preGame();
            		break;
            	case PAUSED :
            		unpauseGame();
            		break;
        		default :
        			break;
            	}
            }
        }
    }
    
    public void quitGame() {
    	if(novsWar.getPlayerManager().getPlayers().size() == 0) {
    		//There are no players in the server, start new game
    		System.out.println("There are no players in the server");
    		if(gameTimer.getTaskID() != 0) { //if there is a running timer
    			System.out.println("Stopped timer");
    			gameTimer.stopTimer();
    		}
    		gameHandler.newGame(world); //waitForPlayers();
    	} else {
    		System.out.println("A player left the server");
    		if(checkPlayerCount()==false) { //if there are not enough players
    			System.out.println("There are not enough players");
    			switch (gameState) {
            	case PRE_GAME :
            		waitForPlayers();
            		break;
            	case DURING_GAME :
            		pauseGame();
            		break;
        		default :
        			break;
            	}
    		}
    	}
    }

    public GameScoreboard getScoreboard() {
        return scoreboard;
    }

    public GameHandler getGameHandler() {
        return gameHandler;
    }

    public BallotBox getBallotBox() {
    	return ballotBox;
    }
    
    public ArrayList<NovsTeam> getTeams() {
    	return enabledTeams;
    }

    public Gamemode getGamemode() {
        return gamemode;
    }
    
    public GameState getGameState() {
    	return gameState;
    }
}
