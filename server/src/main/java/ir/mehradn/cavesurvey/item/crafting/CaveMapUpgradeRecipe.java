package ir.mehradn.cavesurvey.item.crafting;

import eu.pb4.polymer.core.api.item.PolymerRecipe;
import ir.mehradn.cavesurvey.event.OverworldStore;
import ir.mehradn.cavesurvey.item.ModItems;
import ir.mehradn.cavesurvey.util.upgrades.ServerCaveMapUpgrade;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Optional;

public class CaveMapUpgradeRecipe <T extends ServerCaveMapUpgrade> extends ShapedRecipe implements PolymerRecipe {
    public static final HashMap<Integer, RecipeSerializer<?>> serializers = new HashMap<>();
    private final T upgrade;

    public CaveMapUpgradeRecipe(CraftingBookCategory craftingBookCategory, T upgrade) {
        super("", craftingBookCategory, new ShapedRecipePattern(3, 3,
            NonNullList.of(Ingredient.EMPTY,
                Ingredient.of(upgrade.item()), Ingredient.of(upgrade.item()), Ingredient.of(upgrade.item()),
                Ingredient.of(upgrade.item()), Ingredient.of(ModItems.FILLED_CAVE_MAP), Ingredient.of(upgrade.item()),
                Ingredient.of(upgrade.item()), Ingredient.of(upgrade.item()), Ingredient.of(upgrade.item())),
                Optional.empty()),
            new ItemStack(ModItems.CAVE_MAP));
        this.upgrade = upgrade;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (!super.matches(input, level))
            return false;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(ModItems.FILLED_CAVE_MAP))
                return this.upgrade.valid(stack, level);
        }
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(ModItems.FILLED_CAVE_MAP))
                return this.upgrade.upgrade(stack, OverworldStore.get());
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return serializers.get(this.upgrade.id());
    }
}
