package ir.mehradn.cavesurvey.event;

import ir.mehradn.cavesurvey.item.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;

public class LootTableModification {
    public static void register() {
        LootTableEvents.MODIFY.register(LootTableModification::addEmptyCaveMap);
    }

    private static void addEmptyCaveMap(ResourceKey<LootTable> key, LootTable.Builder builder, LootTableSource source, HolderLookup.Provider provider) {
        if (!key.equals(BuiltInLootTables.ANCIENT_CITY))
            return;
        LootPool.Builder pool = LootPool.lootPool()
            .add(LootItem.lootTableItem(ModItems.CAVE_MAP).setWeight(6))
            .add(EmptyLootItem.emptyItem().setWeight(7));
        builder.withPool(pool);
    }
}
