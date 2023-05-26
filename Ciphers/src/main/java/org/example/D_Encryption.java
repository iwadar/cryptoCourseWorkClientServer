package org.example;

import org.example.camellia.ISymmetricalCipher;
import org.example.mode.CBCMode;
import org.example.mode.ECBMode;
import org.example.mode.IModeCipher;
import org.example.mode.ModeCipher;

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
