package brave;

import brave.Span.Kind;
import brave.propagation.TraceContext;
import com.google.auto.value.AutoValue;
import zipkin2.Endpoint;

@AutoValue
abstract class NoopSpan extends Span {
    NoopSpan() {
    }

    static NoopSpan create(TraceContext context) {
        return new AutoValue_NoopSpan(context);
    }

    public boolean isNoop() {
        return true;
    }

    public Span start() {
        return this;
    }

    public Span start(long timestamp) {
        return this;
    }

    public Span name(String name) {
        return this;
    }

    public Span kind(Kind kind) {
        return this;
    }

    public Span annotate(String value) {
        return this;
    }

    public Span annotate(long timestamp, String value) {
        return this;
    }

    public Span remoteEndpoint(Endpoint endpoint) {
        return this;
    }

    public Span tag(String key, String value) {
        return this;
    }

    public void finish() {
    }

    public void finish(long timestamp) {
    }

    public void abandon() {
    }

    public void flush() {
    }
}

