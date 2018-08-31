package brave.propagation;

import brave.internal.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TraceContextOrSamplingFlags {
    public static final TraceContextOrSamplingFlags EMPTY;
    final int type;
    final SamplingFlags value;
    final List<Object> extra;

    public static TraceContextOrSamplingFlags.Builder newBuilder() {
        return new TraceContextOrSamplingFlags.Builder();
    }

    @Nullable
    public Boolean sampled() {
        return this.value.sampled();
    }

    public TraceContextOrSamplingFlags sampled(@Nullable Boolean sampled) {
        switch(this.type) {
            case 1:
                return new TraceContextOrSamplingFlags(this.type, ((TraceContext)this.value).toBuilder().sampled(sampled).build(), this.extra);
            case 2:
                return new TraceContextOrSamplingFlags(this.type, ((TraceIdContext)this.value).toBuilder().sampled(sampled).build(), this.extra);
            case 3:
                return new TraceContextOrSamplingFlags(this.type, (new brave.propagation.SamplingFlags.Builder()).sampled(sampled).debug(this.value.debug()).build(), this.extra);
            default:
                throw new AssertionError("programming error");
        }
    }

    @Nullable
    public TraceContext context() {
        return this.type == 1 ? (TraceContext)this.value : null;
    }

    @Nullable
    public TraceIdContext traceIdContext() {
        return this.type == 2 ? (TraceIdContext)this.value : null;
    }

    @Nullable
    public SamplingFlags samplingFlags() {
        return this.type == 3 ? this.value : null;
    }

    public final List<Object> extra() {
        return this.extra;
    }

    public final TraceContextOrSamplingFlags.Builder toBuilder() {
        TraceContextOrSamplingFlags.Builder result = new TraceContextOrSamplingFlags.Builder();
        result.type = this.type;
        result.value = this.value;
        result.extra = this.extra;
        return result;
    }

    public String toString() {
        return "{value=" + this.value + ", extra=" + this.extra + "}";
    }

    public static TraceContextOrSamplingFlags create(TraceContext context) {
        return new TraceContextOrSamplingFlags(1, context, Collections.emptyList());
    }

    public static TraceContextOrSamplingFlags create(TraceIdContext traceIdContext) {
        return new TraceContextOrSamplingFlags(2, traceIdContext, Collections.emptyList());
    }

    public static TraceContextOrSamplingFlags create(SamplingFlags flags) {
        return new TraceContextOrSamplingFlags(3, flags, Collections.emptyList());
    }

    /** @deprecated */
    @Deprecated
    public static TraceContextOrSamplingFlags create(brave.propagation.TraceContext.Builder builder) {
        if (builder == null) {
            throw new NullPointerException("builder == null");
        } else {
            try {
                return create(builder.build());
            } catch (IllegalStateException var3) {
                SamplingFlags flags = (new brave.propagation.SamplingFlags.Builder()).sampled(builder.sampled()).debug(builder.debug()).build();
                return create(flags);
            }
        }
    }

    TraceContextOrSamplingFlags(int type, SamplingFlags value, List<Object> extra) {
        if (value == null) {
            throw new NullPointerException("value == null");
        } else if (extra == null) {
            throw new NullPointerException("extra == null");
        } else {
            this.type = type;
            this.value = value;
            this.extra = TraceContext.ensureImmutable(extra);
        }
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof TraceContextOrSamplingFlags)) {
            return false;
        } else {
            TraceContextOrSamplingFlags that = (TraceContextOrSamplingFlags)o;
            return this.type == that.type && this.value.equals(that.value) && this.extra.equals(that.extra);
        }
    }

    public int hashCode() {
        int h = 1;
        h = h * 1000003;
        h ^= this.type;
        h *= 1000003;
        h ^= this.value.hashCode();
        h *= 1000003;
        h ^= this.extra.hashCode();
        return h;
    }

    static {
        EMPTY = create(SamplingFlags.EMPTY);
    }

    public static final class Builder {
        int type;
        SamplingFlags value;
        List<Object> extra = Collections.emptyList();

        public final TraceContextOrSamplingFlags.Builder context(TraceContext context) {
            if (context == null) {
                throw new NullPointerException("context == null");
            } else {
                this.type = 1;
                this.value = context;
                return this;
            }
        }

        public final TraceContextOrSamplingFlags.Builder traceIdContext(TraceIdContext traceIdContext) {
            if (traceIdContext == null) {
                throw new NullPointerException("traceIdContext == null");
            } else {
                this.type = 2;
                this.value = traceIdContext;
                return this;
            }
        }

        public final TraceContextOrSamplingFlags.Builder samplingFlags(SamplingFlags samplingFlags) {
            if (samplingFlags == null) {
                throw new NullPointerException("samplingFlags == null");
            } else {
                this.type = 3;
                this.value = samplingFlags;
                return this;
            }
        }

        public final TraceContextOrSamplingFlags.Builder extra(List<Object> extra) {
            if (extra == null) {
                throw new NullPointerException("extra == null");
            } else {
                this.extra = extra;
                return this;
            }
        }

        public final TraceContextOrSamplingFlags.Builder addExtra(Object extra) {
            if (extra == null) {
                throw new NullPointerException("extra == null");
            } else {
                if (!(this.extra instanceof ArrayList)) {
                    this.extra = new ArrayList(this.extra);
                }

                this.extra.add(extra);
                return this;
            }
        }

        public final TraceContextOrSamplingFlags build() {
            if (!this.extra.isEmpty() && this.type == 1) {
                TraceContext context = (TraceContext)this.value;
                if (context.extra().isEmpty()) {
                    context = context.toBuilder().extra(this.extra).build();
                    return new TraceContextOrSamplingFlags(this.type, context, Collections.emptyList());
                } else {
                    ArrayList<Object> copy = new ArrayList(this.extra);
                    copy.addAll(context.extra());
                    context = context.toBuilder().extra(copy).build();
                    return new TraceContextOrSamplingFlags(this.type, context, Collections.emptyList());
                }
            } else {
                return new TraceContextOrSamplingFlags(this.type, this.value, this.extra);
            }
        }

        Builder() {
        }
    }
}

