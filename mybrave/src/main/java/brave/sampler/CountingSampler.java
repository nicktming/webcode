package brave.sampler;

import java.util.BitSet;
import java.util.Random;

public final class CountingSampler extends Sampler {
    private int i;
    private final BitSet sampleDecisions;

    public static Sampler create(float rate) {
        if (rate == 0.0F) {
            return NEVER_SAMPLE;
        } else if ((double)rate == 1.0D) {
            return ALWAYS_SAMPLE;
        } else if (rate >= 0.01F && rate <= 1.0F) {
            return new CountingSampler(rate);
        } else {
            throw new IllegalArgumentException("rate should be between 0.01 and 1: was " + rate);
        }
    }

    CountingSampler(float rate) {
        int outOf100 = (int)(rate * 100.0F);
        this.sampleDecisions = randomBitSet(100, outOf100, new Random());
    }

    public synchronized boolean isSampled(long traceIdIgnored) {
        boolean result = this.sampleDecisions.get(this.i++);
        if (this.i == 100) {
            this.i = 0;
        }

        return result;
    }

    public String toString() {
        return "CountingSampler()";
    }

    static BitSet randomBitSet(int size, int cardinality, Random rnd) {
        BitSet result = new BitSet(size);
        int[] chosen = new int[cardinality];

        int i;
        for(i = 0; i < cardinality; ++i) {
            chosen[i] = i;
            result.set(i);
        }

        for(; i < size; ++i) {
            int j = rnd.nextInt(i + 1);
            if (j < cardinality) {
                result.clear(chosen[j]);
                result.set(i);
                chosen[j] = i;
            }
        }

        return result;
    }
}
