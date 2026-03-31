package ir.mehradn.cavesurvey.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import ir.mehradn.cavesurvey.item.ModItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(ItemFrame.class)
public abstract class ItemFrameMixin {
	@WrapOperation(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
	private boolean checkCaveMap(ItemStack instance, Item item, Operation<Boolean> original) {
		return original.call(instance, item) || original.call(instance, ModItems.FILLED_CAVE_MAP);
	}
}
