package org.example.mode;

import org.example.camellia.Camellia;
import org.example.camellia.ISymmetricalCipher;

import static org.example.HelpFunction.*;

public class CFBMode implements IModeCipher
{
    private ISymmetricalCipher symmetricalAlgorithm;
    private byte[] initializationVector;
    private byte[] prevBlock;

    public CFBMode(ISymmetricalCipher c, byte[] IV)
    {
        this.symmetricalAlgorithm = c;
        this.initializationVector = IV;
        this.prevBlock = IV;
    }
    public CFBMode(byte[] IV)
    {
        this.initializationVector = IV;
        this.prevBlock = IV;
    }


    @Override
    public byte[] encrypt(byte[] notCipherText)
    {
        reset();
        try {
            for (int i = 0; i < notCipherText.length; i += 16)
            {
                var encrypt = symmetricalAlgorithm.encrypt(prevBlock);
                prevBlock = XORByteArray(encrypt, getArray128(notCipherText, i));
                System.arraycopy(prevBlock, 0, notCipherText, i, 16);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return notCipherText;
    }

    @Override
    public byte[] decrypt(byte[] cipherText)
    {
        reset();
        var encrypt = symmetricalAlgorithm.encrypt(prevBlock);
        prevBlock = encrypt;

        for (int i = 0; i < cipherText.length; i += 16)
        {
            byte[] block = getArray128(cipherText, i);
            System.arraycopy(XORByteArray(prevBlock, block), 0, cipherText, i, 16);
            prevBlock = block;
            encrypt = symmetricalAlgorithm.encrypt(prevBlock);
            prevBlock = encrypt;
        }
        return cipherText;
    }

    @Override
    public void reset()
    {
        this.prevBlock = this.initializationVector;
    }
}
