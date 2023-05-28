package org.example.mode;

import org.example.camellia.Camellia;
import org.example.camellia.ISymmetricalCipher;

import static org.example.HelpFunction.*;

public class OFBMode implements IModeCipher
{

    private ISymmetricalCipher symmetricalAlgorithm;
    private byte[] initializationVector;
    private byte[] prevBlock;

    public OFBMode(ISymmetricalCipher c, byte[] IV)
    {
        this.symmetricalAlgorithm = c;
        this.initializationVector = IV;
        this.prevBlock = IV;
    }

    public OFBMode(byte[] IV)
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
                prevBlock = symmetricalAlgorithm.encrypt(prevBlock);
                System.arraycopy(XORByteArray(prevBlock, getArray128(notCipherText, i)), 0, notCipherText, i, 16);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return notCipherText;
    }

    @Override
    public byte[] decrypt(byte[] cipherText)
    {
        reset();
        prevBlock = symmetricalAlgorithm.encrypt(prevBlock);

        for (int i = 0; i < cipherText.length; i += 16)
        {
            byte[] block = getArray128(cipherText, i);
            System.arraycopy(XORByteArray(prevBlock, block), 0, cipherText, i, 16);
            prevBlock = symmetricalAlgorithm.encrypt(prevBlock);
        }
        return cipherText;
    }

    @Override
    public void reset()
    {
        this.prevBlock = this.initializationVector;
    }
}
