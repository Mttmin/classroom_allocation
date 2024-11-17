package com.roomallocation.model;

public enum RoomType {
    COULOIR_VANNEAU("Couloir cour Vanneau"),
    COULOIR_SCOLARITE("couloir de la scolarité"),
    COULOIR_LABOS("couloir des labos"),
    SALLES_100("Salles 100"),
    AMPHI_COULOIR_BINETS("Amphi couloir binets"),
    SALLES_INFO("Salles info"),
    SALLES_LANGUES("Salles langues"),
    NOUVEAUX_AMPHIS("Nouveaux amphis"),
    GRANDS_AMPHIS("Grands amphis"),
    AMPHIS_80_100("Amphis 80-100 places");

    private final String displayName;
    
    RoomType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static RoomType fromString(String text) {
        for (RoomType type : RoomType.values()) {
            if (type.displayName.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No room type found for: " + text);
    }
}
