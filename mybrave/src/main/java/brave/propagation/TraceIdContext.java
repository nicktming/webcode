package brave.propagation;

import brave.internal.HexCodec;
import brave.internal.Nullable;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TraceIdContext extends SamplingFlags {
    public static TraceIdContext.Builder newBuilder() {
        return (new brave.propagation.AutoValue_TraceIdContext.Builder()).traceIdHigh(0L).debug(false);
    }

    public abstract long traceIdHigh();

    public abstract long traceId();

    @Nullable
    public abstract Boolean sampled();

    public abstract TraceIdContext.Builder toBuilder();

    public String toString() {
        boolean traceHi = this.traceIdHigh() != 0L;
        char[] result = new char[traceHi ? 32 : 16];
        int pos = 0;
        if (traceHi) {
            HexCodec.writeHexLong(result, pos, this.traceIdHigh());
            pos += 16;
        }

        HexCodec.writeHexLong(result, pos, this.traceId());
        return new String(result);
    }

    TraceIdContext() {
    }

    public abstract static class Builder {
        public abstract TraceIdContext.Builder traceIdHigh(long var1);

        public abstract TraceIdContext.Builder traceId(long var1);

        public abstract TraceIdContext.Builder sampled(@Nullable Boolean var1);

        public abstract TraceIdContext.Builder debug(boolean var1);

        public abstract TraceIdContext build();

        Builder() {
        }
    }
}

