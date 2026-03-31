package ir.mehradn.cavesurvey.item.crafting;

import eu.pb4.polymer.core.api.item.PolymerRecipe;
import ir.mehradn.cavesurvey.event.OverworldStore;
import ir.mehradn.cavesurvey.item.ModItems;
import ir.mehradn.cavesurvey.util.upgrades.ServerCaveMapUpgrade;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class CaveMapCloningRecipe extends CustomRecipe implements PolymerRecipe {
    public CaveMapCloningRecipe(CraftingBookCategory craftingBookCategory) {
        super(craftingBookCategory);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack filled = null;
        int empty = 0;
        int other = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(ModItems.FILLED_CAVE_MAP)) {
                if (filled != null)
                    return false;
                filled = stack;
            } else if (ServerCaveMapUpgrade.CLONING.acceptsItem(stack)) {
                empty++;
            } else if (!stack.isEmpty()) {
                other++;
            }
        }
        return filled != null && empty > 0 && other == 0 && ServerCaveMapUpgrade.CLONING.valid(filled, level);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack filled = null;
        int empty = 0;
        int other = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(ModItems.FILLED_CAVE_MAP)) {
                if (filled != null)
                    return ItemStack.EMPTY;
                filled = stack;
            } else if (ServerCaveMapUpgrade.CLONING.acceptsItem(stack)) {
                empty++;
            } else if (!stack.isEmpty()) {
                other++;
            }
        }

        if (filled == null || empty == 0 && other > 0)
            return ItemStack.EMPTY;
        ItemStack cloned = ServerCaveMapUpgrade.CLONING.upgrade(filled, OverworldStore.get());
        cloned.setCount(empty + 1);
        return cloned;
    }

    public boolean canCraftInDimensions(int width, int height) {
        return width >= 1 && height >= 1 && width * height >= 2;
    }

    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipes.CAVE_MAP_CLONING;
    }
}
