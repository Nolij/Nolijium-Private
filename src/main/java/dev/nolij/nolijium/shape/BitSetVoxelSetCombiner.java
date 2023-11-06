package dev.nolij.nolijium.shape;

import dev.nolij.nolijium.util.UnsafeUtils;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.shape.BitSetVoxelSet;
import net.minecraft.util.shape.PairList;
import net.minecraft.util.shape.SimplePairList;

import java.lang.invoke.MethodHandle;
import java.util.BitSet;

public class BitSetVoxelSetCombiner {
    private final NolijPairList xPoints, yPoints, zPoints;
    private final BooleanBiFunction function;
	private final BitSetVoxelSet destination;
    private final long[] destinationBits;
    private final BitSetVoxelWrapper firstWrapper, secondWrapper;
    
    public BitSetVoxelSetCombiner(BitSetVoxelSet first, BitSetVoxelSet second, PairList xPoints, PairList yPoints, PairList zPoints, BooleanBiFunction function) {
	    this.xPoints = new NolijPairList(xPoints);
        this.yPoints = new NolijPairList(yPoints);
        this.zPoints = new NolijPairList(zPoints);
        this.function = function;
        this.firstWrapper = new BitSetVoxelWrapper(first);
        this.secondWrapper = new BitSetVoxelWrapper(second);
        final BitSetVoxelSet bitSetVoxelSet = new BitSetVoxelSet(xPoints.size() - 1, yPoints.size() - 1, zPoints.size() - 1);
        this.destinationBits = getBitSetWords(bitSetVoxelSet.storage);
        bitSetVoxelSet.minX = Integer.MAX_VALUE;
        bitSetVoxelSet.minY = Integer.MAX_VALUE;
        bitSetVoxelSet.minZ = Integer.MAX_VALUE;
        bitSetVoxelSet.maxX = Integer.MIN_VALUE;
        bitSetVoxelSet.maxY = Integer.MIN_VALUE;
        bitSetVoxelSet.maxZ = Integer.MIN_VALUE;
        this.destination = bitSetVoxelSet;
    }
	
	static class BitSetVoxelWrapper {
		private final long[] words;
		private final int sizeX, sizeY, sizeZ;
		
		BitSetVoxelWrapper(long[] words, int sizeX, int sizeY, int sizeZ) {
			this.words = words;
			this.sizeX = sizeX;
			this.sizeY = sizeY;
			this.sizeZ = sizeZ;
		}
		
		BitSetVoxelWrapper(BitSetVoxelSet shape) {
			this(getBitSetWords(shape.storage), shape.sizeX, shape.sizeY, shape.sizeZ);
		}
		
		boolean inBoundsAndContains(int x, int y, int z) {
			if (x < 0 || y < 0 || z < 0 ||
				x >= sizeX || y >= sizeY || z >= sizeZ)
				return false;
			
			int idx = (x * sizeY + y) * sizeZ + z;
			
			return (words[idx >> 6] & (1L << idx)) != 0;
		}
	}
	
	static class NolijPairList {
		final int size;
		final int[] minValues;
		final int[] maxValues;
		
		NolijPairList(PairList list) {
			size = list.size();
			if (list instanceof SimplePairList simpleList) {
				minValues = simpleList.minValues;
				maxValues = simpleList.maxValues;
			} else {
				minValues = new int[size + 1];
				maxValues = new int[size + 1];
				list.forEachPair(new PairList.Consumer() {
					int i = 0;
					@Override
					public boolean merge(int x, int y, int index) {
						minValues[i] = x;
						maxValues[i] = y;
						i++;
						return true;
					}
				});
			}
		}
	}
    
    private static final MethodHandle WORDS, WORDS_IN_USE, RECALCULATE_WORDS;
    
    static {
        try {
            WORDS = UnsafeUtils.implLookup().unreflectGetter(BitSet.class.getDeclaredField("words"));
            WORDS_IN_USE = UnsafeUtils.implLookup().unreflectSetter(BitSet.class.getDeclaredField("wordsInUse"));
            RECALCULATE_WORDS = UnsafeUtils.implLookup().unreflect(BitSet.class.getDeclaredMethod("recalculateWordsInUse"));
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }
    
    private static long[] getBitSetWords(BitSet storage) {
        try {
            return (long[]) WORDS.invokeExact(storage);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }
    
    private static void finalizeShape(BitSet storage, long[] backingArray) {
        try {
            WORDS_IN_USE.invokeExact(storage, backingArray.length);
            RECALCULATE_WORDS.invokeExact(storage);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }
    
    private boolean processY(int x1, int x2) {
        var changed = false;
		
		var maxIndexExclusive = yPoints.size - 1;
		var minValues = yPoints.minValues;
		var maxValues = yPoints.maxValues;
        
        for (int yIndex = 0; yIndex < maxIndexExclusive; yIndex++) {
            final int y1 = minValues[yIndex];
            final int y2 = maxValues[yIndex];
            
            if (processZ(x1, x2, y1, y2)) {
                destination.minY = Math.min(destination.minY, yIndex);
                destination.maxY = Math.max(destination.maxY, yIndex);
                
                changed = true;
            }
        }
        return changed;
    }
	
	private int destinationIndex = 0;
    private boolean processZ(int x1, int x2, int y1, int y2) {
        var setY = false;
	    
	    var maxIndexExclusive = zPoints.size - 1;
	    var minValues = zPoints.minValues;
	    var maxValues = zPoints.maxValues;
		
		var function = this.function;
		var isOr = function == BooleanBiFunction.OR;
        
        for (int zIndex = 0; zIndex < maxIndexExclusive; zIndex++) {
            final int z1 = minValues[zIndex];
            final int z2 = maxValues[zIndex];
			
			final boolean conditionOutput;
			
			if (isOr) {
				conditionOutput =
					firstWrapper.inBoundsAndContains(x1, y1, z1) || secondWrapper.inBoundsAndContains(x2, y2, z2);
			} else 
				conditionOutput = function.apply(firstWrapper.inBoundsAndContains(x1, y1, z1),
					secondWrapper.inBoundsAndContains(x2, y2, z2));
            
            if (conditionOutput) {
                //destination.storage.set(destinationIndex);
                destinationBits[(destinationIndex >> 6)] |= (1L << destinationIndex);
                //destination.storage.set(destinationIndex);
                //destination.getIndex(xIndex, yIndex, zIndex));
                destination.minZ = Math.min(destination.minZ, zIndex);
                destination.maxZ = Math.max(destination.maxZ, zIndex);
                
                setY = true;
            }
            
            destinationIndex++;
        }
        
        return setY;
    }
    
    public BitSetVoxelSet getResult() {
	    var maxIndexExclusive = xPoints.size - 1;
	    var minValues = xPoints.minValues;
	    var maxValues = xPoints.maxValues;
		
        for (int xIndex = 0; xIndex < maxIndexExclusive; xIndex++) {
            final int x1 = minValues[xIndex];
            final int x2 = maxValues[xIndex];
            
            if (processY(x1, x2)) {
                destination.minX = Math.min(destination.minX, xIndex);
                destination.maxX = Math.max(destination.maxX, xIndex);
            }
        }
        
        destination.maxX++;
        destination.maxY++;
        destination.maxZ++;
        
        finalizeShape(destination.storage, this.destinationBits);
        
        return destination;
    }
}
