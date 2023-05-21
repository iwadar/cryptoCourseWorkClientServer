package com.example.clientwithui.camellia;

public interface ISymmetricalCipher
{
        byte[] encrypt(byte[] text);
        byte[] decrypt(byte[] text);
}
