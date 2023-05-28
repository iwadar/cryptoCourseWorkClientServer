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

    public boolean testFerma(BigInteger n, long k) {
        BigInteger a;
        for (long i = 0; i < k; i++)
        {
            a = getRandomBase(n);
            if (!a.modPow(n.subtract(BigInteger.ONE), n).equals(BigInteger.ONE)) {
                return false;
            }
        }
        return true;
    }

    private static int J(BigInteger n, BigInteger m) {
        if (n.mod(m).equals(BigInteger.ZERO)) {
            return 0; // m делит n
        }
        int j = 1;
        if (n.compareTo(BigInteger.ZERO) < 0) {
            n = n.negate(); // правило (b/n) = (-1)^((n-1)/2) * (1/n) * b
            if (m.mod(BigInteger.valueOf(4)).equals(BigInteger.valueOf(3))) {
                j = -j; // n отрицательно и m по модулю находится по 3 модулю 4
            }
        }
        while (!n.equals(BigInteger.ZERO)) {
            while (n.mod(BigInteger.TWO).equals(BigInteger.ZERO)) { // правило (2/n) = (-1)^((n^2-1)/8)
                n = n.divide(BigInteger.TWO);
                if (m.mod(BigInteger.valueOf(8)).equals(BigInteger.valueOf(3)) || m.mod(BigInteger.valueOf(8)).equals(BigInteger.valueOf(5))) {
                    j = -j;
                }
            }
            BigInteger temp = n; // Поменяем местами n и m, чтобы n было большим
            n = m;
            m = temp;

            if (n.mod(BigInteger.valueOf(4)).equals(BigInteger.valueOf(3)) && m.mod(BigInteger.valueOf(4)).equals(BigInteger.valueOf(3))) { // правило (a/b) = -(b/a)
                j = -j;
            }
            n = n.mod(m);

        }
        if (m.equals(BigInteger.ONE)) {
            return j;
        } else {
            return 0; // m не является взаимно простым с n
        }
    }

    public boolean testSoloveyStrassen(BigInteger n, long k) {
        if (n.equals(BigInteger.TWO))
        {
            return true;
        }
        if (n.compareTo(BigInteger.TWO) < 0)
        {
            return false;
        }

        for (long i = 0; i < k; i++) {
            BigInteger a = getRandomBase(n);
            BigInteger exponent = n.subtract(BigInteger.ONE).divide(BigInteger.TWO);
            BigInteger jacobi = BigInteger.valueOf(J(a, n));
            if (!a.gcd(n).equals(BigInteger.ONE)) {
                return false;
            }
            if (!a.modPow(exponent, n).equals(jacobi)) {
                return false;
            }
        }
        return true;
    }
}
