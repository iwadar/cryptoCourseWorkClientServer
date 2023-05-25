package org.example.elgamal;

import java.math.BigInteger;
import java.util.Random;

public class ElgamalKey
{
    private static final int MIN_NUMBER_BIT_IN_KEY = 64;
    private static final int NUMBER_CHECK_SIMPLICITY = 50;
    private ElgamalPublicKey publicKey;
    private BigInteger x; // private key

    public ElgamalPublicKey getPublicKey()
    {
        return this.publicKey;
    }

    public ElgamalKey() {}

    public ElgamalKey(BigInteger p, BigInteger g, BigInteger y) {
        this.publicKey = new ElgamalPublicKey();
        this.publicKey.setP(p);
        this.publicKey.setG(g);
        this.publicKey.setY(y);
    }

    public BigInteger getX() {
        return this.x;
    }

    public void generateKey()
    {
        Random random = new Random(System.currentTimeMillis());
        SimplicityTest simplicityTest = new SimplicityTest();
        BigInteger p;
        do {
            p = new BigInteger(MIN_NUMBER_BIT_IN_KEY, random);
        } while (!simplicityTest.testMillerRabin(p, NUMBER_CHECK_SIMPLICITY) || p.bitLength() != MIN_NUMBER_BIT_IN_KEY);

        BigInteger g = ElgamalPublicKey.generateParamG(p);
//        double lengthOfPrivateKey = Math.random()*(p.bitLength()) + 2;

        do {
            this.x = new BigInteger(p.bitLength(), random);
        } while(this.x.compareTo(p.subtract(BigInteger.ONE)) >= 0 || this.x.compareTo(BigInteger.ONE) <= 0 || x.bitLength() != MIN_NUMBER_BIT_IN_KEY);
        this.publicKey = new ElgamalPublicKey(p, g, this.x);
    }
}
