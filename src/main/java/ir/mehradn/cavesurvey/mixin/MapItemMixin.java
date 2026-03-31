package ir.mehradn.cavesurvey.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import ir.mehradn.cavesurvey.item.CaveMapItem;
import ir.mehradn.cavesurvey.util.CaveMapTagManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapItem.class)
public abstract class MapItemMixin {
	@Inject(method = "inventoryTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/MapItem;update(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;)V"))
	private void updateCaveMap(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected, CallbackInfo ci, @Local MapItemSavedData mapItemSavedData) {
		if (stack.getItem() instanceof CaveMapItem) {
			CaveMapItem.updateMap(level, entity, mapItemSavedData, CaveMapTagManager.getSightLevel(stack));
		}
	}
}
