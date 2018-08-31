package brave.internal;

import brave.Tracer;
import brave.propagation.TraceContext;

public abstract class Internal {
    public static Internal instance;

    public Internal() {
    }

    @Nullable
    public abstract Long timestamp(Tracer var1, TraceContext var2);
}