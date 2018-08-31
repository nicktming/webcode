package brave;

import brave.internal.recorder.Recorder;
import brave.propagation.TraceContext;

final class AutoValue_RealSpan extends RealSpan {
    private final TraceContext context;
    private final Clock clock;
    private final Recorder recorder;

    AutoValue_RealSpan(TraceContext context, Clock clock, Recorder recorder) {
        if (context == null) {
            throw new NullPointerException("Null context");
        } else {
            this.context = context;
            if (clock == null) {
                throw new NullPointerException("Null clock");
            } else {
                this.clock = clock;
                if (recorder == null) {
                    throw new NullPointerException("Null recorder");
                } else {
                    this.recorder = recorder;
                }
            }
        }
    }

    public TraceContext context() {
        return this.context;
    }

    Clock clock() {
        return this.clock;
    }

    Recorder recorder() {
        return this.recorder;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof RealSpan)) {
            return false;
        } else {
            RealSpan that = (RealSpan)o;
            return this.context.equals(that.context()) && this.clock.equals(that.clock()) && this.recorder.equals(that.recorder());
        }
    }

    public int hashCode() {
        int h = 1;
        h = h * 1000003;
        h ^= this.context.hashCode();
        h *= 1000003;
        h ^= this.clock.hashCode();
        h *= 1000003;
        h ^= this.recorder.hashCode();
        return h;
    }
}

