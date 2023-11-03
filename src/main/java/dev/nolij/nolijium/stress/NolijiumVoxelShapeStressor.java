package dev.nolij.nolijium.stress;

import net.minecraft.block.Block;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public final class NolijiumVoxelShapeStressor {
	
	private static int getIndex(int top, int bottom, int front, int back, int left, int right, boolean rotateVertical, boolean rotateHorizontal) {
		return ((((((top | bottom << 1) | front << 2) | back << 3) | left << 4) | right << 5) | (rotateVertical ? 1 : 0) << 6) | (rotateHorizontal ? 1 : 0) << 7;
	}
	
	public static void stress() {
		@SuppressWarnings("MismatchedReadAndWriteOfArray")
		final VoxelShape[] bounds = new VoxelShape[256];
		
		VoxelShape frame = VoxelShapeUtils.combine(
			Block.createCuboidShape(0, 0, 0, 3, 3, 16),
			Block.createCuboidShape(0, 3, 0, 3, 16, 3),
			Block.createCuboidShape(0, 3, 13, 3, 16, 16),
			Block.createCuboidShape(0, 13, 3, 3, 16, 13),
			Block.createCuboidShape(3, 0, 0, 16, 3, 3),
			Block.createCuboidShape(3, 0, 13, 16, 3, 16),
			Block.createCuboidShape(3, 13, 0, 16, 16, 3),
			Block.createCuboidShape(3, 13, 13, 16, 16, 16),
			Block.createCuboidShape(13, 0, 3, 16, 3, 13),
			Block.createCuboidShape(13, 3, 0, 16, 13, 3),
			Block.createCuboidShape(13, 3, 13, 16, 13, 16),
			Block.createCuboidShape(13, 13, 3, 16, 16, 13),
			Block.createCuboidShape(12.5, 14.9, 7.5, 13.5, 15.9, 8.5),//ledTop1
			Block.createCuboidShape(2.5, 14.9, 7.5, 3.5, 15.9, 8.5),//ledTop2
			Block.createCuboidShape(12.5, 7.5, 0.1, 13.5, 8.5, 1.1),//ledBack1
			Block.createCuboidShape(2.5, 7.5, 0.1, 3.5, 8.5, 1.1),//ledBack2
			Block.createCuboidShape(2.5, 0.1, 7.5, 3.5, 1.1, 8.5),//ledBottom2
			Block.createCuboidShape(12.5, 0.1, 7.5, 13.5, 1.1, 8.5),//ledBottom1
			Block.createCuboidShape(12.5, 7.5, 14.9, 13.5, 8.5, 15.9),//ledFront1
			Block.createCuboidShape(2.5, 7.5, 14.9, 3.5, 8.5, 15.9),//ledFront2
			Block.createCuboidShape(0.1, 7.5, 2.5, 1.1, 8.5, 3.5),//ledRight2
			Block.createCuboidShape(0.1, 7.5, 12.5, 1.1, 8.5, 13.5),//ledRight1
			Block.createCuboidShape(14.9, 7.5, 2.5, 15.9, 8.5, 3.5),//ledLeft1
			Block.createCuboidShape(14.9, 7.5, 12.5, 15.9, 8.5, 13.5)//ledLeft2
		);
		VoxelShape frontPanel = VoxelShapeUtils.combine(
			Block.createCuboidShape(3, 5, 14, 13, 11, 15),//connectorFrontToggle
			Block.createCuboidShape(4, 4, 15, 12, 12, 16)//portFrontToggle
		);
		VoxelShape rightPanel = VoxelShapeUtils.combine(
			Block.createCuboidShape(1, 5, 3, 2, 11, 13),//connectorRightToggle
			Block.createCuboidShape(0, 4, 4, 1, 12, 12)//portRightToggle
		);
		VoxelShape leftPanel = VoxelShapeUtils.combine(
			Block.createCuboidShape(14, 5, 3, 15, 11, 13),//connectorLeftToggle
			Block.createCuboidShape(15, 4, 4, 16, 12, 12)//portLeftToggle
		);
		VoxelShape backPanel = VoxelShapeUtils.combine(
			Block.createCuboidShape(3, 5, 1, 13, 11, 2),//connectorBackToggle
			Block.createCuboidShape(4, 4, 0, 12, 12, 1)//portBackToggle
		);
		VoxelShape topPanel = VoxelShapeUtils.combine(
			Block.createCuboidShape(3, 14, 5, 13, 15, 11),//connectorTopToggle
			Block.createCuboidShape(4, 15, 4, 12, 16, 12)//portTopToggle
		);
		VoxelShape bottomPanel = VoxelShapeUtils.combine(
			Block.createCuboidShape(3, 1, 5, 13, 2, 11),//connectorBottomToggle
			Block.createCuboidShape(4, 0, 4, 12, 1, 12)//portBottomToggle
		);
		VoxelShape frameRotated = VoxelShapeUtils.rotate(frame, BlockRotation.CLOCKWISE_90);
		VoxelShape topRotated = VoxelShapeUtils.rotate(topPanel, BlockRotation.CLOCKWISE_90);
		VoxelShape bottomRotated = VoxelShapeUtils.rotate(bottomPanel, BlockRotation.CLOCKWISE_90);
		VoxelShape frameRotatedAlt = VoxelShapeUtils.rotate(frame, Direction.NORTH);
		VoxelShape rightRotated = VoxelShapeUtils.rotate(rightPanel, Direction.NORTH);
		VoxelShape leftRotated = VoxelShapeUtils.rotate(leftPanel, Direction.NORTH);
		for (int rotated = 0; rotated < 3; rotated++) {
			//If we don't need to rotate anything, this is zero
			// If we need to rotate the top and bottom frames, this is one
			// If we need to rotate the left and right frames, this is two
			boolean rotateVertical = rotated == 1;
			boolean rotateHorizontal = rotated == 2;
			VoxelShape baseFrame = rotateVertical ? frameRotated : rotateHorizontal ? frameRotatedAlt : frame;
			for (int top = 0; top < 2; top++) {
				VoxelShape withTop = top == 0 ? baseFrame : VoxelShapes.combine(baseFrame, rotateVertical ? topRotated : topPanel, BooleanBiFunction.OR);
				for (int bottom = 0; bottom < 2; bottom++) {
					VoxelShape withBottom = bottom == 0 ? withTop : VoxelShapes.combine(withTop, rotateVertical ? bottomRotated : bottomPanel, BooleanBiFunction.OR);
					for (int front = 0; front < 2; front++) {
						VoxelShape withFront = front == 0 ? withBottom : VoxelShapes.combine(withBottom, frontPanel, BooleanBiFunction.OR);
						for (int back = 0; back < 2; back++) {
							VoxelShape withBack = back == 0 ? withFront : VoxelShapes.combine(withFront, backPanel, BooleanBiFunction.OR);
							for (int left = 0; left < 2; left++) {
								VoxelShape withLeft = left == 0 ? withBack : VoxelShapes.combine(withBack, rotateHorizontal ? leftRotated : leftPanel, BooleanBiFunction.OR);
								for (int right = 0; right < 2; right++) {
									VoxelShape withRight = right == 0 ? withLeft : VoxelShapes.combine(withLeft, rotateHorizontal ? rightRotated : rightPanel, BooleanBiFunction.OR);
									bounds[getIndex(top, bottom, front, back, left, right, rotateVertical, rotateHorizontal)] = withRight;
								}
							}
						}
					}
				}
			}
		}
	}
	
}
