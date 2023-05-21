package com.example.clientwithui.mode;


import com.example.clientwithui.camellia.Camellia;

import static com.example.clientwithui.HelpFunction.*;

public class CBCMode implements IModeCipher
{
    private Camellia symmetricalAlgorithm;
    private byte[] initializationVector;
    private byte[] prevBlock;

    public CBCMode(Camellia c, byte[] IV)
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
            for (int i = 0; i < copyInputArrayWithPadding.length; i += 16)
            {
                byte[] block = getArray128(copyInputArrayWithPadding, i);
                block = XORByteArray(block, prevBlock);
                prevBlock = symmetricalAlgorithm.encrypt(block);
                System.arraycopy(prevBlock, 0, copyInputArrayWithPadding, i, 16);
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
        for (int i = 0; i < cipherText.length; i += 16)
        {
            byte[] block = getArray128(cipherText, i);
            var decrypt = symmetricalAlgorithm.decrypt(block);
            System.arraycopy(XORByteArray(prevBlock, decrypt), 0, cipherText, i, 16);
            prevBlock = block;
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
