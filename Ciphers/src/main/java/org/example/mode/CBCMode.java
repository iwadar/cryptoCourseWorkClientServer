package org.example.mode;

import org.example.camellia.ISymmetricalCipher;
import org.example.mode.IModeCipher;

import static org.example.HelpFunction.*;

public class CBCMode implements IModeCipher
{
    private ISymmetricalCipher symmetricalAlgorithm;
    private byte[] initializationVector;
    private byte[] prevBlock;

    public CBCMode(ISymmetricalCipher c, byte[] IV)
    {
        symmetricalAlgorithm = c;
        this.initializationVector = IV;
        this.prevBlock = IV;
    }
    public CBCMode(byte[] IV)
    {
        this.initializationVector = IV;
        this.prevBlock = IV;
    }

    @Override
    public byte[] encrypt(byte[] notCipherText)
    {
        reset();
        byte[] copyInputArrayWithPadding = padding(notCipherText, 16);
        try
        {
            for (int i = 0; i < notCipherText.length; i += 16)
            {
                byte[] block = getArray128(notCipherText, i);
                block = XORByteArray(block, prevBlock);
                prevBlock = symmetricalAlgorithm.encrypt(block);
                System.arraycopy(prevBlock, 0, notCipherText, i, 16);
            }
        } catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return notCipherText;
    }

    @Override
    public byte[] decrypt(byte[] cipherText)
    {
        reset();
        for (int i = 0; i < cipherText.length; i += 16)
        {
            byte[] block = getArray128(cipherText, i);
            var decrypt = symmetricalAlgorithm.decrypt(block);
            System.arraycopy(XORByteArray(prevBlock, decrypt), 0, cipherText, i, 16);
            prevBlock = block;
        }
//        cipherText = deletePadding(cipherText);
        return cipherText;
    }
    @Override
    public void reset()
    {
        this.prevBlock = this.initializationVector;
    }
}
