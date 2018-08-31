package brave.internal.zipkin;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public final class Util {
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    static final char[] URL_MAP = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};
    static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static int envOr(String key, int fallback) {
        return System.getenv(key) != null ? Integer.parseInt(System.getenv(key)) : fallback;
    }

    public static String envOr(String key, String fallback) {
        return System.getenv(key) != null ? System.getenv(key) : fallback;
    }

    public static boolean equal(Object a, Object b) {
        return a == b || a != null && a.equals(b);
    }

    public static void checkArgument(boolean expression, String errorMessageTemplate, Object... errorMessageArgs) {
        if (!expression) {
            throw new IllegalArgumentException(String.format(errorMessageTemplate, errorMessageArgs));
        }
    }

    public static <T> T checkNotNull(T reference, String errorMessage) {
        if (reference == null) {
            throw new NullPointerException(errorMessage);
        } else {
            return reference;
        }
    }

    public static <T extends Comparable<? super T>> List<T> sortedList(Collection<T> in) {
        if (in != null && !in.isEmpty()) {
            if (in.size() == 1) {
                //return Collections.singletonList((Comparable)in.iterator().next());
                return Collections.singletonList(in.iterator().next());
            } else {
                Object[] array = in.toArray();
                Arrays.sort(array);
                List result = Arrays.asList(array);
                return Collections.unmodifiableList(result);
            }
        } else {
            return Collections.emptyList();
        }
    }

    public static long midnightUTC(long epochMillis) {
        Calendar day = Calendar.getInstance(UTC);
        day.setTimeInMillis(epochMillis);
        day.set(14, 0);
        day.set(13, 0);
        day.set(12, 0);
        day.set(11, 0);
        return day.getTimeInMillis();
    }

    public static List<Date> getDays(long endTs, Long lookback) {
        long to = midnightUTC(endTs);
        long startMillis = endTs - (lookback != null ? lookback : endTs);
        long from = startMillis <= 0L ? 0L : midnightUTC(startMillis);
        List<Date> days = new ArrayList();

        for(long time = from; time <= to; time += TimeUnit.DAYS.toMillis(1L)) {
            days.add(new Date(time));
        }

        return days;
    }

    public static long lowerHexToUnsignedLong(String lowerHex) {
        int length = lowerHex.length();
        if (length >= 1 && length <= 32) {
            int beginIndex = length > 16 ? length - 16 : 0;
            return lowerHexToUnsignedLong(lowerHex, beginIndex);
        } else {
            throw isntLowerHexLong(lowerHex);
        }
    }

    public static long lowerHexToUnsignedLong(String lowerHex, int index) {
        long result = 0L;

        for(int endIndex = Math.min(index + 16, lowerHex.length()); index < endIndex; ++index) {
            char c = lowerHex.charAt(index);
            result <<= 4;
            if (c >= '0' && c <= '9') {
                result |= (long)(c - 48);
            } else {
                if (c < 'a' || c > 'f') {
                    throw isntLowerHexLong(lowerHex);
                }

                result |= (long)(c - 97 + 10);
            }
        }

        return result;
    }

    static NumberFormatException isntLowerHexLong(String lowerHex) {
        throw new NumberFormatException(lowerHex + " should be a 1 to 32 character lower-hex string with no prefix");
    }

    public static String toLowerHex(long high, long low) {
        char[] result = new char[high != 0L ? 32 : 16];
        int pos = 0;
        if (high != 0L) {
            writeHexLong(result, pos, high);
            pos += 16;
        }

        writeHexLong(result, pos, low);
        return new String(result);
    }

    public static String toLowerHex(long v) {
        char[] data = new char[16];
        writeHexLong(data, 0, v);
        return new String(data);
    }

    static String writeBase64Url(byte[] in) {
        char[] result = new char[(in.length + 2) / 3 * 4];
        int end = in.length - in.length % 3;
        int pos = 0;

        for(int i = 0; i < end; i += 3) {
            result[pos++] = URL_MAP[(in[i] & 255) >> 2];
            result[pos++] = URL_MAP[(in[i] & 3) << 4 | (in[i + 1] & 255) >> 4];
            result[pos++] = URL_MAP[(in[i + 1] & 15) << 2 | (in[i + 2] & 255) >> 6];
            result[pos++] = URL_MAP[in[i + 2] & 63];
        }

        switch(in.length % 3) {
            case 1:
                result[pos++] = URL_MAP[(in[end] & 255) >> 2];
                result[pos++] = URL_MAP[(in[end] & 3) << 4];
                result[pos++] = '=';
                result[pos] = '=';
                break;
            case 2:
                result[pos++] = URL_MAP[(in[end] & 255) >> 2];
                result[pos++] = URL_MAP[(in[end] & 3) << 4 | (in[end + 1] & 255) >> 4];
                result[pos++] = URL_MAP[(in[end + 1] & 15) << 2];
                result[pos] = '=';
        }

        return new String(result);
    }

    public static void writeHexLong(char[] data, int pos, long v) {
        writeHexByte(data, pos + 0, (byte)((int)(v >>> 56 & 255L)));
        writeHexByte(data, pos + 2, (byte)((int)(v >>> 48 & 255L)));
        writeHexByte(data, pos + 4, (byte)((int)(v >>> 40 & 255L)));
        writeHexByte(data, pos + 6, (byte)((int)(v >>> 32 & 255L)));
        writeHexByte(data, pos + 8, (byte)((int)(v >>> 24 & 255L)));
        writeHexByte(data, pos + 10, (byte)((int)(v >>> 16 & 255L)));
        writeHexByte(data, pos + 12, (byte)((int)(v >>> 8 & 255L)));
        writeHexByte(data, pos + 14, (byte)((int)(v & 255L)));
    }

    public static void propagateIfFatal(Throwable t) {
        if (t instanceof VirtualMachineError) {
            throw (VirtualMachineError)t;
        } else if (t instanceof ThreadDeath) {
            throw (ThreadDeath)t;
        } else if (t instanceof LinkageError) {
            throw (LinkageError)t;
        }
    }

    static void writeHexByte(char[] data, int pos, byte b) {
        data[pos + 0] = HEX_DIGITS[b >> 4 & 15];
        data[pos + 1] = HEX_DIGITS[b & 15];
    }

    static AssertionError assertionError(String message, Throwable cause) {
        AssertionError error = new AssertionError(message);
        error.initCause(cause);
        throw error;
    }

    private Util() {
    }
}
