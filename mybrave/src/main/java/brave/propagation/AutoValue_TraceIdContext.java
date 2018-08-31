package brave.propagation;

import brave.internal.Nullable;

final class AutoValue_TraceIdContext extends TraceIdContext {
    private final boolean debug;
    private final long traceIdHigh;
    private final long traceId;
    private final Boolean sampled;

    private AutoValue_TraceIdContext(boolean debug, long traceIdHigh, long traceId, @Nullable Boolean sampled) {
        this.debug = debug;
        this.traceIdHigh = traceIdHigh;
        this.traceId = traceId;
        this.sampled = sampled;
    }

    public boolean debug() {
        return this.debug;
    }

    public long traceIdHigh() {
        return this.traceIdHigh;
    }

    public long traceId() {
        return this.traceId;
    }

    @Nullable
    public Boolean sampled() {
        return this.sampled;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof TraceIdContext)) {
            return false;
        } else {
            boolean var10000;
            label40: {
                TraceIdContext that = (TraceIdContext)o;
                if (this.debug == that.debug() && this.traceIdHigh == that.traceIdHigh() && this.traceId == that.traceId()) {
                    if (this.sampled == null) {
                        if (that.sampled() == null) {
                            break label40;
                        }
                    } else if (this.sampled.equals(that.sampled())) {
                        break label40;
                    }
                }

                var10000 = false;
                return var10000;
            }

            var10000 = true;
            return var10000;
        }
    }

    public int hashCode() {
        int h = 1;
        h = h * 1000003;
        h ^= this.debug ? 1231 : 1237;
        h *= 1000003;
        h ^= (int)(this.traceIdHigh >>> 32 ^ this.traceIdHigh);
        h *= 1000003;
        h ^= (int)(this.traceId >>> 32 ^ this.traceId);
        h *= 1000003;
        h ^= this.sampled == null ? 0 : this.sampled.hashCode();
        return h;
    }

    public brave.propagation.TraceIdContext.Builder toBuilder() {
        return new AutoValue_TraceIdContext.Builder(this);
    }

    static final class Builder extends brave.propagation.TraceIdContext.Builder {
        private Boolean debug;
        private Long traceIdHigh;
        private Long traceId;
        private Boolean sampled;

        Builder() {
        }

        private Builder(TraceIdContext source) {
            this.debug = source.debug();
            this.traceIdHigh = source.traceIdHigh();
            this.traceId = source.traceId();
            this.sampled = source.sampled();
        }

        public brave.propagation.TraceIdContext.Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public brave.propagation.TraceIdContext.Builder traceIdHigh(long traceIdHigh) {
            this.traceIdHigh = traceIdHigh;
            return this;
        }

        public brave.propagation.TraceIdContext.Builder traceId(long traceId) {
            this.traceId = traceId;
            return this;
        }

        public brave.propagation.TraceIdContext.Builder sampled(@Nullable Boolean sampled) {
            this.sampled = sampled;
            return this;
        }

        public TraceIdContext build() {
            String missing = "";
            if (this.debug == null) {
                missing = missing + " debug";
            }

            if (this.traceIdHigh == null) {
                missing = missing + " traceIdHigh";
            }

            if (this.traceId == null) {
                missing = missing + " traceId";
            }

            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing required properties:" + missing);
            } else {
                return new AutoValue_TraceIdContext(this.debug, this.traceIdHigh, this.traceId, this.sampled);
            }
        }
    }
}
