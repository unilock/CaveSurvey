package ir.mehradn.cavesurvey.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multisets;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import ir.mehradn.cavesurvey.CaveSurvey;
import ir.mehradn.cavesurvey.mixin.accessor.MapItemAccessor;
import ir.mehradn.cavesurvey.util.CaveMapTagManager;
import ir.mehradn.cavesurvey.util.CaveMappingAlgorithm;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CaveMapItem extends MapItem implements PolymerItem {
    public static final int MAP_SIZE = 128;
    public static final PolymerModelData MODEL_DATA = PolymerResourcePackUtils.requestModel(Items.FILLED_MAP,
        CaveSurvey.rl("item/filled_cave_map"));

    public CaveMapItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(Level level, int x, int z, byte scale, boolean trackingPosition, boolean unlimitedTracking) {
        ItemStack stack = new ItemStack(ModItems.FILLED_CAVE_MAP);
        stack.set(DataComponents.MAP_ID, MapItemAccessor.invokeCreateNewSavedData(level, x, z, scale, trackingPosition, unlimitedTracking, level.dimension()));
        CaveMapTagManager.setSightLevel(stack, 0);
        return stack;
    }

    public static void updateMap(Level level, Entity viewer, MapItemSavedData data, int sight) {
        if (level.dimension() != data.dimension || !(viewer instanceof Player player))
            return;

        BlockPos headPos = player.blockPosition().above();
        int scale = 1 << data.scale;
        int centerX = data.centerX;
        int centerZ = data.centerZ;
        int relativeX = (headPos.getX() - centerX) / scale + MAP_SIZE / 2;
        int relativeZ = (headPos.getZ() - centerZ) / scale + MAP_SIZE / 2;
        int blockRadius = 1 << (sight + 4);
        int pixelRadius = blockRadius / scale;

        if (MAP_SIZE + pixelRadius + 4 < relativeX || relativeX < -pixelRadius - 4 ||
            MAP_SIZE + pixelRadius + 4 < relativeZ || relativeZ < -pixelRadius - 4)
            return;
        CaveMappingAlgorithm.PixelMatrix matrix = CaveMappingAlgorithm.run(headPos, blockRadius, level);

        for (int pixelX = relativeX - pixelRadius; pixelX < relativeX + pixelRadius; pixelX++) {
            double previousAverageHeight = 0.0;
            for (int pixelZ = relativeZ - pixelRadius; pixelZ < relativeZ + pixelRadius; pixelZ++) {
                if (pixelX < 0 || pixelZ < 0 || pixelX >= MAP_SIZE || pixelZ >= MAP_SIZE)
                    continue;
                int distance = Mth.square(pixelX - relativeX) + Mth.square(pixelZ - relativeZ);
                int realX = (centerX / scale + pixelX - MAP_SIZE / 2) * scale;
                int realZ = (centerZ / scale + pixelZ - MAP_SIZE / 2) * scale;

                double averageHeight = 0.0;
                double averageFluidDepth = 0.0;
                int pixelCount = 0;
                int brightCount = 0;
                LinkedHashMultiset<MapColor> innerColors = LinkedHashMultiset.create();
                for (int innerX = 0; innerX < scale; innerX++) {
                    for (int innerZ = 0; innerZ < scale; innerZ++) {
                        int x = realX + innerX;
                        int z = realZ + innerZ;
                        CaveMappingAlgorithm.PixelInfo info = matrix.get(x, z);
                        if (info == null)
                            continue;

                        pixelCount++;
                        if (info.reachesSky())
                            brightCount++;

                        averageFluidDepth += info.fluidDepth();
                        averageHeight += info.y();
                        innerColors.add(info.color());
                    }
                }

                if (pixelCount == 0) {
                    previousAverageHeight = 0;
                    continue;
                }
                averageHeight /= pixelCount;
                averageFluidDepth /= pixelCount;

                MapColor color = Iterables.getFirst(Multisets.copyHighestCountFirst(innerColors), MapColor.NONE);

                int brightnessLevel;
                if (color == MapColor.WATER) {
                    double darkness = averageFluidDepth * 0.1 + (double)(pixelX + pixelZ & 1) * 0.2;
                    if (darkness > 0.9)
                        brightnessLevel = 0;
                    else if (darkness > 0.5)
                        brightnessLevel = 1;
                    else
                        brightnessLevel = 2;
                } else {
                    double brightness =
                        (averageHeight - previousAverageHeight) * 4.0 / (double)(scale + 4) + ((double)(pixelX + pixelZ & 1) - 0.5) * 0.4;
                    if (brightness < -0.6)
                        brightnessLevel = 0;
                    else if (brightness < 0.6)
                        brightnessLevel = 1;
                    else
                        brightnessLevel = 2;
                }
                if (brightCount >= pixelCount / 2)
                    brightnessLevel++;

                previousAverageHeight = averageHeight;
                if (pixelCount < Mth.square(scale) / 2 || distance >= Mth.square(pixelRadius))
                    continue;

                MapColor.Brightness brightness = switch (brightnessLevel) {
                    case 0 -> MapColor.Brightness.LOWEST;
                    case 1 -> MapColor.Brightness.LOW;
                    case 2 -> MapColor.Brightness.NORMAL;
                    default -> MapColor.Brightness.HIGH;
                };
                data.updateColor(pixelX, pixelZ, color.getPackedId(brightness));
            }
        }
    }

    public static void updateBanners(Level level, Entity viewer, MapItemSavedData data) {
        if (level.dimension() != data.dimension || !(viewer instanceof Player player))
            return;

        BlockPos headPos = player.blockPosition().above();
        int scale = 1 << data.scale;
        int centerX = data.centerX;
        int centerZ = data.centerZ;
        int relativeX = (headPos.getX() - centerX) / scale + MAP_SIZE / 2;
        int relativeZ = (headPos.getZ() - centerZ) / scale + MAP_SIZE / 2;
        int blockRadius = 64;
        int pixelRadius = blockRadius / scale;

        if (MAP_SIZE + pixelRadius + 4 < relativeX || relativeX < -pixelRadius - 4 ||
            MAP_SIZE + pixelRadius + 4 < relativeZ || relativeZ < -pixelRadius - 4)
            return;
        for (int pixelX = relativeX - pixelRadius; pixelX < relativeX + pixelRadius; pixelX++) {
            for (int pixelZ = relativeZ - pixelRadius; pixelZ < relativeZ + pixelRadius; pixelZ++) {
                if (pixelX < 0 || pixelZ < 0 || pixelX >= MAP_SIZE || pixelZ >= MAP_SIZE)
                    continue;
                int realX = (centerX / scale + pixelX - MAP_SIZE / 2) * scale;
                int realZ = (centerZ / scale + pixelZ - MAP_SIZE / 2) * scale;

                for (int innerX = 0; innerX < scale; innerX++)
                    for (int innerZ = 0; innerZ < scale; innerZ++)
                        data.checkBanners(level, realX + innerX, realZ + innerZ);
            }
        }
    }

    public int countHoverText(ItemStack stack, Level level) {
        if (!stack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP))
            return -1;

        MapId mapId = stack.get(DataComponents.MAP_ID);
        MapItemSavedData data = (mapId == null ? null : MapItem.getSavedData(mapId, level));
        MapPostProcessing mapPostProcessing = stack.get(DataComponents.MAP_POST_PROCESSING);
        if (mapId == null || data == null || mapPostProcessing == null)
            return 0;

        int count = 1;
        if (!stack.has(DataComponents.CUSTOM_NAME))
            count++;
        return count;
    }

    @Override
    public Item getPolymerItem(ItemStack stack, ServerPlayer player) {
        return Items.FILLED_MAP;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack stack, TooltipFlag context, HolderLookup.Provider lookup, @Nullable ServerPlayer player) {
        ItemStack out = PolymerItemUtils.createItemStack(stack, context, lookup, player);
        MapId mapId = stack.get(DataComponents.MAP_ID);
        if (mapId != null)
            out.set(DataComponents.MAP_ID, mapId);
        CaveMapTagManager.setSightLevel(out, CaveMapTagManager.getSightLevel(stack));
        CaveMapTagManager.setLore(out, countHoverText(stack, player.level()));
        return out;
    }

    @Override
    public int getPolymerCustomModelData(ItemStack stack, ServerPlayer player) {
        return MODEL_DATA.value();
    }

    @Override
    public void update(Level level, Entity viewer, MapItemSavedData data) {
        updateBanners(level, viewer, data);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        MapItemSavedData data = MapItem.getSavedData(stack, level);
        if (data != null && !data.locked)
            updateMap(level, player, data, CaveMapTagManager.getSightLevel(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        MapId mapId = stack.get(DataComponents.MAP_ID);
        MapItemSavedData data = (mapId == null ? null : context.mapData(mapId));
        MapPostProcessing mapPostProcessing = stack.get(DataComponents.MAP_POST_PROCESSING);
        if (mapId == null || data == null || mapPostProcessing == null)
            return;

        boolean toBeLocked = MapPostProcessing.LOCK.equals(mapPostProcessing);
        if (!stack.has(DataComponents.CUSTOM_NAME))
            tooltipComponents.add(Component.literal("#" + mapId).withStyle(ChatFormatting.GRAY));
        if (data.locked || toBeLocked)
            tooltipComponents.add(Component.translatable("filled_map.locked").withStyle(ChatFormatting.GRAY));
        else {
            int sight = CaveMapTagManager.getSightLevel(stack);
            if (sight == 0)
                tooltipComponents.add(Component.translatable("filled_cave_map.low_sight").withStyle(ChatFormatting.GRAY));
            else if (sight == 1)
                tooltipComponents.add(Component.translatable("filled_cave_map.medium_sight").withStyle(ChatFormatting.GRAY));
            else
                tooltipComponents.add(Component.translatable("filled_cave_map.high_sight").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide)
            return InteractionResultHolder.success(stack);

        player.awardStat(Stats.ITEM_USED.get(this));

        MapItemSavedData data = getSavedData(stack, level);
        if (data == null)
            return InteractionResultHolder.fail(stack);
        if (data.locked)
            return InteractionResultHolder.consume(stack);

        updateMap(level, player, data, CaveMapTagManager.getSightLevel(stack));
        return InteractionResultHolder.success(stack);
    }
}
