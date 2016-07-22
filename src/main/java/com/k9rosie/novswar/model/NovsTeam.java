package com.k9rosie.novswar.model;

import com.k9rosie.novswar.NovsWar;
import com.k9rosie.novswar.manager.PlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashSet;

public class NovsTeam {

    private String teamName;
    private ChatColor color;
    private Team scoreboardTeam;
    private boolean canBeDamaged;
    private boolean canAttack;
    private boolean friendlyFire;
    private int score;

    public NovsTeam(String teamName, ChatColor color, boolean canBeDamaged, boolean canAttack, boolean friendlyFire) {
        this.teamName = teamName;
        this.color = color;
        this.canBeDamaged = canBeDamaged;
        this.canAttack = canAttack;
        this.friendlyFire = friendlyFire;
        score = 0;
        scoreboardTeam = null;
    }

    public int getScore() {
        return score;
    }
    
    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public ChatColor getColor() {
        return color;
    }

    public void setColor(ChatColor color) {
        this.color = color;
    }

    public boolean canBeDamaged() {
        return canBeDamaged;
    }

    public void setCanBeDamaged(boolean canBeDamaged) {
        this.canBeDamaged = canBeDamaged;
    }

    public boolean canAttack() {
        return canAttack;
    }

    public void setCanAttack(boolean canAttack) {
        this.canAttack = canAttack;
    }

    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }

    public boolean getFriendlyFire() {
        return friendlyFire;
    }
    
    public void incrementScore() {
        score++;
    }

    public void incrementScore(int increment) {
        score += increment;
    }

    public void decrementScore() {
        score--;
    }

    public void decrementScore(int increment) {
        score -= increment;
    }

    public HashSet<NovsPlayer> getPlayers() {
        HashSet<NovsPlayer> players = (HashSet) NovsWar.getInstance().getPlayerManager().getNovsPlayers();
        HashSet<NovsPlayer> teamMembers = new HashSet<NovsPlayer>();
        for (NovsPlayer player : players) {
            if (player.getTeam().equals(this)) {
                teamMembers.add(player);
            }
        }

        return teamMembers;
    }

    public void setScoreboardTeam(Team scoreboardTeam) {
        this.scoreboardTeam = scoreboardTeam;
    }

    public Team getScoreboardTeam() {
        return scoreboardTeam;
    }
}
