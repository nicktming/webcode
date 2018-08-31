package brave.propagation;

import brave.internal.HexCodec;
import brave.propagation.Propagation.Factory;
import brave.propagation.Propagation.Getter;
import brave.propagation.Propagation.KeyFactory;
import brave.propagation.Propagation.Setter;
import brave.propagation.SamplingFlags.Builder;
import brave.propagation.TraceContext.Extractor;
import brave.propagation.TraceContext.Injector;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class B3Propagation<K> implements Propagation<K> {
    public static final Factory FACTORY = new Factory() {
        public <K> Propagation<K> create(KeyFactory<K> keyFactory) {
            return new B3Propagation(keyFactory);
        }

        public boolean supportsJoin() {
            return true;
        }

        public String toString() {
            return "B3PropagationFactory";
        }
    };
    static final String TRACE_ID_NAME = "X-B3-TraceId";
    static final String SPAN_ID_NAME = "X-B3-SpanId";
    static final String PARENT_SPAN_ID_NAME = "X-B3-ParentSpanId";
    static final String SAMPLED_NAME = "X-B3-Sampled";
    static final String FLAGS_NAME = "X-B3-Flags";
    final K traceIdKey;
    final K spanIdKey;
    final K parentSpanIdKey;
    final K sampledKey;
    final K debugKey;
    final List<K> fields;

    B3Propagation(KeyFactory<K> keyFactory) {
        this.traceIdKey = keyFactory.create("X-B3-TraceId");
        this.spanIdKey = keyFactory.create("X-B3-SpanId");
        this.parentSpanIdKey = keyFactory.create("X-B3-ParentSpanId");
        this.sampledKey = keyFactory.create("X-B3-Sampled");
        this.debugKey = keyFactory.create("X-B3-Flags");
        this.fields = Collections.unmodifiableList(Arrays.asList(this.traceIdKey, this.spanIdKey, this.parentSpanIdKey, this.sampledKey, this.debugKey));
    }

    public List<K> keys() {
        return this.fields;
    }

    public <C> Injector<C> injector(Setter<C, K> setter) {
        if (setter == null) {
            throw new NullPointerException("setter == null");
        } else {
            return new B3Propagation.B3Injector(this, setter);
        }
    }

    public <C> Extractor<C> extractor(Getter<C, K> getter) {
        if (getter == null) {
            throw new NullPointerException("getter == null");
        } else {
            return new B3Propagation.B3Extractor(this, getter);
        }
    }

    static final class B3Extractor<C, K> implements Extractor<C> {
        final B3Propagation<K> propagation;
        final Getter<C, K> getter;

        B3Extractor(B3Propagation<K> propagation, Getter<C, K> getter) {
            this.propagation = propagation;
            this.getter = getter;
        }

        public TraceContextOrSamplingFlags extract(C carrier) {
            if (carrier == null) {
                throw new NullPointerException("carrier == null");
            } else {
                String traceId = this.getter.get(carrier, this.propagation.traceIdKey);
                String sampled = this.getter.get(carrier, this.propagation.sampledKey);
                String debug = this.getter.get(carrier, this.propagation.debugKey);
                if (traceId == null && sampled == null && debug == null) {
                    return TraceContextOrSamplingFlags.EMPTY;
                } else {
                    Boolean sampledV = sampled != null ? sampled.equals("1") || sampled.equalsIgnoreCase("true") : null;
                    boolean debugV = "1".equals(debug);
                    String spanId = this.getter.get(carrier, this.propagation.spanIdKey);
                    if (spanId == null) {
                        return TraceContextOrSamplingFlags.create(debugV ? SamplingFlags.DEBUG : Builder.build(sampledV));
                    } else {
                        brave.propagation.TraceContext.Builder result = TraceContext.newBuilder().sampled(sampledV).debug(debugV);
                        result.traceIdHigh(traceId.length() == 32 ? HexCodec.lowerHexToUnsignedLong(traceId, 0) : 0L);
                        result.traceId(HexCodec.lowerHexToUnsignedLong(traceId));
                        result.spanId(HexCodec.lowerHexToUnsignedLong(spanId));
                        String parentSpanIdString = this.getter.get(carrier, this.propagation.parentSpanIdKey);
                        if (parentSpanIdString != null) {
                            result.parentId(HexCodec.lowerHexToUnsignedLong(parentSpanIdString));
                        }

                        return TraceContextOrSamplingFlags.create(result.build());
                    }
                }
            }
        }
    }

    static final class B3Injector<C, K> implements Injector<C> {
        final B3Propagation<K> propagation;
        final Setter<C, K> setter;

        B3Injector(B3Propagation<K> propagation, Setter<C, K> setter) {
            this.propagation = propagation;
            this.setter = setter;
        }

        public void inject(TraceContext traceContext, C carrier) {
            this.setter.put(carrier, this.propagation.traceIdKey, traceContext.traceIdString());
            this.setter.put(carrier, this.propagation.spanIdKey, HexCodec.toLowerHex(traceContext.spanId()));
            if (traceContext.parentId() != null) {
                this.setter.put(carrier, this.propagation.parentSpanIdKey, HexCodec.toLowerHex(traceContext.parentId()));
            }

            if (traceContext.debug()) {
                this.setter.put(carrier, this.propagation.debugKey, "1");
            } else if (traceContext.sampled() != null) {
                this.setter.put(carrier, this.propagation.sampledKey, traceContext.sampled() ? "1" : "0");
            }

        }
    }
}
