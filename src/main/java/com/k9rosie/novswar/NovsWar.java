package com.k9rosie.novswar;

import com.k9rosie.novswar.config.ConfigurationCache;
import com.k9rosie.novswar.database.DatabaseThread;
import com.k9rosie.novswar.gamemode.GamemodeHandler;
import com.k9rosie.novswar.manager.PlayerManager;
import com.k9rosie.novswar.manager.TeamManager;
import com.k9rosie.novswar.manager.WorldManager;
import org.bukkit.configuration.file.FileConfiguration;

public class NovsWar {
	
	private NovsWarPlugin plugin;
	private static NovsWar instance;
	
	private ConfigurationCache configurationCache;
	private TeamManager teamManager;
	private PlayerManager playerManager;
	private WorldManager worldManager;
	private DatabaseThread databaseThread;
	private GamemodeHandler gamemodeHandler;

	private boolean lobbyEnabled;
	private boolean votingEnabled;

	public NovsWar(NovsWarPlugin plugin) {
		this.plugin = plugin;
		instance = this;
		
		configurationCache = new ConfigurationCache(this);
		teamManager = new TeamManager(this);
		playerManager = new PlayerManager(this);
		worldManager = new WorldManager(this);
        databaseThread = new DatabaseThread(this);
        gamemodeHandler = new GamemodeHandler(this);

		lobbyEnabled = true;
	}
	
	public void initialize() {
		configurationCache.initialize();

        lobbyEnabled = configurationCache.getConfig("core").getBoolean("core.lobby.enabled");
        votingEnabled = configurationCache.getConfig("core").getBoolean("core.voting.enabled");

        databaseThread.start();

		teamManager.initialize();
		worldManager.initialize();
	}
	
	public static NovsWar getInstance() {
		return instance;
	}
	
	public NovsWarPlugin getPlugin() {
		return plugin;
	}

	public ConfigurationCache getConfigurationCache() {
		return configurationCache;
	}
	
	public TeamManager getTeamManager() {
		return teamManager;
	}

	public WorldManager getWorldManager() {
		return worldManager;
	}

    public PlayerManager getPlayerManager() {
        return playerManager;
    }
    
    public DatabaseThread getDatabaseThread() {
    	return databaseThread;
    }
    
    public GamemodeHandler getGamemodeHandler() {
    	return gamemodeHandler;
    }

	public boolean isLobbyEnabled() {
		return lobbyEnabled;
	}
}
