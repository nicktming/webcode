package brave.internal.recorder;

import brave.Clock;
import brave.internal.Nullable;
import brave.propagation.TraceContext;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import zipkin2.Endpoint;
import zipkin2.Span;

final class MutableSpanMap extends ReferenceQueue<TraceContext> {
    static final Logger logger = Logger.getLogger(MutableSpanMap.class.getName());
    final ConcurrentMap<Object, MutableSpan> delegate = new ConcurrentHashMap(64);
    final Endpoint localEndpoint;
    final Clock clock;
    final Reporter<Span> reporter;
    final AtomicBoolean noop;

    MutableSpanMap(Endpoint localEndpoint, Clock clock, Reporter<Span> reporter, AtomicBoolean noop) {
        this.localEndpoint = localEndpoint;
        this.clock = clock;
        this.reporter = reporter;
        this.noop = noop;
    }

    @Nullable
    MutableSpan get(TraceContext context) {
        if (context == null) {
            throw new NullPointerException("context == null");
        } else {
            this.reportOrphanedSpans();
            return (MutableSpan)this.delegate.get(new MutableSpanMap.LookupKey(context));
        }
    }

    MutableSpan getOrCreate(TraceContext context) {
        MutableSpan result = this.get(context);
        if (result != null) {
            return result;
        } else {
            MutableSpan newSpan = new MutableSpan(context, this.localEndpoint);
            MutableSpan previousSpan = (MutableSpan)this.delegate.putIfAbsent(new MutableSpanMap.RealKey(context, this), newSpan);
            return previousSpan != null ? previousSpan : newSpan;
        }
    }

    @Nullable
    MutableSpan remove(TraceContext context) {
        if (context == null) {
            throw new NullPointerException("context == null");
        } else {
            MutableSpan result = (MutableSpan)this.delegate.remove(new MutableSpanMap.LookupKey(context));
            this.reportOrphanedSpans();
            return result;
        }
    }

    void reportOrphanedSpans() {
        Reference reference;
        while((reference = this.poll()) != null) {
            TraceContext context = (TraceContext)reference.get();
            MutableSpan value = (MutableSpan)this.delegate.remove(reference);
            if (value != null && !this.noop.get()) {
                try {
                    value.annotate(this.clock.currentTimeMicroseconds(), "brave.flush");
                    this.reporter.report(value.toSpan());
                } catch (RuntimeException var5) {
                    if (context != null && logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "error flushing " + context, var5);
                    }
                }
            }
        }

    }

    public String toString() {
        return "MutableSpanMap" + this.delegate.keySet();
    }

    static final class LookupKey {
        final TraceContext context;

        LookupKey(TraceContext context) {
            this.context = context;
        }

        public int hashCode() {
            return this.context.hashCode();
        }

        public boolean equals(Object other) {
            return this.context.equals(((MutableSpanMap.RealKey)other).get());
        }
    }

    static final class RealKey extends WeakReference<TraceContext> {
        final int hashCode;

        RealKey(TraceContext context, ReferenceQueue<TraceContext> queue) {
            super(context, queue);
            this.hashCode = context.hashCode();
        }

        public String toString() {
            TraceContext context = (TraceContext)this.get();
            return context != null ? "WeakReference(" + context + ")" : "ClearedReference()";
        }

        public int hashCode() {
            return this.hashCode;
        }

        public boolean equals(Object other) {
            TraceContext thisContext = (TraceContext)this.get();
            TraceContext thatContext = (TraceContext)((MutableSpanMap.RealKey)other).get();
            if (thisContext == null) {
                return thatContext == null;
            } else {
                return thisContext.equals(thatContext);
            }
        }
    }
}
