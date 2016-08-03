package com.k9rosie.novswar.util;

public enum RegionType {
    BATTLEFIELD,
    INTERMISSION_GATE,
    DEATH_REGION,
    OBJECTIVE,
    TEAM_SPAWN;

    public static RegionType parseString(String regionType) {
        switch (regionType.toLowerCase()) {
            case "battlefield": return RegionType.BATTLEFIELD;
            case "intermission_gate": return RegionType.INTERMISSION_GATE;
            case "death_region": return RegionType.DEATH_REGION;
            case "objective": return RegionType.OBJECTIVE;
            case "team_spawn": return RegionType.TEAM_SPAWN;
        }
        return null;
    }
}
