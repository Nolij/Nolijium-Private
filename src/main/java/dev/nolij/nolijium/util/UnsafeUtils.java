package dev.nolij.nolijium.util;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.BitSet;

public class UnsafeUtils {
	private static final Unsafe UNSAFE;
	private static final MethodHandles.Lookup INTERNAL_LOOKUP;
	static {
		try {
			final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
			unsafeField.setAccessible(true);
			UNSAFE = (Unsafe) unsafeField.get(null);
			final Field hackfield = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
			@SuppressWarnings("deprecation") var fieldOffset = UNSAFE.staticFieldOffset(hackfield);
			@SuppressWarnings("deprecation") var fieldBase = UNSAFE.staticFieldBase(hackfield);
			INTERNAL_LOOKUP = (MethodHandles.Lookup) UNSAFE.getObject(fieldBase, fieldOffset);
		} catch (Throwable e) {
			throw new AssertionError(e);
		}
	}
	
	public static MethodHandles.Lookup implLookup() {
		return INTERNAL_LOOKUP;
	}
}
