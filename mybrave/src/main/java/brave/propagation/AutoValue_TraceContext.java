package brave.propagation;

import brave.internal.Nullable;
import java.util.List;

final class AutoValue_TraceContext extends TraceContext {
    private final boolean debug;
    private final long traceIdHigh;
    private final long traceId;
    private final Long parentId;
    private final Boolean sampled;
    private final long spanId;
    private final boolean shared;
    private final List<Object> extra;

    private AutoValue_TraceContext(boolean debug, long traceIdHigh, long traceId, @Nullable Long parentId, @Nullable Boolean sampled, long spanId, boolean shared, List<Object> extra) {
        this.debug = debug;
        this.traceIdHigh = traceIdHigh;
        this.traceId = traceId;
        this.parentId = parentId;
        this.sampled = sampled;
        this.spanId = spanId;
        this.shared = shared;
        this.extra = extra;
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
    public Long parentId() {
        return this.parentId;
    }

    @Nullable
    public Boolean sampled() {
        return this.sampled;
    }

    public long spanId() {
        return this.spanId;
    }

    public boolean shared() {
        return this.shared;
    }

    public List<Object> extra() {
        return this.extra;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof TraceContext)) {
            return false;
        } else {
            TraceContext that = (TraceContext)o;
            boolean var10000;
            if (this.debug == that.debug() && this.traceIdHigh == that.traceIdHigh() && this.traceId == that.traceId()) {
                label49: {
                    if (this.parentId == null) {
                        if (that.parentId() != null) {
                            break label49;
                        }
                    } else if (!this.parentId.equals(that.parentId())) {
                        break label49;
                    }

                    if (this.sampled == null) {
                        if (that.sampled() != null) {
                            break label49;
                        }
                    } else if (!this.sampled.equals(that.sampled())) {
                        break label49;
                    }

                    if (this.spanId == that.spanId() && this.shared == that.shared() && this.extra.equals(that.extra())) {
                        var10000 = true;
                        return var10000;
                    }
                }
            }

            var10000 = false;
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
        h ^= this.parentId == null ? 0 : this.parentId.hashCode();
        h *= 1000003;
        h ^= this.sampled == null ? 0 : this.sampled.hashCode();
        h *= 1000003;
        h ^= (int)(this.spanId >>> 32 ^ this.spanId);
        h *= 1000003;
        h ^= this.shared ? 1231 : 1237;
        h *= 1000003;
        h ^= this.extra.hashCode();
        return h;
    }

    public brave.propagation.TraceContext.Builder toBuilder() {
        return new AutoValue_TraceContext.Builder(this);
    }

    static final class Builder extends brave.propagation.TraceContext.Builder {
        private Boolean debug;
        private Long traceIdHigh;
        private Long traceId;
        private Long parentId;
        private Boolean sampled;
        private Long spanId;
        private Boolean shared;
        private List<Object> extra;

        Builder() {
        }

        private Builder(TraceContext source) {
            this.debug = source.debug();
            this.traceIdHigh = source.traceIdHigh();
            this.traceId = source.traceId();
            this.parentId = source.parentId();
            this.sampled = source.sampled();
            this.spanId = source.spanId();
            this.shared = source.shared();
            this.extra = source.extra();
        }

        public brave.propagation.TraceContext.Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        boolean debug() {
            if (this.debug == null) {
                throw new IllegalStateException("Property \"debug\" has not been set");
            } else {
                return this.debug;
            }
        }

        public brave.propagation.TraceContext.Builder traceIdHigh(long traceIdHigh) {
            this.traceIdHigh = traceIdHigh;
            return this;
        }

        public brave.propagation.TraceContext.Builder traceId(long traceId) {
            this.traceId = traceId;
            return this;
        }

        public brave.propagation.TraceContext.Builder parentId(@Nullable Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public brave.propagation.TraceContext.Builder sampled(@Nullable Boolean sampled) {
            this.sampled = sampled;
            return this;
        }

        @Nullable
        Boolean sampled() {
            return this.sampled;
        }

        public brave.propagation.TraceContext.Builder spanId(long spanId) {
            this.spanId = spanId;
            return this;
        }

        public brave.propagation.TraceContext.Builder shared(boolean shared) {
            this.shared = shared;
            return this;
        }

        public brave.propagation.TraceContext.Builder extra(List<Object> extra) {
            if (extra == null) {
                throw new NullPointerException("Null extra");
            } else {
                this.extra = extra;
                return this;
            }
        }

        List<Object> extra() {
            if (this.extra == null) {
                throw new IllegalStateException("Property \"extra\" has not been set");
            } else {
                return this.extra;
            }
        }

        public TraceContext autoBuild() {
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

            if (this.spanId == null) {
                missing = missing + " spanId";
            }

            if (this.shared == null) {
                missing = missing + " shared";
            }

            if (this.extra == null) {
                missing = missing + " extra";
            }

            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing required properties:" + missing);
            } else {
                return new AutoValue_TraceContext(this.debug, this.traceIdHigh, this.traceId, this.parentId, this.sampled, this.spanId, this.shared, this.extra);
            }
        }
    }
}
