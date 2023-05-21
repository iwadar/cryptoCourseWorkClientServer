package org.example.elgamal;

import java.math.BigInteger;
import java.util.Random;

public class SimplicityTest implements ISimplicityTest
{
    // Функция проверки простоты числа n с помощью k раундов теста Миллера-Рабина
    // n > 2 и n - нечетное
    public boolean testMillerRabin(BigInteger n, long k)
    {
        if (n.equals(BigInteger.TWO))
        {
            return true;
        }
        if (n.compareTo(BigInteger.TWO) < 0)
        {
            return false;
        }
        // Разложение числа на вид n-1 = (2^s) * t
        BigInteger t = n.subtract(BigInteger.ONE);
        int s = 0;
        while (t.mod(BigInteger.TWO).equals(BigInteger.ZERO))
        {
            t = t.divide(BigInteger.TWO);
            s++;
        }

        // Проверка k раундов теста Миллера-Рабина
        for (int i = 0; i < k; i++)
        {
            BigInteger a = getRandomBase(n);
            BigInteger x = a.modPow(t, n);
            if (x.equals(BigInteger.ONE) || x.equals(n.subtract(BigInteger.ONE)))
            {
                continue;
            }
            boolean isProbablePrime = false;
            for (int j = 0; j < s - 1; j++)
            {
                x = x.modPow(BigInteger.TWO, n);
                if (x.equals(BigInteger.ONE))
                {
                    return false;
                }
                if (x.equals(n.subtract(BigInteger.ONE)))
                {
                    isProbablePrime = true;
                    break;
                }
            }
            if (!isProbablePrime)
            {
                return false;
            }
        }
        return true;
    }
        // Генерация случайного основания a из интервала [2, n-2]
        private static BigInteger getRandomBase(BigInteger n)
        {
            Random random = new Random(System.currentTimeMillis());
            BigInteger a;
            do {
                a = new BigInteger(n.bitLength(), random);
            } while (a.compareTo(n) >= 0 || a.compareTo(BigInteger.ONE) <= 0);
            return a;
        }
}
