package de.raidcraft.skills.entities;

import io.ebean.Model;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Material;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles and caches the item bindings for the given player.
 * <p>As bindings are potentially checked at every player interact
 * a caching layer is needed to avoid mass database queries per second.
 * <p>This class acts as the caching layer in between the database and the bindings.
 * <p>Adding or removing bindings will be directly performed against the database
 * and then updated in this cache.
 */
@Getter
@Accessors(fluent = true)
public class ItemBindings {

    private final UUID playerId;
    private final List<ItemBinding> bindings;

    ItemBindings(SkilledPlayer player) {
        this.playerId = player.id();
        this.bindings = player.itemBindings();
    }

    /**
     * Binds the skill to the given material and action for the player of this binding.
     * <p>No new binding will be created if the binding already exists for this skill.
     * <p>If the material and action is already bound to another skill nothing will happen
     * and the existing binding is returned.
     *
     * @param skill the skill to bind
     * @param material the material to bind to
     * @param action the action that is bound
     * @return the created or existing binding, or the binding for the same material and action with a different skill
     */
    public ItemBinding bind(PlayerSkill skill, Material material, ItemBinding.Action action) {

        return get(material, action).orElseGet(() -> {
            ItemBinding itemBinding = ItemBinding.create(skill, material, action);
            bindings.add(itemBinding);
            return itemBinding;
        });
    }

    /**
     * Unbinds all skills and actions that are bound to the given material.
     * <p>Nothing will happen if no binding exists.
     *
     * @param material the material to unbind
     * @return the item bindings of this player
     */
    public ItemBindings unbind(Material material) {

        ItemBinding.find(playerId, material).forEach(this::unbind);
        return this;
    }

    /**
     * Unbinds the skill for the given material and action if present.
     * <p>Nothing will happen if no binding exists.
     *
     * @param material the material to unbind
     * @param action the action to unbind can be null
     * @return the item bindings of this player
     */
    public ItemBindings unbind(Material material, ItemBinding.Action action) {

        if (action == null) return unbind(material);

        ItemBinding.find(playerId, material, action).ifPresent(this::unbind);
        return this;
    }

    /**
     * Removes all bindings for the given skill.
     *
     * @param skill the skill that should have its bindings removed
     */
    public void unbind(PlayerSkill skill) {

        get(skill).forEach(this::unbind);
    }

    /**
     * Unbinds the given item binding.
     *
     * @param binding the binding that should be removed. can be null.
     */
    public void unbind(ItemBinding binding) {

        if (binding == null) return;
        bindings.remove(binding);
        binding.delete();
    }

    /**
     * Clears all bindings of this player.
     *
     * @return the item bindings of this player
     */
    public ItemBindings clear() {

        bindings.clear();
        ItemBinding.find(playerId).forEach(Model::delete);
        return this;
    }

    /**
     * Checks if the bindings of this plaser contain a binding for the given material and action.
     *
     * @param material the material to check
     * @param action the action to check
     * @return true if the player has any binding for the given material and action
     */
    public boolean contains(Material material, ItemBinding.Action action) {

        return get(material, action).isPresent();
    }

    /**
     * Checks if the player has any binding on the given material.
     *
     * @param material the material to check
     * @return true if the player has any binding on the given material
     */
    public boolean contains(Material material) {

        return !ItemBinding.find(playerId, material).isEmpty();
    }

    /**
     * Gets a binding for the given material and action.
     *
     * @param material the material to get the binding for
     * @param action the action to get a binding for
     * @return the binding or an empty optional
     */
    public Optional<ItemBinding> get(Material material, ItemBinding.Action action) {

        return bindings.stream()
                .filter(itemBinding -> itemBinding.material().equals(material))
                .filter(itemBinding -> itemBinding.action().equals(action))
                .findAny();
    }

    /**
     * Gets all bindings that are associated with the given skill.
     *
     * @param skill the skill to get bindings for
     * @return all bindings that are bound to the given skill
     */
    public List<ItemBinding> get(PlayerSkill skill) {

        return bindings().stream()
                .filter(itemBinding -> itemBinding.skill().equals(skill))
                .collect(Collectors.toList());
    }
}
