package dev.nolij.nolijium.mixin;

import dev.nolij.nolijium.shape.BitSetVoxelSetCombiner;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.shape.BitSetVoxelSet;
import net.minecraft.util.shape.PairList;
import net.minecraft.util.shape.SimplePairList;
import net.minecraft.util.shape.VoxelSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BitSetVoxelSet.class)
public class BitSetVoxelSetMixin {
	
	@Inject(method = "combine", at = @At("HEAD"), cancellable = true)
	private static void combineFast(VoxelSet first, VoxelSet second, PairList xPoints, PairList yPoints, PairList zPoints, BooleanBiFunction function, CallbackInfoReturnable<BitSetVoxelSet> cir) {
		if (first instanceof BitSetVoxelSet firstB && 
			second instanceof BitSetVoxelSet secondB && 
			xPoints instanceof SimplePairList && 
			yPoints instanceof SimplePairList && 
			zPoints instanceof SimplePairList) {
			cir.setReturnValue(new BitSetVoxelSetCombiner(firstB, secondB, xPoints, yPoints, zPoints, function).getResult());
		}
	}
	
}
