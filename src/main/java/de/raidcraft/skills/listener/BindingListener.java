package de.raidcraft.skills.listener;

import de.raidcraft.skills.Messages;
import de.raidcraft.skills.entities.ItemBinding;
import de.raidcraft.skills.entities.ItemBindings;
import de.raidcraft.skills.entities.SkilledPlayer;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class BindingListener implements Listener {

    private final Map<UUID, ItemBindings> bindingsMap = new HashMap<>();
    @Getter
    private final Consumer<UUID> updateBindings;

    public BindingListener() {

        updateBindings = (player) -> {
            SkilledPlayer skilledPlayer = SkilledPlayer.find.byId(player);
            if (skilledPlayer == null) {
                bindingsMap.remove(player);
            } else {
                ItemBindings bindings = skilledPlayer.bindings();
                bindingsMap.put(player, bindings);
            }
        };
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {

        bindingsMap.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {

        if (!bindingsMap.containsKey(event.getPlayer().getUniqueId())) {
            updateBindings.accept(event.getPlayer().getUniqueId());
        }

        ItemBindings bindings = bindingsMap.get(event.getPlayer().getUniqueId());

        Material type = event.getPlayer().getInventory().getItemInMainHand().getType();
        Optional<ItemBinding> binding = Optional.empty();
        switch (event.getAction()) {
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                binding = bindings.get(type, ItemBinding.Action.LEFT_CLICK);
                break;
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                binding = bindings.get(type, ItemBinding.Action.RIGHT_CLICK);
                break;
        }

        binding.map(ItemBinding::skill)
                .ifPresent(skill -> skill.execute(executionResult ->
                        Messages.send(event.getPlayer(), Messages.resultOf(executionResult))));
    }
}
