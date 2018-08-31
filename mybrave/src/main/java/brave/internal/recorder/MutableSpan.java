package brave.internal.recorder;

import brave.Span.Kind;
import brave.internal.HexCodec;
import brave.internal.Nullable;
import brave.propagation.TraceContext;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.Span.Builder;

final class MutableSpan {
    final Builder span;
    boolean finished;
    long timestamp;

    MutableSpan(TraceContext context, Endpoint localEndpoint) {
        this.span = Span.newBuilder().traceId(context.traceIdString()).parentId(context.parentId() != null ? HexCodec.toLowerHex(context.parentId()) : null).id(HexCodec.toLowerHex(context.spanId())).debug(context.debug()).shared(context.shared()).localEndpoint(localEndpoint);
        this.finished = false;
    }

    synchronized MutableSpan start(long timestamp) {
        this.span.timestamp(this.timestamp = timestamp);
        return this;
    }

    synchronized MutableSpan name(String name) {
        this.span.name(name);
        return this;
    }

    synchronized MutableSpan kind(Kind kind) {
        try {
            this.span.kind(zipkin2.Span.Kind.valueOf(kind.name()));
        } catch (IllegalArgumentException var3) {
            ;
        }

        return this;
    }

    synchronized MutableSpan annotate(long timestamp, String value) {
        if ("cs".equals(value)) {
            this.span.kind(zipkin2.Span.Kind.CLIENT).timestamp(this.timestamp = timestamp);
        } else if ("sr".equals(value)) {
            this.span.kind(zipkin2.Span.Kind.SERVER).timestamp(this.timestamp = timestamp);
        } else if ("cr".equals(value)) {
            this.span.kind(zipkin2.Span.Kind.CLIENT);
            this.finish(timestamp);
        } else if ("ss".equals(value)) {
            this.span.kind(zipkin2.Span.Kind.SERVER);
            this.finish(timestamp);
        } else {
            this.span.addAnnotation(timestamp, value);
        }

        return this;
    }

    synchronized MutableSpan tag(String key, String value) {
        this.span.putTag(key, value);
        return this;
    }

    synchronized MutableSpan remoteEndpoint(Endpoint remoteEndpoint) {
        this.span.remoteEndpoint(remoteEndpoint);
        return this;
    }

    synchronized MutableSpan finish(@Nullable Long finishTimestamp) {
        if (this.finished) {
            return this;
        } else {
            this.finished = true;
            if (this.timestamp != 0L && finishTimestamp != null) {
                this.span.duration(Math.max(finishTimestamp - this.timestamp, 1L));
            }

            return this;
        }
    }

    synchronized Span toSpan() {
        return this.span.build();
    }
}
