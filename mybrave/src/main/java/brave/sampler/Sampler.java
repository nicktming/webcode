package brave.sampler;

public abstract class Sampler {
    public static final Sampler ALWAYS_SAMPLE = new Sampler() {
        public boolean isSampled(long traceId) {
            return true;
        }

        public String toString() {
            return "AlwaysSample";
        }
    };
    public static final Sampler NEVER_SAMPLE = new Sampler() {
        public boolean isSampled(long traceId) {
            return false;
        }

        public String toString() {
            return "NeverSample";
        }
    };

    public Sampler() {
    }

    public abstract boolean isSampled(long var1);

    public static Sampler create(float rate) {
        return CountingSampler.create(rate);
    }
}