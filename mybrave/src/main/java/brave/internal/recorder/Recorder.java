package brave.internal.recorder;

import brave.Clock;
import brave.Span.Kind;
import brave.internal.Nullable;
import brave.propagation.TraceContext;
import java.util.concurrent.atomic.AtomicBoolean;
import zipkin2.Endpoint;
import zipkin2.Span;

public final class Recorder {
    final MutableSpanMap spanMap;
    final Reporter<Span> reporter;
    final AtomicBoolean noop;

    public Recorder(Endpoint localEndpoint, Clock clock, Reporter<Span> reporter, AtomicBoolean noop) {
        this.spanMap = new MutableSpanMap(localEndpoint, clock, reporter, noop);
        this.reporter = reporter;
        this.noop = noop;
    }

    @Nullable
    public Long timestamp(TraceContext context) {
        MutableSpan span = this.spanMap.get(context);
        if (span == null) {
            return null;
        } else {
            return span.timestamp == 0L ? null : span.timestamp;
        }
    }

    public void start(TraceContext context, long timestamp) {
        if (!this.noop.get()) {
            this.spanMap.getOrCreate(context).start(timestamp);
        }
    }

    public void name(TraceContext context, String name) {
        if (!this.noop.get()) {
            if (name == null) {
                throw new NullPointerException("name == null");
            } else {
                this.spanMap.getOrCreate(context).name(name);
            }
        }
    }

    public void kind(TraceContext context, Kind kind) {
        if (!this.noop.get()) {
            if (kind == null) {
                throw new NullPointerException("kind == null");
            } else {
                this.spanMap.getOrCreate(context).kind(kind);
            }
        }
    }

    public void annotate(TraceContext context, long timestamp, String value) {
        if (!this.noop.get()) {
            if (value == null) {
                throw new NullPointerException("value == null");
            } else {
                this.spanMap.getOrCreate(context).annotate(timestamp, value);
            }
        }
    }

    public void tag(TraceContext context, String key, String value) {
        if (!this.noop.get()) {
            if (key == null) {
                throw new NullPointerException("key == null");
            } else if (key.isEmpty()) {
                throw new IllegalArgumentException("key is empty");
            } else if (value == null) {
                throw new NullPointerException("value == null");
            } else {
                this.spanMap.getOrCreate(context).tag(key, value);
            }
        }
    }

    public void remoteEndpoint(TraceContext context, Endpoint remoteEndpoint) {
        if (!this.noop.get()) {
            if (remoteEndpoint == null) {
                throw new NullPointerException("remoteEndpoint == null");
            } else {
                this.spanMap.getOrCreate(context).remoteEndpoint(remoteEndpoint);
            }
        }
    }

    public void finish(TraceContext context, long finishTimestamp) {
        MutableSpan span = this.spanMap.remove(context);
        if (span != null && !this.noop.get()) {
            synchronized(span) {
                span.finish(finishTimestamp);
                this.reporter.report(span.toSpan());
            }
        }
    }

    public void abandon(TraceContext context) {
        this.spanMap.remove(context);
    }

    public void flush(TraceContext context) {
        MutableSpan span = this.spanMap.remove(context);
        if (span != null && !this.noop.get()) {
            synchronized(span) {
                span.finish((Long)null);
                this.reporter.report(span.toSpan());
            }
        }
    }
}
