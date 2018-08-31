package brave.propagation;

import brave.internal.HexCodec;
import brave.internal.Nullable;
import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AutoValue
public abstract class TraceContext extends SamplingFlags {
    public static TraceContext.Builder newBuilder() {
        return (new brave.propagation.AutoValue_TraceContext.Builder()).traceIdHigh(0L).debug(false).shared(false).extra(Collections.emptyList());
    }

    public abstract long traceIdHigh();

    public abstract long traceId();

    @Nullable
    public abstract Long parentId();

    @Nullable
    public abstract Boolean sampled();

    public abstract long spanId();

    public abstract boolean shared();

    public abstract List<Object> extra();

    public abstract TraceContext.Builder toBuilder();

    public String traceIdString() {
        char[] result;
        if (this.traceIdHigh() != 0L) {
            result = new char[32];
            HexCodec.writeHexLong(result, 0, this.traceIdHigh());
            HexCodec.writeHexLong(result, 16, this.traceId());
            return new String(result);
        } else {
            result = new char[16];
            HexCodec.writeHexLong(result, 0, this.traceId());
            return new String(result);
        }
    }

    public String toString() {
        boolean traceHi = this.traceIdHigh() != 0L;
        char[] result = new char[(traceHi ? 3 : 2) * 16 + 1];
        int pos = 0;
        if (traceHi) {
            HexCodec.writeHexLong(result, pos, this.traceIdHigh());
            pos += 16;
        }

        HexCodec.writeHexLong(result, pos, this.traceId());
        pos += 16;
        result[pos++] = '/';
        HexCodec.writeHexLong(result, pos, this.spanId());
        return new String(result);
    }

    TraceContext() {
    }

    static List<Object> ensureImmutable(List<Object> extra) {
        if (extra == Collections.EMPTY_LIST) {
            return extra;
        } else {
            return extra.size() == 1 ? Collections.singletonList(extra.get(0)) : Collections.unmodifiableList(new ArrayList(extra));
        }
    }

    public abstract static class Builder {
        public abstract TraceContext.Builder traceIdHigh(long var1);

        public abstract TraceContext.Builder traceId(long var1);

        public abstract TraceContext.Builder parentId(@Nullable Long var1);

        public abstract TraceContext.Builder spanId(long var1);

        public abstract TraceContext.Builder sampled(@Nullable Boolean var1);

        public abstract TraceContext.Builder debug(boolean var1);

        public abstract TraceContext.Builder shared(boolean var1);

        public abstract TraceContext.Builder extra(List<Object> var1);

        abstract List<Object> extra();

        public abstract TraceContext autoBuild();

        public final TraceContext build() {
            return this.extra(TraceContext.ensureImmutable(this.extra())).autoBuild();
        }

        @Nullable
        abstract Boolean sampled();

        abstract boolean debug();

        Builder() {
        }
    }

    public interface Extractor<C> {
        TraceContextOrSamplingFlags extract(C var1);
    }

    public interface Injector<C> {
        void inject(TraceContext var1, C var2);
    }
}

