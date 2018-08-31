package brave;

import brave.propagation.TraceContext;

final class AutoValue_NoopSpan extends NoopSpan {
    private final TraceContext context;

    AutoValue_NoopSpan(TraceContext context) {
        if (context == null) {
            throw new NullPointerException("Null context");
        } else {
            this.context = context;
        }
    }

    public TraceContext context() {
        return this.context;
    }

    public String toString() {
        return "NoopSpan{context=" + this.context + "}";
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof NoopSpan) {
            NoopSpan that = (NoopSpan)o;
            return this.context.equals(that.context());
        } else {
            return false;
        }
    }

    public int hashCode() {
        int h = 1;
        h = h * 1000003;
        h ^= this.context.hashCode();
        return h;
    }
}