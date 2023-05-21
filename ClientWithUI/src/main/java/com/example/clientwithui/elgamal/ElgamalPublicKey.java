package com.example.clientwithui.elgamal;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ElgamalPublicKey
{
    private static final int MIN_NUMBER_BIT_IN_KEY = 64;
    private BigInteger y;
    private BigInteger g;
    private BigInteger p;
    static List<BigInteger> primitiveRoots = new ArrayList<>();
    private static final int NUMBER_CHECK_SIMPLICITY = 50;

    public ElgamalPublicKey(BigInteger p, BigInteger g, BigInteger x)
    {
        this.p = p;
        this.g = g;
        this.y = g.modPow(x, p); // y = g^x mod p
    }

    public ElgamalPublicKey() {

    }

    public BigInteger getP()
    {
        return this.p;
    }
    public BigInteger getG()
    {
        return this.g;
    }
    public BigInteger getY()
    {
        return this.y;
    }

    public void setP(BigInteger p)
    {
        this.p = p;
    }
    public void setG(BigInteger g)
    {
        this.g = g;
    }
    public void setY(BigInteger y)
    {
        this.y = y;
    }

    public static BigInteger generateParamG(BigInteger p)
    {
        if (p.equals(BigInteger.TWO))
        {
            return new BigInteger("1");
        }
        Random random = new Random(System.currentTimeMillis());
        SimplicityTest simplicityTest = new SimplicityTest();
        BigInteger g;
        BigInteger pSubtractOne = p.subtract(BigInteger.ONE);
        while (true) {
            do {
                g = new BigInteger(p.bitLength(), random);
            } while (g.compareTo(BigInteger.ONE) <= 0 || g.compareTo(p) >= 0 || !simplicityTest.testMillerRabin(g, NUMBER_CHECK_SIMPLICITY) || g.bitLength() != MIN_NUMBER_BIT_IN_KEY);

            if (g.modPow(pSubtractOne.divide(BigInteger.TWO), p).equals(BigInteger.ONE)) {
                continue;
            }
            if (!g.modPow(pSubtractOne, p).equals(BigInteger.ONE)) {
                continue;
            }
            for (BigInteger bi = BigInteger.valueOf(2);
                 bi.compareTo(p) < 0;
                 bi = bi.add(BigInteger.ONE)) {
                boolean flagPrimaryRoot = true;
                for (BigInteger bj = BigInteger.valueOf(1);
                     bj.compareTo(bi) < 0;
                     bj = bj.add(BigInteger.ONE)) {
                    if (g.modPow(bj.multiply(pSubtractOne).divide(bi), p).equals(BigInteger.ONE)) {
                        flagPrimaryRoot = false;
                        break;
                    }
                }
                if (flagPrimaryRoot) {
                    return g;
                }
            }
        }
    }
}
