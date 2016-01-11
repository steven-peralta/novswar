package com.k9rosie.novswar;

import com.k9rosie.novswar.listener.PlayerListener;
import com.k9rosie.novswar.listener.EntityListener;
import com.k9rosie.novswar.listener.WorldListener;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class NovsWarPlugin extends JavaPlugin {
	
	private NovsWar novswar;
	private Server server;
	private PluginManager pluginManager;

	public void onEnable() {
		server = getServer();
		pluginManager = server.getPluginManager();

		registerListeners();

		novswar = new NovsWar(this);
		novswar.initialize();


		getLogger().info("novswar enabled. written by k9rosie.");
	}
	
	public void onDisable() {
		
	}
	
	public NovsWar getNovswarInstance() {
		return novswar;
	}

	private void registerListeners() {
		pluginManager.registerEvents(new PlayerListener(this), this);
		pluginManager.registerEvents(new EntityListener(this), this);
		pluginManager.registerEvents(new WorldListener(this), this);
	}
}