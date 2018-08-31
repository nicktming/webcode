package brave;

import brave.Span.Kind;
import brave.internal.recorder.Recorder;
import brave.propagation.TraceContext;
import com.google.auto.value.AutoValue;
import zipkin2.Endpoint;

@AutoValue
abstract class RealSpan extends Span {
    RealSpan() {
    }

    abstract Clock clock();

    abstract Recorder recorder();

    static RealSpan create(TraceContext context, Clock clock, Recorder recorder) {
        return new AutoValue_RealSpan(context, clock, recorder);
    }

    public boolean isNoop() {
        return false;
    }

    public Span start() {
        return this.start(this.clock().currentTimeMicroseconds());
    }

    public Span start(long timestamp) {
        this.recorder().start(this.context(), timestamp);
        return this;
    }

    public Span name(String name) {
        this.recorder().name(this.context(), name);
        return this;
    }

    public Span kind(Kind kind) {
        this.recorder().kind(this.context(), kind);
        return this;
    }

    public Span annotate(String value) {
        return this.annotate(this.clock().currentTimeMicroseconds(), value);
    }

    public Span annotate(long timestamp, String value) {
        this.recorder().annotate(this.context(), timestamp, value);
        return this;
    }

    public Span tag(String key, String value) {
        this.recorder().tag(this.context(), key, value);
        return this;
    }

    public Span remoteEndpoint(Endpoint remoteEndpoint) {
        this.recorder().remoteEndpoint(this.context(), remoteEndpoint);
        return this;
    }

    public void finish() {
        this.finish(this.clock().currentTimeMicroseconds());
    }

    public void finish(long timestamp) {
        this.recorder().finish(this.context(), timestamp);
    }

    public void abandon() {
        this.recorder().abandon(this.context());
    }

    public void flush() {
        this.recorder().flush(this.context());
    }

    public String toString() {
        return "RealSpan(" + this.context() + ")";
    }
}
