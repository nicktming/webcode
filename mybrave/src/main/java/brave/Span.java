package brave;

import brave.propagation.TraceContext;
import zipkin.Endpoint;

public abstract class Span implements SpanCustomizer {
    public Span() {
    }

    public abstract boolean isNoop();

    public abstract TraceContext context();

    public abstract Span start();

    public abstract Span start(long var1);

    public abstract Span name(String var1);

    public abstract Span kind(Span.Kind var1);

    public abstract Span annotate(String var1);

    public abstract Span annotate(long var1, String var3);

    public abstract Span tag(String var1, String var2);

    /** @deprecated */
    @Deprecated
    public final Span remoteEndpoint(Endpoint endpoint) {
        return this.isNoop() ? this : this.remoteEndpoint(endpoint.toV2());
    }

    public abstract Span remoteEndpoint(zipkin2.Endpoint var1);

    public abstract void finish();

    public abstract void abandon();

    public abstract void finish(long var1);

    public abstract void flush();

    public static enum Kind {
        CLIENT,
        SERVER,
        PRODUCER,
        CONSUMER;

        private Kind() {
        }
    }
}

