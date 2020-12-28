package de.raidcraft.skills;

public class RCSkillsException extends Exception {

    public RCSkillsException() {
    }

    public RCSkillsException(String message) {
        super(message);
    }

    public RCSkillsException(String message, Throwable cause) {
        super(message, cause);
    }

    public RCSkillsException(Throwable cause) {
        super(cause);
    }

    public RCSkillsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
