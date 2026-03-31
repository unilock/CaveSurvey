package ir.mehradn.cavesurvey.mixin.accessor;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MapItem.class)
public interface MapItemAccessor {
    @Invoker("createNewSavedData")
    static MapId invokeCreateNewSavedData(Level level, int x, int z, int scale, boolean trackingPosition,
                                          boolean unlimitedTracking, ResourceKey<Level> dimension) {
        throw new LinkageError();
    }
}
