package ir.mehradn.cavesurvey.component;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.function.UnaryOperator;

public class ModDataComponents {
	public static final DataComponentType<CaveSurveyData> CAVE_SURVEY = registerType("cave_survey", builder -> builder.persistent(CaveSurveyData.CODEC).networkSynchronized(CaveSurveyData.STREAM_CODEC));

	public static void register() {
		// NO-OP
	}

	private static <T> DataComponentType<T> registerType(String path, UnaryOperator<DataComponentType.Builder<T>> builder) {
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, ResourceLocation.fromNamespaceAndPath("cave-survey", path), builder.apply(DataComponentType.builder()).build());
	}
}
