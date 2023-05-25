package org.example.camellia;

public class CamelliaFunction {
    private static final int NUMBER_VALUE_T_AND_Y = 8;
    private static final int NUMBER_BIT_IN_INT = 32;

    public static long F(long F_IN, long KE) {
        long x = F_IN ^ KE;
        long F_OUT = 0L;
        byte[] t = new byte[NUMBER_VALUE_T_AND_Y];
        byte[] y = new byte[NUMBER_VALUE_T_AND_Y];

        t[0] = (byte) ((x >> 56) & Camellia.MASK8);
        t[1] = (byte) ((x >> 48) & Camellia.MASK8);
        t[2] = (byte) ((x >> 40) & Camellia.MASK8);
        t[3] = (byte) ((x >> 32) & Camellia.MASK8);
        t[4] = (byte) ((x >> 24) & Camellia.MASK8);
        t[5] = (byte) ((x >> 16) & Camellia.MASK8);
        t[6] = (byte) ((x >>  8) & Camellia.MASK8);
        t[7] = (byte) (x         & Camellia.MASK8);

        t[0] = CamelliaSBlocks.getSBox1(t[0]);
        t[1] = CamelliaSBlocks.getSBox2(t[1]);
        t[2] = CamelliaSBlocks.getSBox3(t[2]);
        t[3] = CamelliaSBlocks.getSBox4(t[3]);
        t[4] = CamelliaSBlocks.getSBox2(t[4]);
        t[5] = CamelliaSBlocks.getSBox3(t[5]);
        t[6] = CamelliaSBlocks.getSBox4(t[6]);
        t[7] = CamelliaSBlocks.getSBox1(t[7]);

        y[0] = (byte) ((t[0] ^ t[2] ^ t[3] ^ t[5] ^ t[6] ^ t[7]) & Camellia.MASK8);
        y[1] = (byte) ((t[0] ^ t[1] ^ t[3] ^ t[4] ^ t[6] ^ t[7]) & Camellia.MASK8);
        y[2] = (byte) ((t[0] ^ t[1] ^ t[2] ^ t[4] ^ t[5] ^ t[7]) & Camellia.MASK8);
        y[3] = (byte) ((t[1] ^ t[2] ^ t[3] ^ t[4] ^ t[5] ^ t[6]) & Camellia.MASK8);
        y[4] = (byte) ((t[0] ^ t[1] ^ t[5] ^ t[6] ^ t[7]) & Camellia.MASK8);
        y[5] = (byte) ((t[1] ^ t[2] ^ t[4] ^ t[6] ^ t[7]) & Camellia.MASK8);
        y[6] = (byte) ((t[2] ^ t[3] ^ t[4] ^ t[5] ^ t[7]) & Camellia.MASK8);
        y[7] = (byte) ((t[0] ^ t[3] ^ t[4] ^ t[5] ^ t[6]) & Camellia.MASK8);

        for (int i = 0; i < NUMBER_VALUE_T_AND_Y; i++)
        {
            F_OUT <<= 8;
            F_OUT |= y[i] & Camellia.MASK8;
        }
        return F_OUT;
    }

    public static long FL(long FL_IN, long KE)
    {
//        1 0011111111110010000000000101001 11100000001110100010001100100111

//                       x1                                  x2
//        10011111111110010000000000101001 11100000001110100010001100100111
//        0 1001110100000100011101110011100 00110110011011110100001101100001
//                       k1                                  k2
//          1001110100000100011101110011100 00110110011011110100001101100001
//        00000110000000100000001100000000
//        110011110000011101101011011001

        int x1, x2, k1, k2;
        x1 = (int) ((FL_IN >> NUMBER_BIT_IN_INT) & 0xffffffffL);
        x2 = (int) (FL_IN & 0xffffffffL);

        k1 = (int) (KE >>> NUMBER_BIT_IN_INT);
        k2 = (int) (KE & 0xffffffffL);

        x2 = x2 ^ (cycleShift((x1 & k1), 1));
        x1 = x1 ^ (x2 | k2);
        long bla = ((long) x1) << NUMBER_BIT_IN_INT;
        bla = bla | (long)x2 & 0xFFFFFFFFL;
        return bla;
    }

    // & -> 00001110100000000000000000001000
    // <<?  00011101000000000000000000010000
    // x2 = 11111101001110100010001100110111
    // x1 = 1100000100001100110001101011110
//          1100000100001100110001101011110 00000000000000000000000000000000
//          1100000100001100110001101011110 11111101001110100010001100110111
//     x1 = 1100000100001100110001101011110 11111101001110100010001100110111

    public static long FL_INV(long FL_INV, long KE)
    {
        int y1, y2, k1, k2;
        y1 =  (int) (FL_INV >>> NUMBER_BIT_IN_INT);
        y2 = (int) (FL_INV & 0xffffffffL);

        k1 = (int) (KE >>> NUMBER_BIT_IN_INT);
        k2 = (int) (KE & 0xffffffffL);

        y1 = y1 ^ (y2 | k2);
        y2 = y2 ^ (cycleShift((y1 & k1), 1));
        long bla = ((long) y1) << NUMBER_BIT_IN_INT;
        bla = bla | (long)y2 & 0xFFFFFFFFL;
        return bla;
    }

    public static int cycleShift(int value, int shift)
    {
//        int temp = ((value) >>> (NUMBER_BIT_IN_INT - shift));
//        return ((value << shift) | temp);
        return Integer.rotateLeft(value, shift);
//        return  (value >>> shift) | (value << (NUMBER_BIT_IN_INT - shift));
    }
}
