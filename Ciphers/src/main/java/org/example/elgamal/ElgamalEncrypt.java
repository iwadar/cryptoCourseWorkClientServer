package org.example.elgamal;

import java.math.BigInteger;
import java.util.Random;

public class ElgamalEncrypt
{
    private ElgamalKey key;
    private BigInteger kSession;

    public ElgamalEncrypt(ElgamalKey key)
    {
        this.key = key;
    }

    public BigInteger[] encrypt(byte[] text)
    {
        BigInteger[] encryptText = new BigInteger[2 * text.length];
        BigInteger pSubtractOne = key.getPublicKey().getP().subtract(BigInteger.ONE);
        Random random = new Random(System.currentTimeMillis());
        do{
            kSession = new BigInteger(pSubtractOne.bitLength(), random);
        } while (!kSession.gcd(pSubtractOne).equals(BigInteger.ONE));
        for(int i = 0; i < encryptText.length; i += 2)
        {
            encryptText[i] = key.getPublicKey().getG().modPow(kSession, key.getPublicKey().getP());
            encryptText[i + 1] = key.getPublicKey().getY().modPow(kSession, key.getPublicKey().getP()).multiply(new BigInteger(String.valueOf(text[i / 2])).mod(key.getPublicKey().getP()));
        }
        return  encryptText;
    }

    public BigInteger[] decrypt(BigInteger[] encryptText)
    {
        BigInteger[] decryptText = new BigInteger[encryptText.length / 2];
        for(int i = 0; i < encryptText.length; i += 2)
        {
            decryptText[i / 2] = encryptText[i + 1].multiply(encryptText[i].modPow(key.getPublicKey().getP().subtract(BigInteger.ONE).subtract(key.getX()), key.getPublicKey().getP())).mod(key.getPublicKey().getP());
//            encryptText[i] = key.getPublicKey().getG().modPow(kSession, key.getPublicKey().getP());
//            encryptText[i + 1] = key.getPublicKey().getY().modPow(kSession, key.getPublicKey().getP()).multiply(new BigInteger(String.valueOf(text[i / 2])).mod(key.getPublicKey().getP()));
        }
//        byte[] array = new byte[decryptText.length];
//        for (int i = 0; i < decryptText.length; i++)
//        {
//            byte arr[] = decryptText[i].toByteArray();
//            System.arraycopy(arr, 0, array, i, arr.length);
//        }
//        return array;
        return decryptText;
    }
}
