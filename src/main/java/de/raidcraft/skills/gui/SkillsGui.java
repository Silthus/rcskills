package de.raidcraft.skills.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import de.raidcraft.skills.Messages;
import de.raidcraft.skills.entities.PlayerSkill;
import de.raidcraft.skills.entities.SkilledPlayer;
import net.kyori.adventure.text.serializer.craftbukkit.BukkitComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SkillsGui {

    public static void display(Player player) {

        ChestGui gui = new ChestGui(6, "Skills von " + player.getName());

        gui.addPane(skillSlots(player));
        gui.show(player);
    }

    private static PaginatedPane skillSlots(Player player) {

        List<GuiItem> items = SkilledPlayer.getOrCreate(player).activeSkills()
                .stream().filter(skill -> skill.configuredSkill().skillslots() > 0)
                .sorted()
                .map(SkillsGui::skill)
                .collect(Collectors.toList());
        PaginatedPane pane = new PaginatedPane(0, 0, 9, 1);
        pane.populateWithGuiItems(items);

        return pane;
    }

    private static GuiItem skill(PlayerSkill skill) {

        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(BukkitComponentSerializer.gson().serialize(Messages.skill(skill, false)));
        meta.setLore(Arrays.asList(
                BukkitComponentSerializer.gson().serialize(Messages.skillInfo(skill.configuredSkill(), skill.player()))
        ));
        return new GuiItem(item);
    }
}
