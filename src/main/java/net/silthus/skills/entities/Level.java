package net.silthus.skills.entities;

import io.ebean.Finder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.silthus.ebean.BaseEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter(AccessLevel.PACKAGE)
@Table(name = "rcs_levels")
public class Level extends BaseEntity {

    public static final Finder<UUID, Level> find = new Finder<>(Level.class);

    private int level = 1;
    private long totalExp = 0;

    @OneToMany(cascade = CascadeType.REMOVE)
    private List<LevelHistory> history = new ArrayList<>();

    Level() {
    }
}
