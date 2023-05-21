package com.example.clientwithui.camellia;

public class CamelliaSBlocks {
    private static final int NUMBER_BIT_IN_BYTE = 8;
    private static final int NUMBER_BIT_IN_INT = 32;

    // размер матрицы S -- 16x16
    public static final short[][] SBOX1 =  {{112, 130, 44, 236, 179, 39, 192, 229, 228, 133, 87, 53, 234, 12, 174, 65},
            {35, 239, 107, 147, 69, 25, 165, 33, 237, 14, 79, 78, 29, 101, 146, 189},
            {134, 184, 175, 143, 124, 235, 31, 206, 62, 48, 220, 95, 94, 197, 11, 26},
            {166, 225, 57, 202, 213, 71, 93, 61, 217, 1, 90, 214, 81, 86, 108, 77},
            {139, 13, 154, 102, 251, 204, 176, 45, 116, 18, 43, 32, 240, 177, 132, 153},
            {223, 76, 203, 194, 52, 126, 118, 5, 109, 183, 169, 49, 209, 23, 4, 215},
            {20, 88, 58, 97, 222, 27, 17, 28, 50, 15,156, 22, 83, 24, 242, 34},
            {254, 68, 207, 178, 195, 181, 122, 145, 36, 8, 232, 168, 96, 252, 105, 80},
            {170, 208, 160, 125, 161, 137, 98, 151, 84, 91, 30, 149, 224, 255, 100, 210},
            {16, 196, 0, 72, 163, 247, 117, 219, 138, 3, 230, 218, 9, 63, 221, 148},
            {135, 92, 131, 2, 205, 74, 144, 51, 115, 103, 246, 243, 157, 127, 191, 226},
            {82, 155, 216, 38, 200, 55, 198, 59, 129, 150, 111, 75, 19, 190, 99, 46},
            {233, 121, 167, 140, 159, 110, 188, 142, 41, 245, 249, 182, 47, 253, 180, 89},
            {120, 152, 6, 106, 231, 70, 113, 186, 212, 37, 171, 66, 136, 162, 141, 250},
            {114, 7, 185, 85, 248, 238, 172, 10, 54, 73, 42, 104, 60, 56, 241, 164},
            {64, 40, 211, 123, 187, 201, 67, 193, 21, 227, 173, 244, 119, 199, 128, 158}
    };

    public static byte getSBox1(byte value)
    {
        // первые четыре бита это строка
        // вторые четыре бита это столбец
        int row = (value >> 4) & 0x0F;
        int column = value & 0x0F;
        return (byte)(SBOX1[row][column] & 0xFF);
    }

    public static byte getSBox2(byte value)
    {
        byte valueFromSBox1 = getSBox1(value);
        return cycleShift(valueFromSBox1, 1);
    }

    public static byte getSBox3(byte value)
    {
        byte valueFromSBox1 = getSBox1(value);
        return cycleShift(valueFromSBox1,7);
    }

    public static byte getSBox4(byte value)
    {
        int shiftX = cycleShift(value, 1);
        return getSBox1((byte) (shiftX & 0xFF));
    }

    private static byte cycleShift(byte value, int shift)
    {
        int temp = (value & 0xFF) >> (NUMBER_BIT_IN_BYTE - shift);
        return (byte) ((value << shift) | temp);
    }
}
