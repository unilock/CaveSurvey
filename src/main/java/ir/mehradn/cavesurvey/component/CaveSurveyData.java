package ir.mehradn.cavesurvey.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CaveSurveyData(byte sight, byte lore) {
	public static final CaveSurveyData DEFAULT = new CaveSurveyData((byte) 0, (byte) 0);
	
	public static final Codec<CaveSurveyData> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.BYTE.fieldOf("sight").forGetter(CaveSurveyData::sight), 
					Codec.BYTE.fieldOf("lore").forGetter(CaveSurveyData::lore)
			).apply(instance, CaveSurveyData::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, CaveSurveyData> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BYTE,
			CaveSurveyData::sight,
			ByteBufCodecs.BYTE,
			CaveSurveyData::lore,
			CaveSurveyData::new
	);
}
