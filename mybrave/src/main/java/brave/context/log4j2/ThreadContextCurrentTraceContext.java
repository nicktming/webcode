package brave.context.log4j2;

import brave.internal.HexCodec;
import brave.internal.Nullable;
import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
import brave.propagation.CurrentTraceContext.Default;
import brave.propagation.CurrentTraceContext.Scope;
import org.apache.logging.log4j.ThreadContext;

public final class ThreadContextCurrentTraceContext extends CurrentTraceContext {
    final CurrentTraceContext delegate;

    public static ThreadContextCurrentTraceContext create() {
        return create(Default.inheritable());
    }

    public static ThreadContextCurrentTraceContext create(CurrentTraceContext delegate) {
        return new ThreadContextCurrentTraceContext(delegate);
    }

    ThreadContextCurrentTraceContext(CurrentTraceContext delegate) {
        if (delegate == null) {
            throw new NullPointerException("delegate == null");
        } else {
            this.delegate = delegate;
        }
    }

    public TraceContext get() {
        return this.delegate.get();
    }

    public Scope newScope(@Nullable TraceContext currentSpan) {
        final String previousTraceId = ThreadContext.get("traceId");
        final String previousSpanId = ThreadContext.get("spanId");
        if (currentSpan != null) {
            ThreadContext.put("traceId", currentSpan.traceIdString());
            ThreadContext.put("spanId", HexCodec.toLowerHex(currentSpan.spanId()));
        } else {
            ThreadContext.remove("traceId");
            ThreadContext.remove("spanId");
        }

        final Scope scope = this.delegate.newScope(currentSpan);

        class ThreadContextCurrentTraceContextScope implements Scope {
            ThreadContextCurrentTraceContextScope() {
            }

            public void close() {
                scope.close();
                ThreadContext.put("traceId", previousTraceId);
                ThreadContext.put("spanId", previousSpanId);
            }
        }

        return new ThreadContextCurrentTraceContextScope();
    }
}

