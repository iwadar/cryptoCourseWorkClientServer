package com.example.clientwithui;

import com.example.clientwithui.camellia.ISymmetricalCipher;
import com.example.clientwithui.mode.CBCMode;
import com.example.clientwithui.mode.ECBMode;
import com.example.clientwithui.mode.IModeCipher;
import com.example.clientwithui.mode.ModeCipher;

public class D_Encryption {
    private final ISymmetricalCipher algorithm;
    ModeCipher mode;
    IModeCipher modeForCrypto;
    private byte[] initVector;

    protected D_Encryption(ISymmetricalCipher algo, ModeCipher mode, String IV){
        this.mode = mode;
        this.initVector = IV.getBytes();
        this.algorithm = algo;
        this.modeForCrypto = returnConcreteMode();
    }


    private IModeCipher returnConcreteMode(){
        switch (mode) {
            case ECB :
                return new ECBMode(algorithm);
            case CBC:
                return new CBCMode(algorithm, initVector);
//            case CFB:
//                return new ModeCFB(algo, initVector);
//            case OFB:
//                return new ModeOFB(algo, initVector);
//            case CTR:
//                return new ModeCTR(algo, initVector);
//            case RD:
//                return new ModeRD(algo, initVector);
//            case RDH:
//                return new ModeRDH(algo, initVector);
        }
        return new ECBMode(algorithm);
    }

    public byte[] encrypt(byte[] text) {
        return modeForCrypto.encrypt(text);
    }
    public byte[] decrypt(byte[] text) {
        return modeForCrypto.decrypt(text);
    }
}
