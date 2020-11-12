package net.silthus.skills;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.InvalidClassException;

import static net.silthus.skills.Messages.msg;

@Data
@Accessors(fluent = true)
public abstract class AbstractRequirement implements Requirement {

    private final String type;
    private final String name;
    private final String description;

    public AbstractRequirement() {
        if (!getClass().isAnnotationPresent(RequirementType.class)) {
            throw new RuntimeException("Cannot instantiate a requirement without a type identifier. " +
                    "Use the @RequirementType annotation or call super(...)");
        }
        this.type = getClass().getAnnotation(RequirementType.class).value().toLowerCase();
        this.name = msg(msgIdentifier("name"), type);
        this.description = msg(msgIdentifier("description"));
    }

    protected final String msgIdentifier(String key) {

        return "requirements." + type() + "." + key;
    }
}
