package ir.mehradn.cavesurvey.util;

import ir.mehradn.cavesurvey.component.CaveSurveyData;
import ir.mehradn.cavesurvey.component.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

public interface CaveMapTagManager {
    String CAVE_MAP_ID = "cave-survey:cave_map";
    String FILLED_CAVE_MAP_ID = "cave-survey:filled_cave_map";

    static boolean isClientCaveMap(ItemStack stack) {
        if (!(stack.get(DataComponents.CUSTOM_DATA) instanceof CustomData customData))
            return false;
        return (stack.is(Items.MAP) && PolymerItemUtils.getPolymerIdentifier(customData).toString().equals(CAVE_MAP_ID) ||
                stack.is(Items.FILLED_MAP) && PolymerItemUtils.getPolymerIdentifier(customData).toString().equals(FILLED_CAVE_MAP_ID));
    }

    static int getSightLevel(ItemStack stack) {
        if (!(stack.get(ModDataComponents.CAVE_SURVEY) instanceof CaveSurveyData data))
            return 0;
        return Mth.clamp(data.sight(), 0, 2);
    }

    static void setSightLevel(ItemStack stack, int sightLevel) {
        stack.update(ModDataComponents.CAVE_SURVEY, CaveSurveyData.DEFAULT, data -> new CaveSurveyData((byte) Mth.clamp(sightLevel, 0, 2), data.lore()));
    }

    static void setLore(ItemStack stack, int lore) {
        stack.update(ModDataComponents.CAVE_SURVEY, CaveSurveyData.DEFAULT, data -> new CaveSurveyData(data.sight(), (byte)(lore + 1)));
    }

    static int getLore(ItemStack stack) {
        if (!(stack.get(ModDataComponents.CAVE_SURVEY) instanceof CaveSurveyData data))
            return 0;
        return data.lore() - 1;
    }
}
