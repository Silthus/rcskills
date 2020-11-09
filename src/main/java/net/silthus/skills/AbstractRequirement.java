package net.silthus.skills;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import static net.silthus.skills.Messages.msg;

@Data
@EqualsAndHashCode(of = "identifier")
@Accessors(fluent = true)
public abstract class AbstractRequirement implements Requirement {

    private final String identifier;
    private final String name;
    private final String description;

    public AbstractRequirement(String identifier, String name, String description) {

        this.identifier = identifier;
        this.name = name;
        this.description = description;
    }

    public AbstractRequirement(String identifier) {

        this.identifier = identifier;
        this.name = msg(msgIdentifier("name"), identifier);
        this.description = msg(msgIdentifier("description"));
    }

    protected final String msgIdentifier(String key) {

        return "requirements." + identifier() + "." + key;
    }
}
