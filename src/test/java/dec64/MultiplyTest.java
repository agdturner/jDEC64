package dec64;

import dec64.annotations.DEC64;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

import static dec64.Constants64.DEC64_NAN;
import static dec64.Constants64.DEC64_ONE;
import static dec64.Constants64.DEC64_TWO;
import static dec64.Constants64.DEC64_ZERO;

@RunWith(Parameterized.class)
public class MultiplyTest {
    @DEC64 private static final long nan = DEC64_NAN;
    @DEC64 private static final long nannan = 32896;
    @DEC64 private static final long zero = DEC64_ZERO;
    @DEC64 private static final long zip = 250;
    @DEC64 private static final long one = DEC64_ONE;
    @DEC64 private static final long two = DEC64_TWO;
    @DEC64 private static final long three = dec64_new(3, 0);
    @DEC64 private static final long four = dec64_new(4, 0);
    @DEC64 private static final long five = dec64_new(5, 0);
    @DEC64 private static final long six = dec64_new(6, 0);
    @DEC64 private static final long seven = dec64_new(7, 0);
    @DEC64 private static final long eight = dec64_new(8, 0);
    @DEC64 private static final long nine = dec64_new(9, 0);
    @DEC64 private static final long ten = dec64_new(10, 0);
    @DEC64 private static final long minnum = dec64_new(1, -127);
    @DEC64 private static final long maxnum = dec64_new(36028797018963967L, 127);
    @DEC64 private static final long half = dec64_new(5, -1);
    @DEC64 private static final long negative_nine = dec64_new(-9, 0);
    @DEC64 private static final long pi = dec64_new(31415926535897932L, -16);
    @DEC64 private static final long maxint = dec64_new(36028797018963967L, 0);
    @DEC64 private static final long negative_maxint = dec64_new(-36028797018963968L, 0);
    @DEC64 private static final long one_over_maxint = dec64_new(27755575615628914L, -33);
    @DEC64 private static final long negative_one = dec64_new(-1, 0);
    @DEC64 private static final long negative_pi = dec64_new(-31415926535897932L, -16);
    @DEC64 private static final long epsilon = dec64_new(1, -16);

    @DEC64 private final long first;
    @DEC64 private final long second;
    @DEC64 private final long expected;
    private final String comment;

    @Parameterized.Parameters(name="{3}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {nan, nan, nan, "nan * nan"},
            {nan, zero, zero, "nan * zero"},
            {nannan, nannan, nan, "nannan * nannan"},
            {nannan, one, nan, "nannan * 1"},
            {zero, nan, zero, "0 * nan"},
            {zero, nannan, zero, "0 * nannan"},
            {zero, zip, zero, "zero * zip"},
            {zero, maxnum, zero, "zero * maxnum"},
            {zip, zero, zero, "zip * zero"},
            {zip, zip, zero, "zip * zip"},
            {minnum, half, minnum, "minnum * half"},
            {minnum, minnum, zero, "minnum * minnum"},
            {epsilon, epsilon, dec64_new(1, -32), "epsilon * epsilon"},
            {one, nannan, nan, "1 * nannan"},
            {negative_one, one, negative_one, "-1 * 1"},
            {negative_one, negative_one, one, "-1 * -1"},
            {two, five, ten, "2 * 5"},
            {two, maxnum, nan, "2 * maxnum"},
            {two, dec64_new(36028797018963967L, 126), dec64_new(7205759403792793L, 127), "2 * a big one"},
            {three, two, six, "3 * 2"},
            {ten, dec64_new(36028797018963967L, 126), maxnum, "10 * a big one"},
            {ten, dec64_new(1, 127), dec64_new(10, 127), "10 * 1e127"},
            {dec64_new(1, 2), dec64_new(1, 127), dec64_new(100, 127), "1e2 * 1e127"},
            {dec64_new(1, 12), dec64_new(1, 127), dec64_new(1000000000000L, 127), "1e2 * 1e127"},
            {dec64_new(1, 12), dec64_new(1, 127), dec64_new(1000000000000L, 127), "1e12 * 1e127"},
            {dec64_new(3, 16), dec64_new(1, 127), dec64_new(30000000000000000L, 127), "3e16 * 1e127"},
            {dec64_new(3, 17), dec64_new(1, 127), nan, "3e16 * 1e127"},
            {dec64_new(-3, 16), dec64_new(1, 127), dec64_new(-30000000000000000L, 127), "3e16 * 1e127"},
            {dec64_new(-3, 17), dec64_new(1, 127), nan, "3e16 * 1e127"},
            {dec64_new(9999999999999999L, 0), ten, dec64_new(9999999999999999L, 1), "9999999999999999 * 10"},
            {maxint, zero, zero, "maxint * zero"},
            {maxint, epsilon, dec64_new(36028797018963967L, -16), "maxint * epsilon"},
            {maxint, maxint, dec64_new(12980742146337068L, 17), "maxint * maxint"},
            {maxint, one_over_maxint, one, "maxint * 1 / maxint"},
            {negative_maxint, nan, nan, "-maxint * nan"},
            {negative_maxint, maxint, dec64_new(-12980742146337069L, 17), "-maxint * maxint"},
            {maxnum, maxnum, nan, "maxnum * maxnum"},
            {maxnum, minnum, maxint, "maxnum * minnum"},
        });
    }

    public MultiplyTest(@DEC64 long first, @DEC64 long second, @DEC64 long expected, String comment) {
        this.first = first;
        this.second = second;
        this.expected = expected;
        this.comment = comment;
    }

    @Test
    public void multiply() {
        assertEquals(comment, expected, Basic64.multiply(first, second));
    }

    private static @DEC64 long dec64_new(@DEC64 long coeff, @DEC64 long exponent) {
        return Basic64.of(coeff, exponent);
    }
}
