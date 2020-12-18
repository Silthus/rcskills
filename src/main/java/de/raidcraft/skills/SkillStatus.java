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
}
