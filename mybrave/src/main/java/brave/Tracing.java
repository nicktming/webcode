package brave;

import brave.internal.Internal;
import brave.internal.Nullable;
import brave.internal.Platform;
import brave.propagation.CurrentTraceContext;
import brave.propagation.Propagation;
import brave.propagation.TraceContext;
import brave.propagation.Propagation.Factory;
import brave.propagation.Propagation.KeyFactory;
import brave.sampler.Sampler;
import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;
import zipkin2.Endpoint;
import zipkin2.Span;

public abstract class Tracing implements Closeable {
    static volatile Tracing current = null;
    final AtomicBoolean noop = new AtomicBoolean(false);

    public Tracing() {
    }

    public static Tracing.Builder newBuilder() {
        return new Tracing.Builder();
    }

    public abstract Tracer tracer();

    public Propagation<String> propagation() {
        return this.propagationFactory().create(KeyFactory.STRING);
    }

    public abstract Factory propagationFactory();

    public abstract CurrentTraceContext currentTraceContext();

    public abstract Clock clock();

    @Nullable
    public static Tracer currentTracer() {
        Tracing tracing = current;
        return tracing != null ? tracing.tracer() : null;
    }

    public boolean isNoop() {
        return this.noop.get();
    }

    public void setNoop(boolean noop) {
        this.noop.set(noop);
    }

    @Nullable
    public static Tracing current() {
        return current;
    }

    public abstract void close();

    static {
        Internal.instance = new Internal() {
            public Long timestamp(Tracer tracer, TraceContext context) {
                return tracer.recorder.timestamp(context);
            }
        };
    }

    static final class Default extends Tracing {
        final Tracer tracer;
        final Factory propagationFactory;
        final Propagation<String> stringPropagation;
        final CurrentTraceContext currentTraceContext;
        final Clock clock;

        Default(Tracing.Builder builder) {
            this.tracer = new Tracer(builder, this.noop);
            this.propagationFactory = builder.propagationFactory;
            this.stringPropagation = builder.propagationFactory.create(KeyFactory.STRING);
            this.currentTraceContext = builder.currentTraceContext;
            this.clock = builder.clock;
            this.maybeSetCurrent();
        }

        public Tracer tracer() {
            return this.tracer;
        }

        public Propagation<String> propagation() {
            return this.stringPropagation;
        }

        public Factory propagationFactory() {
            return this.propagationFactory;
        }

        public CurrentTraceContext currentTraceContext() {
            return this.currentTraceContext;
        }

        public Clock clock() {
            return this.clock;
        }

        private void maybeSetCurrent() {
            if (current == null) {
                Class var1 = Tracing.class;
                synchronized(Tracing.class) {
                    if (current == null) {
                        current = this;
                    }

                }
            }
        }

        public void close() {
            if (current == this) {
                Class var1 = Tracing.class;
                synchronized(Tracing.class) {
                    if (current == this) {
                        current = null;
                    }

                }
            }
        }
    }

    public static final class Builder {
        String localServiceName;
        Endpoint localEndpoint;
        Reporter<Span> reporter;
        Clock clock;
        Sampler sampler;
        CurrentTraceContext currentTraceContext;
        boolean traceId128Bit;
        boolean supportsJoin;
        Factory propagationFactory;

        public Tracing.Builder localServiceName(String localServiceName) {
            if (localServiceName == null) {
                throw new NullPointerException("localServiceName == null");
            } else {
                this.localServiceName = localServiceName;
                return this;
            }
        }

        /** @deprecated */
        @Deprecated
        public Tracing.Builder localEndpoint(zipkin.Endpoint localEndpoint) {
            if (localEndpoint == null) {
                throw new NullPointerException("localEndpoint == null");
            } else {
                return this.localEndpoint(localEndpoint.toV2());
            }
        }

        public Tracing.Builder localEndpoint(Endpoint localEndpoint) {
            if (localEndpoint == null) {
                throw new NullPointerException("localEndpoint == null");
            } else {
                this.localEndpoint = localEndpoint;
                return this;
            }
        }

        public Tracing.Builder spanReporter(Reporter<Span> reporter) {
            if (reporter == null) {
                throw new NullPointerException("spanReporter == null");
            } else {
                this.reporter = reporter;
                return this;
            }
        }

        /** @deprecated */
        /*
        @Deprecated
        public Tracing.Builder reporter(final zipkin.reporter.Reporter<zipkin.Span> reporter) {
            if (reporter == null) {
                throw new NullPointerException("spanReporter == null");
            } else if (reporter == zipkin.reporter.Reporter.NOOP) {
                this.reporter = Reporter.NOOP;
                return this;
            } else {
                this.reporter = new Reporter<Span>() {
                    public void report(Span span) {
                        reporter.report(V2SpanConverter.toSpan(span));
                    }

                    public String toString() {
                        return reporter.toString();
                    }
                };
                return this;
            }
        }
        */

        public Tracing.Builder clock(Clock clock) {
            if (clock == null) {
                throw new NullPointerException("clock == null");
            } else {
                this.clock = clock;
                return this;
            }
        }

        public Tracing.Builder sampler(Sampler sampler) {
            if (sampler == null) {
                throw new NullPointerException("sampler == null");
            } else {
                this.sampler = sampler;
                return this;
            }
        }

        public Tracing.Builder currentTraceContext(CurrentTraceContext currentTraceContext) {
            if (currentTraceContext == null) {
                throw new NullPointerException("currentTraceContext == null");
            } else {
                this.currentTraceContext = currentTraceContext;
                return this;
            }
        }

        public Tracing.Builder propagationFactory(Factory propagationFactory) {
            if (propagationFactory == null) {
                throw new NullPointerException("propagationFactory == null");
            } else {
                this.propagationFactory = propagationFactory;
                return this;
            }
        }

        public Tracing.Builder traceId128Bit(boolean traceId128Bit) {
            this.traceId128Bit = traceId128Bit;
            return this;
        }

        public Tracing.Builder supportsJoin(boolean supportsJoin) {
            this.supportsJoin = supportsJoin;
            return this;
        }

        public Tracing build() {
            if (this.clock == null) {
                this.clock = Platform.get();
            }

            if (this.localEndpoint == null) {
                this.localEndpoint = Platform.get().localEndpoint();
                if (this.localServiceName != null) {
                    this.localEndpoint = this.localEndpoint.toBuilder().serviceName(this.localServiceName).build();
                }
            }

            if (this.reporter == null) {
                this.reporter = Platform.get();
            }

            return new Tracing.Default(this);
        }

        Builder() {
            this.sampler = Sampler.ALWAYS_SAMPLE;
            this.currentTraceContext = brave.propagation.CurrentTraceContext.Default.inheritable();
            this.traceId128Bit = false;
            this.supportsJoin = true;
            this.propagationFactory = Factory.B3;
        }
    }
}
