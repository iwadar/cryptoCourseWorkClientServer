package com.example.clientwithui.camellia;

import static com.example.clientwithui.HelpFunction.*;

public class Camellia implements ISymmetricalCipher {
    public static final long MASK8 = 0xffL;
    private final CamelliaKey key;
    protected static final long[] c = { 0xA09E667F3BCC908BL,
                                        0xB67AE8584CAA73B2L,
                                        0xC6EF372FE94F82BEL,
                                        0x54FF53A5F1D36F1CL,
                                        0x10E527FADE682D1DL,
                                        0xB05688C2B3E6C1FDL};
    public Camellia(CamelliaKey k)
    {
        this.key = k;
    }

    @Override
    public byte[] encrypt(byte[] text)
    {
        if (text.length != 16)
        {
            return text;
        }
        long[] Ds = getLongFrom128Byte(text);
        var encrypt = encrypt(Ds[0], Ds[1]);
        return twoLongToOneByteArray(encrypt);
    }

    @Override
    public byte[] decrypt(byte[] text)
    {
        if (text.length != 16)
        {
            return text;
        }
        long[] Ds = getLongFrom128Byte(text);
        var decrypt = decrypt(Ds[0], Ds[1]);
        return twoLongToOneByteArray(decrypt);
    }

    private long[] encrypt(long D1, long D2)
    {
        D1 = D1 ^ key.kw1;           // Предварительное забеливание
        D2 = D2 ^ key.kw2;
        D2 = D2 ^ CamelliaFunction.F(D1, key.k1);
        D1 = D1 ^ CamelliaFunction.F(D2, key.k2)    ;
        D2 = D2 ^ CamelliaFunction.F(D1, key.k3);
        D1 = D1 ^ CamelliaFunction.F(D2, key.k4);
        D2 = D2 ^ CamelliaFunction.F(D1, key.k5);
        D1 = D1 ^ CamelliaFunction.F(D2, key.k6);
        D1 = CamelliaFunction.FL(D1, key.ke1);         // FL
        D2 = CamelliaFunction.FL_INV(D2, key.ke2);     // FL_INV
        D2 = D2 ^ CamelliaFunction.F(D1, key.k7);
        D1 = D1 ^ CamelliaFunction.F(D2, key.k8);
        D2 = D2 ^ CamelliaFunction.F(D1, key.k9);
        D1 = D1 ^ CamelliaFunction.F(D2, key.k10);
        D2 = D2 ^ CamelliaFunction.F(D1, key.k11);
        D1 = D1 ^ CamelliaFunction.F(D2, key.k12);
        D1 = CamelliaFunction.FL(D1, key.ke3);         // FL
        D2 = CamelliaFunction.FL_INV(D2, key.ke4);     // FL_INV
        D2 = D2 ^ CamelliaFunction.F(D1, key.k13);
        D1 = D1 ^ CamelliaFunction.F(D2, key.k14);
        D2 = D2 ^ CamelliaFunction.F(D1, key.k15);
        D1 = D1 ^ CamelliaFunction.F(D2, key.k16);
        D2 = D2 ^ CamelliaFunction.F(D1, key.k17);
        D1 = D1 ^ CamelliaFunction.F(D2, key.k18);
        if (!key.getFlagSizeKey())
        {
            D1 = CamelliaFunction.FL(D1, key.ke5);     // FL
            D2 = CamelliaFunction.FL_INV(D2, key.ke6);     // FL_INV
            D2 = D2 ^ CamelliaFunction.F(D1, key.k19);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k20);
            D2 = D2 ^ CamelliaFunction.F(D1, key.k21);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k22);
            D2 = D2 ^ CamelliaFunction.F(D1, key.k23);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k24);
        }
        D2 = D2 ^ key.kw3;           // Финальное забеливание
        D1 = D1 ^ key.kw4;
        long[] C = {D2, D1};
        return C;
    }

    private long[] decrypt(long D1, long D2)
    {
        D1 = D1 ^ key.kw3;           // Предварительное забеливание
        D2 = D2 ^ key.kw4;
        if (key.getFlagSizeKey())
        {
            D2 = D2 ^ CamelliaFunction.F(D1, key.k18);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k17);
            D2 = D2 ^ CamelliaFunction.F(D1, key.k16);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k15);
            D2 = D2 ^ CamelliaFunction.F(D1, key.k14);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k13);
            D1 = CamelliaFunction.FL(D1, key.ke4);         // FL
            D2 = CamelliaFunction.FL_INV(D2, key.ke3);     // FL_INV
            D2 = D2 ^ CamelliaFunction.F(D1, key.k12);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k11);
            D2 = D2 ^ CamelliaFunction.F(D1, key.k10);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k9);
            D2 = D2 ^ CamelliaFunction.F(D1, key.k8);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k7);
            D1 = CamelliaFunction.FL(D1, key.ke2);         // FL
            D2 = CamelliaFunction.FL_INV(D2, key.ke1);     // FL_INV
            D2 = D2 ^ CamelliaFunction.F(D1, key.k6);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k5);
            D2 = D2 ^ CamelliaFunction.F(D1, key.k4);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k3);
            D2 = D2 ^ CamelliaFunction.F(D1, key.k2);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k1);
        }
        else
        {
            D2 = D2 ^ CamelliaFunction.F(D1, key.k24);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k23);
            D2 = D2 ^ CamelliaFunction.F(D1, key.k22);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k21);
            D2 = D2 ^ CamelliaFunction.F(D1, key.k20);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k19);
            D1 = CamelliaFunction.FL(D1, key.ke6);         // FL
            D2 = CamelliaFunction.FL_INV(D2, key.ke5);     // FL_INV
            D2 = D2 ^ CamelliaFunction.F(D1, key.k18);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k17);
            D2 = D2 ^ CamelliaFunction.F(D1, key.k16);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k15);
            D2 = D2 ^ CamelliaFunction.F(D1, key.k14);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k13);
            D1 = CamelliaFunction.FL(D1, key.ke4);         // FL
            D2 = CamelliaFunction.FL_INV(D2, key.ke3);     // FL_INV
            D2 = D2 ^ CamelliaFunction.F(D1, key.k12);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k11);
            D2 = D2 ^ CamelliaFunction.F(D1, key.k10);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k9);
            D2 = D2 ^ CamelliaFunction.F(D1, key.k8);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k7);
            D1 = CamelliaFunction.FL(D1, key.ke2);     // FL
            D2 = CamelliaFunction.FL_INV(D2, key.ke1);     // FL_INV
            D2 = D2 ^ CamelliaFunction.F(D1, key.k6);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k5);
            D2 = D2 ^ CamelliaFunction.F(D1, key.k4);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k3);
            D2 = D2 ^ CamelliaFunction.F(D1, key.k2);
            D1 = D1 ^ CamelliaFunction.F(D2, key.k1);
        }
        D2 = D2 ^ key.kw1;           // Финальное забеливание
        D1 = D1 ^ key.kw2;
        long[] M = {D2, D1};
        return M;
    }
}
