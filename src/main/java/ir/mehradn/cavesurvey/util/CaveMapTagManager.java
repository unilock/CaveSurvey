package ir.mehradn.cavesurvey.util;

import ir.mehradn.cavesurvey.component.CaveSurveyData;
import ir.mehradn.cavesurvey.component.ModDataComponents;
import ir.mehradn.cavesurvey.item.ModItems;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public interface CaveMapTagManager {
    static boolean isCaveMap(ItemStack stack) {
        return (stack.is(ModItems.CAVE_MAP) || stack.is(ModItems.FILLED_CAVE_MAP));
    }

    static int getSightLevel(ItemStack stack) {
        if (!(stack.get(ModDataComponents.CAVE_SURVEY) instanceof CaveSurveyData data))
            return 0;
        return Mth.clamp(data.sight(), 0, 2);
    }

    static void setSightLevel(ItemStack stack, int sightLevel) {
        stack.update(ModDataComponents.CAVE_SURVEY, CaveSurveyData.DEFAULT, data -> new CaveSurveyData((byte) Mth.clamp(sightLevel, 0, 2), data.lore()));
    }

    static int getLore(ItemStack stack) {
        if (!(stack.get(ModDataComponents.CAVE_SURVEY) instanceof CaveSurveyData data))
            return 0;
        return data.lore() - 1;
    }
}
