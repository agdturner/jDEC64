package dec64;

import dec64.annotations.DEC64;
import static dec64.Constants64.*;

/**
 *
 * @author kittylyst
 */
public final class Basic64 {

    // Max and min coefficients - not DEC64 values - should not be neded outside the library
    final static long DEC64_MAX_COEFFICIENT = 0x7f_ffff_ffff_ffffL; // 36_028_797_018_963_967L
    final static long DEC64_MIN_COEFFICIENT = -36_028_797_018_963_968L; // -0x80_0000_0000_0000L

    private final static long DEC64_EXPONENT_MASK = 0xFFL;
    private final static long DEC64_COEFFICIENT_MASK = 0xffff_ffff_ffff_ff00L;

    private final static long DEC64_COEFFICIENT_OVERFLOW_MASK = 0x7F00_0000_0000_0000L;

    private final static byte MAX_DIGITS = 17;

    private static final long[] POWERS = new long[20];

    static {
        POWERS[0] = 1;
        for (int i = 1; i < POWERS.length; i++) {
            POWERS[i] = POWERS[i-1] * 10;
        }
    }

    private Basic64() {
    }

    /**
     * Returns the coefficient of a DEC64 number as a long. 
     * The value will be 56 bits long and 
     * @param number
     * @return 
     */
    public static long coefficient(@DEC64 long number) {
        return number > 0 ? number >> 8 : -(-number >> 8);
    }

    public static byte exponent(@DEC64 long number) {
        return (byte) (number & DEC64_EXPONENT_MASK);
    }

    public static long exponentAsLong(byte exp) {
        return ((long) exp & DEC64_EXPONENT_MASK);
    }

    public static boolean overflow(long number) {
        return (number & DEC64_COEFFICIENT_OVERFLOW_MASK) != 0;
    }

    public static byte digits(@DEC64 long number) {
        if (isNaN(number)) {
            return -1;
        }
        if (isZero(number)) {
            return 0;
        }
        byte out = 0;
        long coeff = coefficient(abs(number));
        while (coeff > 0) {
            coeff = coeff / 10L;
            out++;
        }
        return out;
    }

    public static @DEC64
    long of(long coeff, long exponent) {
        if (coeff == 0) {
            return DEC64_ZERO;
        }
        return pack(coeff, exponent);
    }

    public static @DEC64
    long level(@DEC64 long number) {
        return number & DEC64_COEFFICIENT_MASK;
    }

    public static @DEC64
    long reduceExponent(@DEC64 long number) {
        return of(10 * coefficient(number), (byte) (exponent(number) - 1));
    }

    public static @DEC64
    long canonical(@DEC64 long number) {
        if (isNaN(number))
            return DEC64_NAN;
        byte exp = exponent(number);
        if (exp == 127 || exp == 0)
            return number;

        long out = number;
        long coeff = coefficient(number);
        if (exp > 0) {
            while (exp > 0 && coeff < DEC64_MAX_COEFFICIENT) {
                out = of(10 * coeff, --exp);
                coeff = coefficient(out);
            }
        } else {
            while (exp < 0 && coeff % 10 == 0) {
                out = of(coeff / 10L, ++exp);
                coeff = coefficient(out);
            }
        }

        return out;
    }

    public static boolean isNaN(@DEC64 long number) {
        return (DEC64_EXPONENT_MASK & (long) exponent(number)) == DEC64_NAN;
    }

    public static boolean isInteger(@DEC64 long number) {
        return exponent(number) < 0;
    }

    public static boolean isBasic(@DEC64 long number) {
        return exponent(number) == 0;
    }

    public static boolean isZero(@DEC64 long number) {
        return !isNaN(number) && coefficient(number) == DEC64_ZERO;
    }

    public static boolean equals64(@DEC64 long a, @DEC64 long b) {
        if (isNaN(a) || isNaN(b))
            return false; // NaN != NaN
        byte expa = exponent(a);
        byte expb = exponent(b);
        if (expa == expb) {
            return coefficient(a) == coefficient(b);
        }

        // Slow path - first reduce the smaller exponent
        if (expa > expb) {
            @DEC64 long lastA = a;
            while (!isNaN(a)) {
                lastA = a;
                a = reduceExponent(a);
                if (exponent(a) == expb) {
                    return coefficient(a) == coefficient(b);
                }
            }
        } else {
            @DEC64 long lastB = b;
            while (!isNaN(b)) {
                lastB = b;
                b = reduceExponent(b);
                if (exponent(b) == expa) {
                    return coefficient(a) == coefficient(b);
                }
            }
        }

        return false;
    }

    public static @DEC64
    long add(@DEC64 long a, @DEC64 long b) {
        if (isNaN(a) || isNaN(b))
            return DEC64_NAN;
        byte expa = exponent(a);
        byte expb = exponent(b);
        if (expa == expb) {
            long coeff = coefficient(a) + coefficient(b);
            return of(coeff, expa);
        }

        // Slow path - first reduceExponent the smaller exponent
        if (expa > expb) {
            @DEC64 long lastA = a;
            while (!isNaN(a)) {
                lastA = a;
                a = reduceExponent(a);
                if (exponent(a) == expb) {
                    long coeff = coefficient(a) + coefficient(b);
                    return of(coeff, expb);
                }
            }
            // Have tried & failed to match by reducing a's exponent.
            // Now we must try to inflate b's exponent to match
            a = lastA;
        } else {
            @DEC64 long lastB = b;
            while (!isNaN(b)) {
                lastB = b;
                b = reduceExponent(b);
                if (exponent(b) == expa) {
                    long coeff = coefficient(a) + coefficient(b);
                    return of(coeff, expa);
                }
            }

        }

        return 0;
    }

    public static @DEC64
    long subtract(@DEC64 long a, @DEC64 long b) {
        if (isNaN(a) || isNaN(b))
            return DEC64_NAN;
        a = canonical(a);
        b = canonical(b);
        byte expa = exponent(a);
        byte expb = exponent(b);
        if (expa == expb) {
            long coeff = coefficient(a) - coefficient(b);
            if (overflow(coeff))
                return DEC64_NAN;
            return of(coeff, expa);
        }
        if (expa > expb) {
            long coeffa = coefficient(a);
            for (int i = 0; i < (expa - expb); i++) {
                // FIXME Implement overflow case
                coeffa *= 10;
            }
            return of(coeffa - coefficient(b), expb);
        } else {
            long coeffb = coefficient(b);
            for (int i = 0; i < (expb - expa); i++) {
                // FIXME Implement overflow case
                coeffb *= 10;
            }
            return of(coeffb - coefficient(a), expa);
        }
    }

    public static @DEC64
    long multiply(@DEC64 long a, @DEC64 long b) {
        if (isZero(a) || isZero(b)) {
            return DEC64_ZERO;
        }
        if (isNaN(a) || isNaN(b))
            return DEC64_NAN;
        final long coeff = coefficient(a) * coefficient(b);
        if (overflow(coeff))
            return DEC64_NAN;
        return of(coeff, (exponent(a) + exponent(b)));
    }

    /**
     * 
     * @param a
     * @param b
     * @return 
     */
    public static @DEC64
    long divide(@DEC64 long a, @DEC64 long b) {
        if (isNaN(a) || isNaN(b))
            return DEC64_NAN;
        if (coefficient(a) == 0)
            return DEC64_ZERO;
        if (coefficient(b) == 0)
            return DEC64_NAN;

        @DEC64 long recip = reciprocal(of(coefficient(b),0));

        return multiply(of(coefficient(a), exponent(a) - exponent(b)), recip);
    }

    public static @DEC64
    long reciprocal(@DEC64 long r) {
        if (isNaN(r) || coefficient(r) == 0)
            return DEC64_NAN;

        byte digits = digits(r);
        if (digits < 1)
            return DEC64_NAN;

        byte exp = exponent(r);
        long coeff = coefficient(r);
        long numerator = 1L;
        for (byte b = 0; b < digits-1; b++) {
            numerator *= 10;
        }

        long ratio = numerator / coeff;
        long remainder = numerator % coeff;
        long outCoeff = ratio;

        MAIN: while (remainder > 0) {
            while (remainder < coeff) {
                if (outCoeff * 10 > DEC64_MAX_COEFFICIENT) {
                    break MAIN;
                }
                outCoeff *= 10;
                remainder *= 10;
                exp++; // ????
            }
            ratio =  remainder / coeff;
            remainder = remainder % coeff;
            
            outCoeff += ratio;
        }

        return of(outCoeff,-(exp + digits - 1));
    }

    public static @DEC64
    long abs(@DEC64 long number) {
        if (isNaN(number))
            return DEC64_NAN;
        @DEC64 long coeff = coefficient(number);
        if (coeff >= 0)
            return number;
        if (coeff > DEC64_MIN_COEFFICIENT)
            return of(-coeff, exponent(number));
        return DEC64_NAN;
    }

    public static @DEC64
    long dec(@DEC64 long minuend) {
        minuend = canonical(minuend);
        if (isBasic(minuend)) {
            return minuend - DEC64_ONE;
        }
        byte exp = exponent(minuend);
        if (exp > 0) {
            return minuend;
        }
        return subtract(minuend, DEC64_ONE);
    }/* decrementation */


    public static @DEC64
    long half(@DEC64 long dividend) {
        if (isNaN(dividend))
            return DEC64_NAN;
        long coeff = coefficient(dividend);
        byte exp = exponent(dividend);
        // FIXME If coeff is large, this might not work
        if ((coeff & 1L) == 0L) {
            return of(coeff / 2L, exp);
        }
        return of(coeff * 5, --exp);
    }/* quotient */


    public static @DEC64
    long inc(@DEC64 long augend) {
        augend = canonical(augend);
        if (isBasic(augend)) {
            return augend + DEC64_ONE;
        }
        byte exp = exponent(augend);
        if (exp > 0) {
            return augend;
        }

        return add(augend, DEC64_ONE);
    }

    public static @DEC64
    long modulo(@DEC64 long dividend, @DEC64 long divisor) {
        if (coefficient(divisor) == 0)
            return DEC64_NAN;

        // Modulo. It produces the same result as
        return subtract(dividend, multiply(integer_divide(dividend, divisor), divisor));
    }/* modulation */


    public static @DEC64
    long neg(@DEC64 long number) {
        if (isNaN(number))
            return DEC64_NAN;
        @DEC64 long coeff = coefficient(number);
        if (coeff > DEC64_MIN_COEFFICIENT)
            return of(-coeff, exponent(number));
        return DEC64_NAN;
    }

    ////////////////////////////////////////////////////////
    public static long normal(long number) {
        return DEC64_NAN;
    }/* normalization */


    public static long not(long value) {
        return DEC64_NAN;
    }/* notation */


    public static long round(long number, long place) {
        return 0;
    }/* quantization */


    public static long signum(long number) {
        return DEC64_NAN;
    }/* signature */


    public static @DEC64
    long floor(@DEC64 long dividend) {
        return DEC64_NAN;
    }/* integer */


    public static @DEC64
    long integer_divide(@DEC64 long dividend, @DEC64 long divisor) {
        if (coefficient(divisor) == 0)
            return DEC64_NAN;
        // FIXME
        return 0;
    }/* quotient */


    public static @DEC64
    long ceiling(@DEC64 long number) {
        return DEC64_NAN;
    }/* integer */


    public static boolean less(@DEC64 long comparahend, @DEC64 long comparator) {
        return false;
    }/* comparison */


    public static @DEC64
    long pack(long coeff, long exponent) {
        final long ultimateCoefficient = 36028797018963968L;
        final long eightOverTen = -3689348814741910323L;

        for (;;) {
            if (exponent > 127) {
                return packDecrease(coeff, exponent);
            }

            //mov     r10,r0          ; r10 is the coefficient
            //mov     r1,3602879701896396800 ; the ultimate coefficient * 100
            //not     r10             ; r10 is -coefficient

            //test    r10,r10         ; look at the sign bit
            //cmovs   r10,r0          ; r10 is the absolute value of the coefficient
            final long absCoefficient = Math.abs(coeff);

            //cmp     r10,r1          ; compare with the actual coefficient
            //jae     pack_large      ; deal with the very large coefficient
            if (absCoefficient >= coeff) {
                //mov     r1,r0           ; r1 is the coefficient
                //sar     r1,63           ; r1 is -1 if negative, or 0 if positive
                final long sign = coeff >>= 63;

                //mov     r9,r1           ; r9 is -1 or 0
                //xor     r0,r1           ; complement the coefficient if negative
                coeff ^= sign;

                //and     r9,1            ; r9 is 1 or 0
                final long absSign = sign & 1;

                //add     r0,r9           ; r0 is absolute value of coefficient
                coeff += absSign;

                //add     r8,1            ; add 1 to the exponent
                exponent++;

                // FIXME the multiplication below yields a 128-bit result in X86-64 assembly,
                //       high bits stored in r2, low bits stored in r0. We lean on multiplyHigh (Java 9+).
                //
                //mov     r11,eight_over_ten ; magic number
                //mul     r11             ; multiply abs(coefficient) by magic number
                //mov     r0,r2           ; r0 is the product shift 64 bits
                coeff = Math.multiplyHigh(coeff, eightOverTen);

                //shr     r0,3            ; r0 is divided by 8: the abs(coefficient) / 10
                //xor     r0,r1           ; complement the coefficient if it was negative
                //add     r0,r9           ; coefficient's sign is restored
                coeff >>>= 3;
                coeff ^= sign;
                coeff += absSign;

                //jmp     pack            ; start over
                continue;
            }

            //xor     r11,r11         ; r11 is zero
            long n = 0;

            //mov     r1,36028797018963967 ; the ultimate coefficient - 1
            //mov     r9,-127         ; r9 is the ultimate exponent
            //cmp     r1,r10          ; compare with the actual coefficient
            //adc     r11,0           ; add 1 to r11 if 1 digit too big
            if (absCoefficient >= ultimateCoefficient - 1) {
                n++;
            }

            //cmp     r1,r10          ; compare with the actual coefficient
            //adc     r11,0           ; add 1 to r11 if 2 digits too big
            if (absCoefficient >= ultimateCoefficient * 10  - 1) {
                n++;
            }

            //mov     r1,360287970189639679 ; the ultimate coefficient * 10 - 1
            //sub     r9,r8           ; r9 is the difference from the actual exponent
            final long expDiff = -127 - exponent;

            //cmp     r9,r11          ; which excess is larger?
            //cmovl   r9,r11          ; take the max
            //test    r9,r9           ; if neither was zero
            //jnz     pack_increase   ; then increase the exponent by the excess
            final long expMax = Math.max(expDiff, n);
            if (expMax != 0) {
                //cmp     r9,20           ; is the difference more than 20?
                //jae     return_zero     ; if so, the result is zero (rare)
                if (expMax >= 20) {
                    return DEC64_ZERO;
                }

                //mov     r10,power
                //mov     r10,[r10][r9*8] ; r10 is 10^r9
                final long pow = POWERS[(int) expMax];

                //mov     r11,r10         ; r11 is the power of ten
                //neg     r11             ; r11 is the negation of the power of ten
                long negPow = -pow;

                // FIXME this isn't quite right
                //test    r0,r0           ; examine the sign of the coefficient
                //cmovns  r11,r10         ; r11 has the sign of the coefficient
                //sar     r11,1           ; r11 is half the signed power of ten
                final long temp = negPow >> 1;

                //add     r0,r11          ; r0 is the coefficient plus the rounding fudge
                coeff += temp;

                // FIXME expressing this in Java will probably require some trickery similar to Math.multiplyHigh()
                //cqo                     ; sign extend r0 into r2
                //idiv    r10             ; divide by the power of ten
                coeff /= pow;

                //add     r8,r9           ; increase the exponent
                exponent += expMax;

                //jmp     pack            ; start over
                continue;
            }

            //shl     r0,8            ; shift the exponent into position
            //cmovz   r8,r0           ; if the coefficient is zero, also zero the exp
            //movzx   r8,r8_b         ; zero out all but the bottom 8 bits of the exp
            //or      r0,r8           ; mix in the exponent
            //ret                     ; whew
            return packSimple(coeff, exponent);
        }
    }

    private static @DEC64
    long packDecrease(long coeff, long exponent) {
        // exponent represents 10^N, so for each N > 127 multiply coeff by 10.
        // if we detect an overflow while we're multiplying the best we can do is return a NaN.
        do {
            final long temp = coeff * 10;
            if (temp / 10 != coeff) {
                // overflow
                return DEC64_NAN;
            }
            coeff = temp;
            exponent--;
        } while (exponent > 127);

        //
        // FIXME here I **think** we're just checking that we have room for the coming left shift. extra bit is
        //       likely because the top bit of the coeff is reserved somehow. Need to check the reference, but zzz.
        //
        //       mov r9,$coeff   ; r9 is the coefficient
        //       sar r9,56       ; r9 is top 8 bits of the coefficient
        //       adc r9,0        ; add the ninth bit
        //       jnz return_null ; the number is still too large
        //
        if ((coeff & 0xff80000000000000L) != 0) {
            return DEC64_NAN;
        }

        return packSimple(coeff, exponent);
    }

    private static @DEC64
    long packSimple(long coeff, long exponent) {
        // make room for the exponent
        coeff <<= 8;
        if (coeff == 0) {
            // think this is just a bit of hygiene: coeff is zero and 0 * 10^N == 0, so clear the exponent.
            exponent = 0;
        }
        final long result = coeff | (((byte) exponent) & 0xffL);
        return result;
    }
}
