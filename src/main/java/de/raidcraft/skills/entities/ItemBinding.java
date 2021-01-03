package de.raidcraft.skills.entities;

import io.ebean.Finder;
import io.ebean.annotation.DbEnumValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import org.bukkit.Material;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ToString
@Getter
@Setter
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "rcs_player_bindings")
public class ItemBinding extends BaseEntity {

    public static final Finder<UUID, ItemBinding> find = new Finder<>(ItemBinding.class);

    static ItemBinding create(PlayerSkill skill, Material material, Action action) {

        return find(skill.player().id(), material, action).orElseGet(() -> {
            ItemBinding binding = new ItemBinding(skill, material, action);
            binding.save();
            return binding;
        });
    }

    static Optional<ItemBinding> find(UUID playerId, Material material, Action action) {

        return find.query().where()
                .eq("player_id", playerId)
                .and().eq("material", material.getKey().toString())
                .and().eq("action", action)
                .findOneOrEmpty();
    }

    static List<ItemBinding> find(UUID playerId, Material material) {

        return find.query().where()
                .eq("player_id", playerId)
                .and().eq("material", material.getKey().toString())
                .findList();
    }

    static List<ItemBinding> find(UUID playerId) {

        return find.query().where()
                .eq("player_id", playerId)
                .findList();
    }

    @ManyToOne
    private SkilledPlayer player;
    @ManyToOne
    private PlayerSkill skill;
    private String material;
    private Action action;

    ItemBinding(PlayerSkill skill, Material material, Action action) {
        this.player = skill.player();
        this.skill = skill;
        this.material(material);
        this.action = action;
    }

    public ItemBinding material(Material material) {

        this.material = material.getKey().toString();
        return this;
    }

    public Material material() {

        return Material.matchMaterial(material);
    }

    @Getter
    public enum Action {

        RIGHT_CLICK("Rechtsklick"),
        LEFT_CLICK("Linksklick");

        private final String friendlyName;

        Action(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        @DbEnumValue
        public String getValue() {

            return name();
        }
    }
}
