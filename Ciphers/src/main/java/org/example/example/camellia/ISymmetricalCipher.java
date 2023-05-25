package org.example.example.camellia;

public interface ISymmetricalCipher
{
        byte[] encrypt(byte[] text);
        byte[] decrypt(byte[] text);
}
