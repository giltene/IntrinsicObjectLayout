/*
 * Written by Gil Tene and Martin Thompson, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 */

package org.ObjectLayout;

/**
 * This class contains the intrinsifiable portions of PrimitiveCharArray behavior. JDK implementations
 * that choose to intrinsify PrimitiveCharArray are expected to replace the implementation of this
 * base class.
 */

abstract class AbstractPrimitiveCharArray extends AbstractPrimitiveArray {

    private final char[][] longAddressableElements; // Used to store elements at indexes above Integer.MAX_VALUE
    private final char[] intAddressableElements;

    final char[] _asArray() {
        if (_getLength() > Integer.MAX_VALUE) {
            throw new IllegalStateException(
                    "Cannot make char[] from array with more than Integer.MAX_VALUE elements (" +
                            _getLength() + ")");
        }
        // TODO replace with direct access logic, along the lines of:
        // return (char[]) deriveContainedObjectAtOffset(this, getInstanceSize(this.class));
        // (This assumes that *if* length fits in an int, a valid int-indexable array header
        // is placed immediately after the instance body...)
        //
        // getInstanceSize(this.class) can hopefully be optimized either statically (e.g. in
        // CHA-like form by proving that the type known at the call site is a lead class), or
        // dynamically (e.g. in inline-cache form by checking the class against a cached type,
        // and using the cache size associated with that type in the fastpath).
        return intAddressableElements;
    }

    char _get(final int index) {
        // TODO replace with direct access logic:
        return intAddressableElements[index];
    }

    char _get(final long index) {
        // TODO replace with direct access logic:
        if (index < Integer.MAX_VALUE) {
            return _get((int) index);
        }

        // Calculate index into long-addressable-only partitions:
        final long longIndex = (index - Integer.MAX_VALUE);
        final int partitionIndex = (int)(longIndex >>> MAX_EXTRA_PARTITION_SIZE_POW2_EXPONENT);
        final int partitionOffset = (int)longIndex & PARTITION_MASK;

        return longAddressableElements[partitionIndex][partitionOffset];
    }

    void _set(final int index, final char value) {
        // TODO replace with direct access logic:
        intAddressableElements[index] = value;
    }

    void _set(final long index, final char value) {
        // TODO replace with direct access logic:
        if (index < Integer.MAX_VALUE) {
            _set((int) index, value);
        }

        // Calculate index into long-addressable-only partitions:
        final long longIndex = (index - Integer.MAX_VALUE);
        final int partitionIndex = (int)(longIndex >>> MAX_EXTRA_PARTITION_SIZE_POW2_EXPONENT);
        final int partitionOffset = (int)longIndex & PARTITION_MASK;

        longAddressableElements[partitionIndex][partitionOffset] = value;
    }

    AbstractPrimitiveCharArray() {
        // TODO: replace new with constructObjectAtOffset() call:
        intAddressableElements = (char[]) createIntAddressableElements(char.class);
        longAddressableElements = (char[][]) createLongAddressableElements(char.class);
    }

    AbstractPrimitiveCharArray(AbstractPrimitiveCharArray sourceArray) {
        // TODO: replace clone() call with constructObjectAtOffset() call, followed by call to copy contents:
        intAddressableElements = sourceArray.intAddressableElements.clone();
        int numLongAddressablePartitions = sourceArray.longAddressableElements.length;
        longAddressableElements = new char[numLongAddressablePartitions][];
        for (int i = 0; i < numLongAddressablePartitions; i++) {
            longAddressableElements[i] = sourceArray.longAddressableElements[i].clone();
        }
    }
}
