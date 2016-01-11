package com.k9rosie.novswar.model;

import com.k9rosie.novswar.game.Gamemode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class NovsWorld implements Listener {

    private World world;
    private String name;
    private Gamemode gamemode;
    private HashSet<NovsTeam> enabledTeams;
    private HashMap<NovsTeam, Location> teamSpawns;
    private ArrayList<NovsRegion> intermissionGateRegions;
    private ArrayList<NovsRegion> respawnGateRegions;

    public NovsWorld(World world, String name, Gamemode gamemode) {
        this.world = world;
        this.name = name;
        this.gamemode = gamemode;
        enabledTeams = new HashSet<NovsTeam>();
        teamSpawns = new HashMap<NovsTeam, Location>();
    }

    public World getBukkitWorld() {
        return world;
    }

    public void setBukkitWorld(World world) {
        this.world = world;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Gamemode getGamemode() {
        return gamemode;
    }

    public void setGamemode(Gamemode gamemode) {
        this.gamemode = gamemode;
    }

    public HashSet<NovsTeam> getEnabledTeams() {
        return enabledTeams;
    }

    public HashMap<NovsTeam, Location> getTeamSpawns() {
        return teamSpawns;
    }

    public ArrayList<NovsRegion> getIntermissionGateRegions() {
        return intermissionGateRegions;
    }

    public ArrayList<NovsRegion> getRespawnGateRegions() {
        return respawnGateRegions;
    }
}
