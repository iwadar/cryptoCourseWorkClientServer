package org.example.example.mode;

public interface IModeCipher
{
    public byte[] encrypt(byte[] notCipherText);
    public byte[] decrypt(byte[] cipherText);
    public void reset();
}
