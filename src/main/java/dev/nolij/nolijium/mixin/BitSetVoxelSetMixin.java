package dev.nolij.nolijium.mixin;

import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.shape.BitSetVoxelSet;
import net.minecraft.util.shape.PairList;
import net.minecraft.util.shape.SimplePairList;
import net.minecraft.util.shape.VoxelSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BitSetVoxelSet.class)
public class BitSetVoxelSetMixin {
	
	/**
	 * @author	Nolij
	 * @reason	the old code is bad lol
	 */
	@Overwrite
	public static BitSetVoxelSet combine(VoxelSet first, VoxelSet second, PairList xPoints, PairList yPoints, PairList zPoints, BooleanBiFunction function) {
		if (!(xPoints instanceof SimplePairList simpleXPoints))
			throw new AssertionError();
		if (!(yPoints instanceof SimplePairList simpleYPoints))
			throw new AssertionError();
		if (!(zPoints instanceof SimplePairList simpleZPoints))
			throw new AssertionError();
		
		final BitSetVoxelSet bitSetVoxelSet = new BitSetVoxelSet(xPoints.size() - 1, yPoints.size() - 1, zPoints.size() - 1);
		bitSetVoxelSet.minX = Integer.MAX_VALUE;
		bitSetVoxelSet.minY = Integer.MAX_VALUE;
		bitSetVoxelSet.minZ = Integer.MAX_VALUE;
		bitSetVoxelSet.maxX = Integer.MIN_VALUE;
		bitSetVoxelSet.maxY = Integer.MIN_VALUE;
		bitSetVoxelSet.maxZ = Integer.MIN_VALUE;
		
		for (int xIndex = 0; xIndex < xPoints.size() - 1; xIndex++) {
			final int x1 = simpleXPoints.minValues[xIndex];
			final int x2 = simpleXPoints.maxValues[xIndex];
			
			var setX = false;
			
			for (int yIndex = 0; yIndex < yPoints.size() - 1; yIndex++) {
				final int y1 = simpleYPoints.minValues[yIndex];
				final int y2 = simpleYPoints.maxValues[yIndex];
				
				var setY = false;
				
				for (int zIndex = 0; zIndex < zPoints.size() - 1; zIndex++) {
					final int z1 = simpleZPoints.minValues[zIndex];
					final int z2 = simpleZPoints.maxValues[zIndex];
					
					if (function.apply(first.inBoundsAndContains(x1, y1, z1), second.inBoundsAndContains(x2, y2, z2))) {
						bitSetVoxelSet.storage.set(bitSetVoxelSet.getIndex(xIndex, yIndex, zIndex));
						bitSetVoxelSet.minZ = Math.min(bitSetVoxelSet.minZ, zIndex);
						bitSetVoxelSet.maxZ = Math.max(bitSetVoxelSet.maxZ, zIndex);
						
						setY = true;
					}
				}
				
				if (setY) {
					bitSetVoxelSet.minY = Math.min(bitSetVoxelSet.minY, yIndex);
					bitSetVoxelSet.maxY = Math.max(bitSetVoxelSet.maxY, yIndex);
					
					setX = true;
				}
			}
			
			if (setX) {
				bitSetVoxelSet.minX = Math.min(bitSetVoxelSet.minX, xIndex);
				bitSetVoxelSet.maxX = Math.max(bitSetVoxelSet.maxX, xIndex);
			}
		}
		
		bitSetVoxelSet.maxX++;
		bitSetVoxelSet.maxY++;
		bitSetVoxelSet.maxZ++;
		
		return bitSetVoxelSet;
		
//		int[] bounds = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
//		xPoints.forEachPair((x1, x2, xIndex) -> {
//			boolean[] bls = new boolean[]{false};
//			yPoints.forEachPair((y1, y2, yIndex) -> {
//				boolean[] bls2 = new boolean[]{false};
//				zPoints.forEachPair((z1, z2, zIndex) -> {
//					if (function.apply(first.inBoundsAndContains(x1, y1, z1), second.inBoundsAndContains(x2, y2, z2))) {
//						bitSetVoxelSet.storage.set(bitSetVoxelSet.getIndex(xIndex, yIndex, zIndex));
//						bounds[2] = Math.min(bounds[2], zIndex);
//						bounds[5] = Math.max(bounds[5], zIndex);
//						bls2[0] = true;
//					}
//					
//					return true;
//				});
//				if (bls2[0]) {
//					bounds[1] = Math.min(bounds[1], yIndex);
//					bounds[4] = Math.max(bounds[4], yIndex);
//					bls[0] = true;
//				}
//				
//				return true;
//			});
//			if (bls[0]) {
//				bounds[0] = Math.min(bounds[0], xIndex);
//				bounds[3] = Math.max(bounds[3], xIndex);
//			}
//			
//			return true;
//		});
//		bitSetVoxelSet.minX = bounds[0];
//		bitSetVoxelSet.minY = bounds[1];
//		bitSetVoxelSet.minZ = bounds[2];
//		bitSetVoxelSet.maxX = bounds[3] + 1;
//		bitSetVoxelSet.maxY = bounds[4] + 1;
//		bitSetVoxelSet.maxZ = bounds[5] + 1;
//		return bitSetVoxelSet;
	}
	
}
