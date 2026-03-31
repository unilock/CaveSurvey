package ir.mehradn.cavesurvey.util.upgrades;

import ir.mehradn.cavesurvey.util.CaveMapTagManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public interface CaveMapUpgrade {
    Integer id();

    Item item();

    boolean vanilla();

    boolean valid(ItemStack mapStack, Level level);

    ItemStack upgrade(ItemStack mapStack, Level level);

    interface Cloning extends CaveMapUpgrade {
        @Override
        default Integer id() {
            return 0;
        }

        @Override
        default Item item() {
            return Items.MAP;
        }

        @Override
        default boolean vanilla() {
            return true;
        }

        @Override
        default boolean valid(ItemStack mapStack, Level level) {
            MapItemSavedData data = MapItem.getSavedData(mapStack, level);
            return data != null && !data.isExplorationMap();
        }

        @Override
        default ItemStack upgrade(ItemStack mapStack, Level level) {
            ItemStack newStack = mapStack.copyWithCount(2);
            CaveMapTagManager.setSightLevel(mapStack, 0);
            return newStack;
        }
    }

    interface Extending extends CaveMapUpgrade {
        @Override
        default Integer id() {
            return 1;
        }

        @Override
        default Item item() {
            return Items.PAPER;
        }

        @Override
        default boolean vanilla() {
            return true;
        }

        @Override
        default boolean valid(ItemStack stack, Level level) {
            MapItemSavedData data = MapItem.getSavedData(stack, level);
            return data != null && !data.locked && !data.isExplorationMap() && data.scale < 2;
        }

        @Override
        default ItemStack upgrade(ItemStack mapStack, Level level) {
            ItemStack newStack = mapStack.copyWithCount(1);
            MapItemSavedData mapItemSavedData = MapItem.getSavedData(mapStack, level);
            if (mapItemSavedData != null) {
                MapId mapId = level.getFreeMapId();
                level.setMapData(mapId, mapItemSavedData.scaled());
                newStack.set(DataComponents.MAP_ID, mapId);
            }
            return newStack;
        }
    }

    interface Improving extends CaveMapUpgrade {
        @Override
        default Integer id() {
            return 2;
        }

        @Override
        default Item item() {
            return Items.AMETHYST_SHARD;
        }

        @Override
        default boolean vanilla() {
            return false;
        }

        @Override
        default boolean valid(ItemStack stack, Level level) {
            MapItemSavedData data = MapItem.getSavedData(stack, level);
            return data != null && !data.locked && CaveMapTagManager.getSightLevel(stack) < 2;
        }

        @Override
        default ItemStack upgrade(ItemStack mapStack, Level level) {
            int newVision = CaveMapTagManager.getSightLevel(mapStack) + 1;
            ItemStack newStack = mapStack.copyWithCount(1);
            CaveMapTagManager.setSightLevel(newStack, newVision);
            return newStack;
        }
    }

    interface Locking extends CaveMapUpgrade {
        @Override
        default Integer id() {
            return 3;
        }

        @Override
        default Item item() {
            return Items.GLASS_PANE;
        }

        @Override
        default boolean vanilla() {
            return true;
        }

        @Override
        default boolean valid(ItemStack stack, Level level) {
            MapItemSavedData data = MapItem.getSavedData(stack, level);
            return data != null && !data.isExplorationMap() && !data.locked;
        }

        @Override
        default ItemStack upgrade(ItemStack mapStack, Level level) {
            ItemStack newStack = mapStack.copyWithCount(1);
            MapItemSavedData mapItemSavedData = MapItem.getSavedData(mapStack, level);
            if (mapItemSavedData != null) {
                MapId mapId = level.getFreeMapId();
                MapItemSavedData mapItemSavedData2 = mapItemSavedData.locked();
                level.setMapData(mapId, mapItemSavedData2);
                newStack.set(DataComponents.MAP_ID, mapId);
            }
            CaveMapTagManager.setSightLevel(newStack, 0);
            return newStack;
        }
    }
}
