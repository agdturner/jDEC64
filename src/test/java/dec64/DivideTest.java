package dec64;

import dec64.annotations.DEC64;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;
import static dec64.Constants64.*;

@RunWith(Parameterized.class)
public class DivideTest {
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
            {nannan, DEC64_TWO, nan, "nannan"},
            {nan, DEC64_TWO, nan, "nan"},
            {zero, DEC64_TWO, zero, "zero"},
            {zip, DEC64_TWO, zero, "zip"},
            {one, DEC64_TWO, half, "one"},
            {two, DEC64_TWO, one, "two"},
            {ten, DEC64_TWO, five, "ten"},
            {minnum, DEC64_TWO, minnum, "minnum"},
            {dec64_new(-2, 0), DEC64_TWO, dec64_new(-1, 0), "-2"},
            {dec64_new(-1, 0), DEC64_TWO, dec64_new(-5, -1), "-1"},
            {nan, nan, nan, "nan / nan"},
            {four, two, two, "4 / 2"},
            {six, two, three, "6 / 2"},
            {dec64_new(4195835, 0), dec64_new(3145727, 0), dec64_new(13338204491362410L, -16), "4195835 / 3145727"},
            {nan, three, nan, "nan / 3"},
            {nannan, nannan, nan, "nannan / nannan"},
            {nannan, one, nan, "nannan / 1"},
            {zero, nan, zero, "0 / nan"},
            {zero, nannan, zero, "0 / nannan"},
            {zero, zip, zero, "zero / zip"},
            {zip, nan, zero, "zip / nan"},
            {zip, nannan, zero, "zip / nannan"},
            {zip, zero, zero, "zip / zero"},
            {zip, zip, zero, "zip / zip"},
            {zero, one, zero, "0 / 1"},
            {zero, zero, zero, "0 / 0"},
            {one, zero, nan, "1 / 0"},
            {one, negative_one, dec64_new(-10000000000000000L, -16), "1 / -1"},
            {negative_one, one, dec64_new(-10000000000000000L, -16), "-1 / 1"},
            {one, two, dec64_new(5000000000000000L, -16), "1 / 2"},
            {one, three, dec64_new(33333333333333333L, -17), "1 / 3"},
            {two, three, dec64_new(6666666666666667L, -16), "2 / 3"},
            {two, dec64_new(30000000000000000L, -16), dec64_new(6666666666666667L, -16), "2 / 3 alias"},
            {dec64_new(20000000000000000L, -16), three, dec64_new(6666666666666667L, -16), "2 / 3 alias"},
            {dec64_new(20000000000000000L, -16), dec64_new(30000000000000000L, -16), dec64_new(6666666666666667L, -16), "2 / 3 alias"},
            {five, three, dec64_new(16666666666666667L, -16), "5 / 3"},
            {five, dec64_new(-30000000000000000L, -16), dec64_new(-16666666666666667L, -16), "5 / -3"},
            {dec64_new(-50000000000000000L, -16), three, dec64_new(-16666666666666667L, -16), "-5 / 3"},
            {dec64_new(-50000000000000000L, -16), dec64_new(-30000000000000000L, -16), dec64_new(16666666666666667L, -16), "-5 / -3"},
            {six, nan, nan, "6 / nan"},
            {six, three, dec64_new(20000000000000000L, -16), "6 / 3"},
            {zero, nine, zero, "0 / 9"},
            {one, nine, dec64_new(11111111111111111L, -17), "1 / 9"},
            {two, nine, dec64_new(22222222222222222L, -17), "2 / 9"},
            {three, nine, dec64_new(33333333333333333L, -17), "3 / 9"},
            {four, nine, dec64_new(4444444444444444L, -16), "4 / 9"},
            {five, nine, dec64_new(5555555555555556L, -16), "5 / 9"},
            {six, nine, dec64_new(6666666666666667L, -16), "6 / 9"},
            {seven, nine, dec64_new(7777777777777778L, -16), "7 / 9"},
            {eight, nine, dec64_new(8888888888888889L, -16), "8 / 9"},
            {nine, nine, one, "9 / 9"},
            {zero, negative_nine, zero, "0 / -9"},
            {one, negative_nine, dec64_new(-11111111111111111L, -17), "1 / -9"},
            {two, negative_nine, dec64_new(-22222222222222222L, -17), "2 / -9"},
            {three, negative_nine, dec64_new(-33333333333333333L, -17), "3 / -9"},
            {four, negative_nine, dec64_new(-4444444444444444L, -16), "4 / -9"},
            {five, negative_nine, dec64_new(-5555555555555556L, -16), "5 / -9"},
            {six, negative_nine, dec64_new(-6666666666666667L, -16), "6 / -9"},
            {seven, negative_nine, dec64_new(-7777777777777778L, -16), "7 / -9"},
            {eight, negative_nine, dec64_new(-8888888888888889L, -16), "8 / -9"},
            {nine, negative_nine, negative_one, "9 / -9"},
            {pi, negative_pi, dec64_new(-10000000000000000L, -16), "pi / -pi"},
            {negative_pi, pi, dec64_new(-10000000000000000L, -16), "-pi / pi"},
            {negative_pi, negative_pi, dec64_new(10000000000000000L, -16), "-pi / -pi"},
            {dec64_new(-16, 0), ten, dec64_new(-16, -1), "-16 / 10"},
            {maxint, epsilon, dec64_new(36028797018963967L, 16), "maxint / epsilon"},
            {one, maxint, one_over_maxint, "one / maxint"},
            {one, one_over_maxint, maxint, "one / one / maxint"},
            {one, negative_maxint, dec64_new(-27755575615628914L, -33), "one / -maxint"},
            {maxnum, epsilon, nan, "maxnum / epsilon"},
            {maxnum, maxnum, dec64_new(10000000000000000L, -16), "maxnum / maxnum"},
            {dec64_new(10, -1), maxint, one_over_maxint, "one / maxint alias 1"},
            {dec64_new(100, -2), maxint, one_over_maxint, "one / maxint alias 2"},
            {dec64_new(1000, -3), maxint, one_over_maxint, "one / maxint alias 3"},
            {dec64_new(10000, -4), maxint, one_over_maxint, "one / maxint alias 4"},
            {dec64_new(100000, -5), maxint, one_over_maxint, "one / maxint alias 5"},
            {dec64_new(1000000, -6), maxint, one_over_maxint, "one / maxint alias 6"},
            {dec64_new(10000000, -7), maxint, one_over_maxint, "one / maxint alias 7"},
            {dec64_new(100000000, -8), maxint, one_over_maxint, "one / maxint alias 8"},
            {dec64_new(1000000000, -9), maxint, one_over_maxint, "one / maxint alias 9"},
            {dec64_new(10000000000L, -10), maxint, one_over_maxint, "one / maxint alias 10"},
            {dec64_new(100000000000L, -11), maxint, one_over_maxint, "one / maxint alias 11"},
            {dec64_new(1000000000000L, -12), maxint, one_over_maxint, "one / maxint alias 12"},
            {dec64_new(10000000000000L, -13), maxint, one_over_maxint, "one / maxint alias 13"},
            {dec64_new(100000000000000L, -14), maxint, one_over_maxint, "one / maxint alias 14"},
            {dec64_new(1000000000000000L, -15), maxint, one_over_maxint, "one / maxint alias 15"},
            {dec64_new(10000000000000000L, -16), maxint, one_over_maxint, "one / maxint alias 16"},
            {minnum, two, minnum, "minnum / 2"},
            {one, 0x1437EEECD800000L, dec64_new(28114572543455208L, -31), "1/17!"},
            {one, 0x52D09F700003L, dec64_new(28114572543455208L, -31), "1/17!"},
        });
    }

    public DivideTest(@DEC64 long first, @DEC64 long second, @DEC64 long expected, String comment) {
        this.first = first;
        this.second = second;
        this.expected = expected;
        this.comment = comment;
    }

    @Test
    public void divide() {
        @DEC64 long result = Basic64.divide(first, second);
        System.out.println(String.format("first = 0x%x, second = 0x%x, result = 0x%x", first, second, result));
        assertEquals(comment, expected, result);
    }

    private static @DEC64 long dec64_new(@DEC64 long coeff, @DEC64 long exponent) {
        return Basic64.of(coeff, exponent);
    }
}
