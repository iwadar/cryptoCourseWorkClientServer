package org.example.elgamal;

import java.math.BigInteger;

public interface ISimplicityTest
{
    boolean testMillerRabin(BigInteger n, long rounds);
}
