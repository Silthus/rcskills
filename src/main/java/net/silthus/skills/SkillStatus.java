package net.silthus.skills;

import io.ebean.annotation.DbEnumValue;

public enum SkillStatus {

    ENABLED,
    DISABLED,
    ACTIVE,
    UNLOCKED,
    INACTIVE,
    REMOVED,
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
