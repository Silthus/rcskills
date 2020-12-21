package de.raidcraft.skills.entities;

import io.ebean.Finder;
import io.ebean.annotation.DbEnumValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "rcs_skill_slots")
@Accessors(fluent = true)
public class SkillSlot extends BaseEntity {

    public static final Finder<UUID, SkillSlot> find = new Finder<>(SkillSlot.class);

    public static Optional<SkillSlot> of(@NonNull PlayerSkill skill) {

        return find.query()
                .where().eq("skill_id", skill.id())
                .findOneOrEmpty();
    }

    @ManyToOne
    private SkilledPlayer player;
    @OneToOne
    @Setter(AccessLevel.PACKAGE)
    private PlayerSkill skill;
    private Status status = Status.ELIGIBLE;

    SkillSlot(SkilledPlayer player) {
        this.player = player;
    }

    public boolean free() {

        return status == Status.FREE;
    }

    public SkillSlot assign(PlayerSkill skill) {

        if (skill == null && status == Status.IN_USE) {
            status = Status.FREE;
        } else if (skill != null) {
            status = Status.IN_USE;
        }

        this.skill = skill;
        return this;
    }

    public SkillSlot unassign() {

        if (this.skill != null) {
            this.status = Status.FREE;
        }
        this.skill = null;
        return this;
    }

    public enum Status {
        /**
         * The skill slot is available to the player and can be bought.
         */
        ELIGIBLE,
        /**
         * The skill slot was bought by the player and can now be used to host skills.
         */
        FREE,
        /**
         * The skill slot is used by a skill.
         */
        IN_USE;

        @DbEnumValue
        public String getValue() {

            return name();
        }
    }
}
