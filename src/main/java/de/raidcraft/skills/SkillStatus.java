package de.raidcraft.skills;

import io.ebean.annotation.DbEnumValue;

public enum SkillStatus {

    ACTIVE,
    UNLOCKED,
    NOT_PRESENT;

    public boolean isActive() {

        return this == ACTIVE;
    }

    public boolean isUnlocked() {

        return isActive() || this == UNLOCKED;
    }

    @DbEnumValue
    public String getValue() {
        return name();
    }

    public String localized() {

        switch (this) {
            case ACTIVE:
                return "aktiv";
            case UNLOCKED:
                return "freigeschaltet";
            default:
            case NOT_PRESENT:
                return "N/A";
        }
    }
}
