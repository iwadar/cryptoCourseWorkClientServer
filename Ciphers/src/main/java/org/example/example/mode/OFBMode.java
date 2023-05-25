package org.example.example.mode;

import org.example.example.camellia.Camellia;

import static org.example.HelpFunction.*;

public class OFBMode implements IModeCipher
{

    private Camellia symmetricalAlgorithm;
    private byte[] initializationVector;
    private byte[] prevBlock;

    public OFBMode(Camellia c, byte[] IV)
    {
        symmetricalAlgorithm = c;
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
        byte[] copyInputArrayWithPadding = padding(notCipherText, 16);
        try
        {
            for (int i = 0; i < copyInputArrayWithPadding.length; i += 16)
            {
               prevBlock = symmetricalAlgorithm.encrypt(prevBlock);
               System.arraycopy(XORByteArray(prevBlock, getArray128(copyInputArrayWithPadding, i)), 0, copyInputArrayWithPadding, i, 16);
            }
        } catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return copyInputArrayWithPadding;
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
        cipherText = deletePadding(cipherText);
        return cipherText;
    }

    @Override
    public void reset()
    {
        this.prevBlock = this.initializationVector;
    }
}
