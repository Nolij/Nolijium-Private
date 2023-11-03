package dev.nolij.nolijium.mixin;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.shape.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(VoxelShapes.class)
public class VoxelShapesMixin {
	
	/**
	 * @author	Nolij
	 * @reason	Optimization requires SimplePairLists
	 */
	@Overwrite
	@VisibleForTesting
	public static PairList createListPair(int size, DoubleList first, DoubleList second, boolean includeFirst, boolean includeSecond) {
		return new SimplePairList(first, second, includeFirst, includeSecond);
	}
	
}
