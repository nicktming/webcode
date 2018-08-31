package brave.propagation;

import brave.internal.Nullable;

public abstract class SamplingFlags {
    public static final SamplingFlags EMPTY = new SamplingFlags.SamplingFlagsImpl((Boolean)null, false);
    public static final SamplingFlags SAMPLED = new SamplingFlags.SamplingFlagsImpl(true, false);
    public static final SamplingFlags NOT_SAMPLED = new SamplingFlags.SamplingFlagsImpl(false, false);
    public static final SamplingFlags DEBUG = new SamplingFlags.SamplingFlagsImpl(true, true);

    @Nullable
    public abstract Boolean sampled();

    public abstract boolean debug();

    SamplingFlags() {
    }

    static final class SamplingFlagsImpl extends SamplingFlags {
        final Boolean sampled;
        final boolean debug;

        SamplingFlagsImpl(Boolean sampled, boolean debug) {
            this.sampled = sampled;
            this.debug = debug;
        }

        public Boolean sampled() {
            return this.sampled;
        }

        public boolean debug() {
            return this.debug;
        }

        public String toString() {
            return "SamplingFlags(sampled=" + this.sampled + ", debug=" + this.debug + ")";
        }
    }

    public static final class Builder {
        Boolean sampled;
        boolean debug = false;

        public Builder() {
        }

        public SamplingFlags.Builder sampled(@Nullable Boolean sampled) {
            this.sampled = sampled;
            return this;
        }

        public SamplingFlags.Builder debug(boolean debug) {
            this.debug = debug;
            if (debug) {
                this.sampled(true);
            }

            return this;
        }

        public static SamplingFlags build(@Nullable Boolean sampled) {
            if (sampled != null) {
                return sampled ? SamplingFlags.SAMPLED : SamplingFlags.NOT_SAMPLED;
            } else {
                return SamplingFlags.EMPTY;
            }
        }

        public SamplingFlags build() {
            return this.debug ? SamplingFlags.DEBUG : build(this.sampled);
        }
    }
}

