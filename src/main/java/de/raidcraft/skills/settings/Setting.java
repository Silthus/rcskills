package de.raidcraft.skills.settings;

import de.raidcraft.skills.entities.ItemBindings;
import lombok.Value;

@Value
public class Setting<TType> {

    public static final Setting<ItemBindings> ITEM_BINDINGS = new Setting<>("item-binding", "Item Bindings", "Aktive Skills die auf Items gebunden sind.", ItemBindings.class);

    String key;
    String name;
    String description;
    Class<TType> type;
}
