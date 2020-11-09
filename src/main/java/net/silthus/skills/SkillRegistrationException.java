package net.silthus.skills;

public class SkillRegistrationException extends Exception {


    public SkillRegistrationException() {

    }

    public SkillRegistrationException(String message) {

        super(message);
    }

    public SkillRegistrationException(String message, Throwable cause) {

        super(message, cause);
    }

    public SkillRegistrationException(Throwable cause) {

        super(cause);
    }
}
