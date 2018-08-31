package brave;

import brave.internal.Nullable;
import brave.internal.Platform;
import brave.internal.recorder.Recorder;
import brave.propagation.CurrentTraceContext;
import brave.propagation.SamplingFlags;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import brave.propagation.TraceIdContext;
import brave.propagation.CurrentTraceContext.Scope;
import brave.sampler.Sampler;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import zipkin.Endpoint;
//import zipkin.reporter.Reporter;

public final class Tracer {
    final Clock clock;
    final Recorder recorder;
    final Sampler sampler;
    final CurrentTraceContext currentTraceContext;
    final boolean traceId128Bit;
    final AtomicBoolean noop;
    final boolean supportsJoin;

    /** @deprecated */
    @Deprecated
    public static Tracer.Builder newBuilder() {
        return new Tracer.Builder();
    }

    Tracer(brave.Tracing.Builder builder, AtomicBoolean noop) {
        this.noop = noop;
        this.supportsJoin = builder.supportsJoin && builder.propagationFactory.supportsJoin();
        this.clock = builder.clock;
        this.recorder = new Recorder(builder.localEndpoint, this.clock, builder.reporter, this.noop);
        this.sampler = builder.sampler;
        this.currentTraceContext = builder.currentTraceContext;
        this.traceId128Bit = builder.traceId128Bit || builder.propagationFactory.requires128BitTraceId();
    }

    /** @deprecated */
    @Deprecated
    public Clock clock() {
        return this.clock;
    }

    public Span newTrace() {
        return this.toSpan(this.newRootContext(SamplingFlags.EMPTY, Collections.emptyList()));
    }

    public final Span joinSpan(TraceContext context) {
        if (context == null) {
            throw new NullPointerException("context == null");
        } else if (!this.supportsJoin) {
            return this.newChild(context);
        } else {
            brave.propagation.TraceContext.Builder builder = context.toBuilder();
            if (context.sampled() == null) {
                builder.sampled(this.sampler.isSampled(context.traceId()));
            } else {
                builder.shared(true);
            }

            return this.toSpan(builder.build());
        }
    }

    public Span newChild(TraceContext parent) {
        if (parent == null) {
            throw new NullPointerException("parent == null");
        } else {
            return this.nextSpan(TraceContextOrSamplingFlags.create(parent));
        }
    }

    public Span nextSpan(TraceContextOrSamplingFlags extracted) {
        TraceContext parent = extracted.context();
        if (extracted.samplingFlags() != null) {
            TraceContext implicitParent = this.currentTraceContext.get();
            if (implicitParent == null) {
                return this.toSpan(this.newRootContext(extracted.samplingFlags(), extracted.extra()));
            }

            parent = appendExtra(implicitParent, extracted.extra());
        }

        long nextId = Platform.get().randomLong();
        if (parent != null) {
            return this.toSpan(parent.toBuilder().spanId(nextId).parentId(parent.spanId()).shared(false).build());
        } else {
            TraceIdContext traceIdContext = extracted.traceIdContext();
            if (extracted.traceIdContext() != null) {
                Boolean sampled = traceIdContext.sampled();
                if (sampled == null) {
                    sampled = this.sampler.isSampled(traceIdContext.traceId());
                }

                return this.toSpan(TraceContext.newBuilder().sampled(sampled).debug(traceIdContext.debug()).traceIdHigh(traceIdContext.traceIdHigh()).traceId(traceIdContext.traceId()).spanId(nextId).extra(extracted.extra()).build());
            } else {
                throw new AssertionError("should not reach here");
            }
        }
    }

    public Span newTrace(SamplingFlags samplingFlags) {
        return this.toSpan(this.newRootContext(samplingFlags, Collections.emptyList()));
    }

    public Span toSpan(TraceContext context) {
        if (context == null) {
            throw new NullPointerException("context == null");
        } else {
            return (Span)(!this.noop.get() && Boolean.TRUE.equals(context.sampled()) ? RealSpan.create(context, this.clock, this.recorder) : NoopSpan.create(context));
        }
    }

    TraceContext newRootContext(SamplingFlags samplingFlags, List<Object> extra) {
        long nextId = Platform.get().randomLong();
        Boolean sampled = samplingFlags.sampled();
        if (sampled == null) {
            sampled = this.sampler.isSampled(nextId);
        }

        return TraceContext.newBuilder().sampled(sampled).traceIdHigh(this.traceId128Bit ? Platform.get().nextTraceIdHigh() : 0L).traceId(nextId).spanId(nextId).debug(samplingFlags.debug()).extra(extra).build();
    }

    public Tracer.SpanInScope withSpanInScope(@Nullable Span span) {
        return new Tracer.SpanInScope(this.currentTraceContext.newScope(span != null ? span.context() : null));
    }

    @Nullable
    public Span currentSpan() {
        TraceContext currentContext = this.currentTraceContext.get();
        return currentContext != null ? this.toSpan(currentContext) : null;
    }

    public Span nextSpan() {
        TraceContext parent = this.currentTraceContext.get();
        return parent == null ? this.newTrace() : this.newChild(parent);
    }

    static TraceContext appendExtra(TraceContext context, List<Object> extra) {
        if (extra.isEmpty()) {
            return context;
        } else if (context.extra().isEmpty()) {
            return context.toBuilder().extra(extra).build();
        } else {
            List<Object> merged = new ArrayList(context.extra());
            merged.addAll(extra);
            return context.toBuilder().extra(merged).build();
        }
    }

    public static final class SpanInScope implements Closeable {
        final Scope scope;

        SpanInScope(Scope scope) {
            if (scope == null) {
                throw new NullPointerException("scope == null");
            } else {
                this.scope = scope;
            }
        }

        public void close() {
            this.scope.close();
        }

        public String toString() {
            return this.scope.toString();
        }
    }

    /** @deprecated */
    @Deprecated
    public static final class Builder {
        final brave.Tracing.Builder delegate = new brave.Tracing.Builder();

        public Builder() {
        }

        public Tracer.Builder localServiceName(String localServiceName) {
            this.delegate.localServiceName(localServiceName);
            return this;
        }

        /** @deprecated */
        @Deprecated
        public Tracer.Builder localEndpoint(Endpoint localEndpoint) {
            return this.localEndpoint(localEndpoint.toV2());
        }

        public Tracer.Builder localEndpoint(zipkin2.Endpoint localEndpoint) {
            this.delegate.localEndpoint(localEndpoint);
            return this;
        }

        /** @deprecated */
        /*
        @Deprecated
        public Tracer.Builder reporter(Reporter<zipkin.Span> reporter) {
            this.delegate.reporter(reporter);
            return this;
        }
        */

        public Tracer.Builder spanReporter(Reporter<zipkin2.Span> reporter) {
            this.delegate.spanReporter(reporter);
            return this;
        }

        public Tracer.Builder clock(Clock clock) {
            this.delegate.clock(clock);
            return this;
        }

        public Tracer.Builder sampler(Sampler sampler) {
            this.delegate.sampler(sampler);
            return this;
        }

        public Tracer.Builder currentTraceContext(CurrentTraceContext currentTraceContext) {
            this.delegate.currentTraceContext(currentTraceContext);
            return this;
        }

        public Tracer.Builder traceId128Bit(boolean traceId128Bit) {
            this.delegate.traceId128Bit(traceId128Bit);
            return this;
        }

        public Tracer.Builder supportsJoin(boolean supportsJoin) {
            this.delegate.supportsJoin(supportsJoin);
            return this;
        }

        public Tracer build() {
            return this.delegate.build().tracer();
        }
    }
}
