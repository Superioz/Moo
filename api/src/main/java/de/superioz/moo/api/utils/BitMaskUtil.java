package de.superioz.moo.api.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BitMaskUtil {

    public static int getFlag(int pow) {
        return (int) Math.pow(2, pow);
    }

    /**
     * Creates a bit mask with given flags
     *
     * @param flags The flags
     * @return The mask
     */
    public static byte create(byte... flags) {
        return add((byte) 0, flags);
    }

    /**
     * Adds flags to given mask
     *
     * @param mask  The mask
     * @param flags The flags
     * @return The result
     */
    public static byte add(byte mask, byte... flags) {
        byte newMask = mask;
        for(byte b : flags) {
            newMask |= b;
        }
        return newMask;
    }

    public static int add(int mask, int... flags) {
        int newMask = mask;
        for(int i : flags) {
            newMask |= i;
        }
        return newMask;
    }

    /**
     * Removes flags from given mask
     *
     * @param mask  The mask
     * @param flags The flags
     * @return The result
     */
    public static byte remove(byte mask, byte... flags) {
        byte newMask = mask;
        for(byte b : flags) {
            newMask &= ~b;
        }
        return newMask;
    }

    public static int remove(int mask, int... flags) {
        int newMask = mask;
        for(int i : flags) {
            newMask &= ~i;
        }
        return newMask;
    }

    /**
     * Checks if given mask contains flag
     *
     * @param mask The mask
     * @param flag The flag
     * @return The result
     */
    public static boolean contains(byte mask, byte flag) {
        return (mask & flag) == flag;
    }

    public static boolean contains(int mask, int flag) {
        return (mask & flag) == flag;
    }

}
