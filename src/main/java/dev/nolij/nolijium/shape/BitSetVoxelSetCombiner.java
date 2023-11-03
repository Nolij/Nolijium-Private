package dev.nolij.nolijium.shape;

import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.shape.BitSetVoxelSet;
import net.minecraft.util.shape.PairList;
import net.minecraft.util.shape.SimplePairList;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.BitSet;

public class BitSetVoxelSetCombiner {
    private final SimplePairList xPoints, yPoints, zPoints;
    private final BooleanBiFunction function;
    private final BitSetVoxelSet first, second;
    private final BitSetVoxelSet destination;
    private int destinationIndex;
    private final long[] destinationBits;
    private final long[] firstWords, secondWords;

    public BitSetVoxelSetCombiner(BitSetVoxelSet first, BitSetVoxelSet second, PairList xPoints, PairList yPoints, PairList zPoints, BooleanBiFunction function) {
        this.first = first;
        this.second = second;
        this.xPoints = (SimplePairList) xPoints;
        this.yPoints = (SimplePairList) yPoints;
        this.zPoints = (SimplePairList) zPoints;
        this.function = function;
        this.firstWords = getBitSetWords(this.first.storage);
        this.secondWords = getBitSetWords(this.second.storage);
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

    private static final MethodHandle WORDS;
    private static final Unsafe UNSAFE;

    static {
        try {
            var unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            UNSAFE = (Unsafe)unsafeField.get(null);
            var hackfield = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            long fieldOffset = UNSAFE.staticFieldOffset(hackfield);
            Object fieldBase = UNSAFE.staticFieldBase(hackfield);
            MethodHandles.Lookup hack = (MethodHandles.Lookup)UNSAFE.getObject(fieldBase, fieldOffset);
            Field f = BitSet.class.getDeclaredField("words");
            WORDS = hack.unreflectGetter(f);
        } catch(Throwable e) {
            throw new AssertionError(e);
        }
    }

    private static long[] getBitSetWords(BitSet storage) {
        try {
            return (long[])WORDS.invokeExact(storage);
        } catch(Throwable e) {
            throw new AssertionError(e);
        }
    }

    private boolean processY(int x1, int x2) {
        var changed = false;

        for (int yIndex = 0; yIndex < yPoints.size() - 1; yIndex++) {
            final int y1 = yPoints.minValues[yIndex];
            final int y2 = yPoints.maxValues[yIndex];

            if (processZ(x1, x2, y1, y2)) {
                destination.minY = Math.min(destination.minY, yIndex);
                destination.maxY = Math.max(destination.maxY, yIndex);

                changed = true;
            }
        }
        return changed;
    }

    private boolean processZ(int x1, int x2, int y1, int y2) {
        var setY = false;

        for (int zIndex = 0; zIndex < zPoints.size() - 1; zIndex++) {
            final int z1 = zPoints.minValues[zIndex];
            final int z2 = zPoints.maxValues[zIndex];

            if (function.apply(first.inBoundsAndContains(x1, y1, z1), second.inBoundsAndContains(x2, y2, z2))) {
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
        for (int xIndex = 0; xIndex < xPoints.size() - 1; xIndex++) {
            final int x1 = xPoints.minValues[xIndex];
            final int x2 = xPoints.maxValues[xIndex];

            if (processY(x1, x2)) {
                destination.minX = Math.min(destination.minX, xIndex);
                destination.maxX = Math.max(destination.maxX, xIndex);
            }
        }

        destination.maxX++;
        destination.maxY++;
        destination.maxZ++;

        return destination;
    }
}