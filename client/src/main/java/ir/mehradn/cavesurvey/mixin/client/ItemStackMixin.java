package ir.mehradn.cavesurvey.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import ir.mehradn.cavesurvey.util.CaveMapTagManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @WrapOperation(method = "getTooltipLines", at = @At(value = "INVOKE", ordinal = 0, target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private boolean addCaveMapHoverText(List<Component> instance, Object component, Operation<Boolean> original, Item.TooltipContext tooltipContext,
                                        @Nullable Player player, TooltipFlag isAdvanced) {
        original.call(instance, component);

        ItemStack stack = (ItemStack)(Object)this;
        if (!isAdvanced.isAdvanced() || !CaveMapTagManager.isClientCaveMap(stack) || CaveMapTagManager.getLore(stack) == -1)
            return true;

        MapId mapId = stack.get(DataComponents.MAP_ID);
        MapItemSavedData data = (mapId == null || player == null ? null : MapItem.getSavedData(mapId, player.level()));
        MapPostProcessing mapPostProcessing = stack.get(DataComponents.MAP_POST_PROCESSING);
        if (mapId == null || data == null || mapPostProcessing == null) {
            if (isAdvanced.isAdvanced())
                instance.add(Component.translatable("filled_map.unknown").withStyle(ChatFormatting.GRAY));
            return true;
        }

        boolean toBeScaled = MapPostProcessing.SCALE.equals(mapPostProcessing);
        boolean toBeLocked = MapPostProcessing.LOCK.equals(mapPostProcessing);

        if (data.locked || toBeLocked)
            instance.add(Component.translatable("filled_map.locked").withStyle(ChatFormatting.GRAY));

        if (toBeScaled || !toBeLocked)
            instance.add(Component.translatable("filled_map.id", mapId).withStyle(ChatFormatting.GRAY));

        int sight = Math.min(CaveMapTagManager.getSightLevel(stack), 2);
        instance.add(Component.translatable("filled_cave_map.sight_range", 1 << (sight + 4)).withStyle(ChatFormatting.GRAY));
        instance.add(Component.translatable("filled_cave_map.sight_level", sight, 2).withStyle(ChatFormatting.GRAY));

        // TODO: Math.min(data.scale + toBeScaled, 2)
        int scale = Math.min(data.scale + 1, 2);
        instance.add(Component.translatable("filled_map.scale", 1 << scale).withStyle(ChatFormatting.GRAY));
        instance.add(Component.translatable("filled_map.level", scale, 2).withStyle(ChatFormatting.GRAY));

        return true;
    }

    @WrapOperation(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V"))
    private <T> void truncateLore(ItemStack instance, DataComponentType<T> component, Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag isAdvanced, Operation<Void> original) {
        if (DataComponents.LORE.equals(component)) {
            List<Component> list = new ArrayList<>();
            Consumer<Component> tmpAdder = list::add;
            original.call(instance, component, context, tmpAdder, isAdvanced);
            if (isAdvanced.isAdvanced()) {
                ItemStack stack = (ItemStack)(Object)this;
                int truncate = Mth.clamp(CaveMapTagManager.getLore(stack), 0, list.size());
                for (int i = 0; i < truncate; i++)
                    list.removeFirst();
            }
            for (Component line : list) {
                tooltipAdder.accept(line);
            }
        }
    }
}
