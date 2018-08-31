package brave.internal.zipkin;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import zipkin.Annotation;
import zipkin.BinaryAnnotation;
import zipkin.Endpoint;
import zipkin.BinaryAnnotation.Type;
import zipkin2.DependencyLink;
import zipkin2.Span;
import zipkin2.Span.Builder;
import zipkin2.Span.Kind;

public final class V2SpanConverter {
    public V2SpanConverter() {
    }

    public static List<Span> fromSpan(zipkin.Span source) {
        V2SpanConverter.Builders builders = new V2SpanConverter.Builders(source);
        builders.processAnnotations(source);
        builders.processBinaryAnnotations(source);
        return builders.build();
    }

    static boolean closeEnough(Endpoint left, Endpoint right) {
        return left.serviceName.equals(right.serviceName);
    }

    static Builder newBuilder(zipkin.Span source) {
        return Span.newBuilder().traceId(source.traceIdString()).parentId(source.parentId != null ? Util.toLowerHex(source.parentId) : null).id(Util.toLowerHex(source.id)).name(source.name).debug(source.debug);
    }

    public static zipkin.Span toSpan(Span in) {
        String traceId = in.traceId();
        zipkin.Span.Builder result = zipkin.Span.builder().traceId(Util.lowerHexToUnsignedLong(traceId)).parentId(in.parentId() != null ? Util.lowerHexToUnsignedLong(in.parentId()) : null).id(Util.lowerHexToUnsignedLong(in.id())).debug(in.debug()).name(in.name() != null ? in.name() : "");
        if (traceId.length() == 32) {
            result.traceIdHigh(Util.lowerHexToUnsignedLong(traceId, 0));
        }

        long startTs = in.timestamp() == null ? 0L : in.timestamp();
        Long endTs = in.duration() == null ? 0L : in.timestamp() + in.duration();
        if (startTs != 0L) {
            result.timestamp(startTs);
            result.duration(in.duration());
        }

        Endpoint local = in.localEndpoint() != null ? toEndpoint(in.localEndpoint()) : null;
        Endpoint remote = in.remoteEndpoint() != null ? toEndpoint(in.remoteEndpoint()) : null;
        Kind kind = in.kind();
        Annotation cs = null;
        Annotation sr = null;
        Annotation ss = null;
        Annotation cr = null;
        Annotation ms = null;
        Annotation mr = null;
        Annotation ws = null;
        Annotation wr = null;
        String remoteEndpointType = null;
        boolean wroteEndpoint = false;
        int i = 0;

        for(int length = in.annotations().size(); i < length; ++i) {
            zipkin2.Annotation input = (zipkin2.Annotation)in.annotations().get(i);
            Annotation a = Annotation.create(input.timestamp(), input.value(), local);
            if (a.value.length() == 2) {
                if (a.value.equals("cs")) {
                    kind = Kind.CLIENT;
                    cs = a;
                    remoteEndpointType = "sa";
                } else if (a.value.equals("sr")) {
                    kind = Kind.SERVER;
                    sr = a;
                    remoteEndpointType = "ca";
                } else if (a.value.equals("ss")) {
                    kind = Kind.SERVER;
                    ss = a;
                } else if (a.value.equals("cr")) {
                    kind = Kind.CLIENT;
                    cr = a;
                } else if (a.value.equals("ms")) {
                    kind = Kind.PRODUCER;
                    ms = a;
                } else if (a.value.equals("mr")) {
                    kind = Kind.CONSUMER;
                    mr = a;
                } else if (a.value.equals("ws")) {
                    ws = a;
                } else if (a.value.equals("wr")) {
                    wr = a;
                } else {
                    wroteEndpoint = true;
                    result.addAnnotation(a);
                }
            } else {
                wroteEndpoint = true;
                result.addAnnotation(a);
            }
        }

        if (kind != null) {
            switch(kind) {
                case CLIENT:
                    remoteEndpointType = "sa";
                    if (startTs != 0L) {
                        cs = Annotation.create(startTs, "cs", local);
                    }

                    if (endTs != 0L) {
                        cr = Annotation.create(endTs, "cr", local);
                    }
                    break;
                case SERVER:
                    remoteEndpointType = "ca";
                    if (startTs != 0L) {
                        sr = Annotation.create(startTs, "sr", local);
                    }

                    if (endTs != 0L) {
                        ss = Annotation.create(endTs, "ss", local);
                    }
                    break;
                case PRODUCER:
                    remoteEndpointType = "ma";
                    if (startTs != 0L) {
                        ms = Annotation.create(startTs, "ms", local);
                    }

                    if (endTs != 0L) {
                        ws = Annotation.create(endTs, "ws", local);
                    }
                    break;
                case CONSUMER:
                    remoteEndpointType = "ma";
                    if (startTs != 0L && endTs != 0L) {
                        wr = Annotation.create(startTs, "wr", local);
                        mr = Annotation.create(endTs, "mr", local);
                    } else if (startTs != 0L) {
                        mr = Annotation.create(startTs, "mr", local);
                    }
                    break;
                default:
                    throw new AssertionError("update kind mapping");
            }
        }

        Iterator var23 = in.tags().entrySet().iterator();

        while(var23.hasNext()) {
            Entry<String, String> tag = (Entry)var23.next();
            wroteEndpoint = true;
            result.addBinaryAnnotation(BinaryAnnotation.create((String)tag.getKey(), (String)tag.getValue(), local));
        }

        if (cs == null && sr == null && ss == null && cr == null && ws == null && wr == null && ms == null && mr == null) {
            if (local != null && remote != null) {
                result.addBinaryAnnotation(BinaryAnnotation.address("ca", local));
                wroteEndpoint = true;
                remoteEndpointType = "sa";
            }
        } else {
            if (cs != null) {
                result.addAnnotation(cs);
            }

            if (sr != null) {
                result.addAnnotation(sr);
            }

            if (ss != null) {
                result.addAnnotation(ss);
            }

            if (cr != null) {
                result.addAnnotation(cr);
            }

            if (ws != null) {
                result.addAnnotation(ws);
            }

            if (wr != null) {
                result.addAnnotation(wr);
            }

            if (ms != null) {
                result.addAnnotation(ms);
            }

            if (mr != null) {
                result.addAnnotation(mr);
            }

            wroteEndpoint = true;
        }

        if (remoteEndpointType != null && remote != null) {
            result.addBinaryAnnotation(BinaryAnnotation.address(remoteEndpointType, remote));
        }

        if (Boolean.TRUE.equals(in.shared()) && sr != null) {
            result.timestamp((Long)null).duration((Long)null);
        }

        if (local != null && !wroteEndpoint) {
            result.addBinaryAnnotation(BinaryAnnotation.create("lc", "", local));
        }

        return result.build();
    }

    public static Endpoint toEndpoint(zipkin2.Endpoint input) {
        zipkin.Endpoint.Builder result = Endpoint.builder().serviceName(input.serviceName() != null ? input.serviceName() : "").port(input.port() != null ? input.port() : 0);
        if (input.ipv6() != null) {
            result.parseIp(input.ipv6());
        }

        if (input.ipv4() != null) {
            result.parseIp(input.ipv4());
        }

        return result.build();
    }

    static List<zipkin.Span> toSpans(List<Span> spans) {
        if (spans.isEmpty()) {
            return Collections.emptyList();
        } else {
            int length = spans.size();
            List<zipkin.Span> span1s = new ArrayList(length);

            for(int i = 0; i < length; ++i) {
                span1s.add(toSpan((Span)spans.get(i)));
            }

            return span1s;
        }
    }

    public static DependencyLink fromLink(zipkin.DependencyLink link) {
        return DependencyLink.newBuilder().parent(link.parent).child(link.child).callCount(link.callCount).errorCount(link.errorCount).build();
    }

    public static zipkin.DependencyLink toLink(DependencyLink link) {
        return zipkin.DependencyLink.builder().parent(link.parent()).child(link.child()).callCount(link.callCount()).errorCount(link.errorCount()).build();
    }

    public static List<zipkin.DependencyLink> toLinks(List<DependencyLink> links) {
        if (links.isEmpty()) {
            return Collections.emptyList();
        } else {
            int length = links.size();
            List<zipkin.DependencyLink> result = new ArrayList(length);

            for(int i = 0; i < length; ++i) {
                DependencyLink link2 = (DependencyLink)links.get(i);
                result.add(zipkin.DependencyLink.builder().parent(link2.parent()).child(link2.child()).callCount(link2.callCount()).errorCount(link2.errorCount()).build());
            }

            return result;
        }
    }

    public static List<DependencyLink> fromLinks(Iterable<zipkin.DependencyLink> links) {
        List<DependencyLink> result = new ArrayList();
        Iterator var2 = links.iterator();

        while(var2.hasNext()) {
            zipkin.DependencyLink link1 = (zipkin.DependencyLink)var2.next();
            result.add(DependencyLink.newBuilder().parent(link1.parent).child(link1.child).callCount(link1.callCount).errorCount(link1.errorCount).build());
        }

        return result;
    }

    public static List<Span> fromSpans(Iterable<zipkin.Span> spans) {
        List<Span> result = new ArrayList();
        Iterator var2 = spans.iterator();

        while(var2.hasNext()) {
            zipkin.Span span1 = (zipkin.Span)var2.next();
            result.addAll(fromSpan(span1));
        }

        return result;
    }

    static final class Builders {
        final List<Builder> spans = new ArrayList();
        Annotation cs = null;
        Annotation sr = null;
        Annotation ss = null;
        Annotation cr = null;
        Annotation ms = null;
        Annotation mr = null;
        Annotation ws = null;
        Annotation wr = null;

        Builders(zipkin.Span source) {
            this.spans.add(V2SpanConverter.newBuilder(source));
        }

        void processAnnotations(zipkin.Span source) {
            int i = 0;

            for(int length = source.annotations.size(); i < length; ++i) {
                Annotation a = (Annotation)source.annotations.get(i);
                Builder currentSpan = this.forEndpoint(source, a.endpoint);
                if (a.value.length() == 2 && a.endpoint != null) {
                    if (a.value.equals("cs")) {
                        currentSpan.kind(Kind.CLIENT);
                        this.cs = a;
                    } else if (a.value.equals("sr")) {
                        currentSpan.kind(Kind.SERVER);
                        this.sr = a;
                    } else if (a.value.equals("ss")) {
                        currentSpan.kind(Kind.SERVER);
                        this.ss = a;
                    } else if (a.value.equals("cr")) {
                        currentSpan.kind(Kind.CLIENT);
                        this.cr = a;
                    } else if (a.value.equals("ms")) {
                        currentSpan.kind(Kind.PRODUCER);
                        this.ms = a;
                    } else if (a.value.equals("mr")) {
                        currentSpan.kind(Kind.CONSUMER);
                        this.mr = a;
                    } else if (a.value.equals("ws")) {
                        this.ws = a;
                    } else if (a.value.equals("wr")) {
                        this.wr = a;
                    } else {
                        currentSpan.addAnnotation(a.timestamp, a.value);
                    }
                } else {
                    currentSpan.addAnnotation(a.timestamp, a.value);
                }
            }

            Builder producer;
            Builder consumer;
            if (this.cs != null && this.sr != null) {
                this.maybeTimestampDuration(source, this.cs, this.cr);
                producer = this.forEndpoint(source, this.cs.endpoint);
                if (V2SpanConverter.closeEnough(this.cs.endpoint, this.sr.endpoint)) {
                    producer.kind(Kind.CLIENT);
                    consumer = this.newSpanBuilder(source, this.sr.endpoint.toV2()).kind(Kind.SERVER);
                } else {
                    consumer = this.forEndpoint(source, this.sr.endpoint);
                }

                consumer.shared(true).timestamp(this.sr.timestamp);
                if (this.ss != null) {
                    consumer.duration(this.ss.timestamp - this.sr.timestamp);
                }

                if (this.cr == null && source.duration == null) {
                    producer.duration((Long)null);
                }
            } else if (this.cs != null && this.cr != null) {
                this.maybeTimestampDuration(source, this.cs, this.cr);
            } else if (this.sr != null && this.ss != null) {
                this.maybeTimestampDuration(source, this.sr, this.ss);
            } else {
                Iterator var6 = this.spans.iterator();

                while(var6.hasNext()) {
                    consumer = (Builder)var6.next();
                    if (Kind.CLIENT.equals(consumer.kind())) {
                        if (this.cs != null) {
                            consumer.timestamp(this.cs.timestamp);
                        }
                    } else if (Kind.SERVER.equals(consumer.kind()) && this.sr != null) {
                        consumer.timestamp(this.sr.timestamp);
                    }
                }

                if (source.timestamp != null) {
                    ((Builder)this.spans.get(0)).timestamp(source.timestamp).duration(source.duration);
                }
            }

            if (this.cs == null && this.sr != null && source.timestamp == null) {
                this.forEndpoint(source, this.sr.endpoint).shared(true);
            }

            if (this.ms != null && this.mr != null) {
                producer = this.forEndpoint(source, this.ms.endpoint);
                if (V2SpanConverter.closeEnough(this.ms.endpoint, this.mr.endpoint)) {
                    producer.kind(Kind.PRODUCER);
                    consumer = this.newSpanBuilder(source, this.mr.endpoint.toV2()).kind(Kind.CONSUMER);
                } else {
                    consumer = this.forEndpoint(source, this.mr.endpoint);
                }

                consumer.shared(true);
                if (this.wr != null) {
                    consumer.timestamp(this.wr.timestamp).duration(this.mr.timestamp - this.wr.timestamp);
                } else {
                    consumer.timestamp(this.mr.timestamp);
                }

                producer.timestamp(this.ms.timestamp).duration(this.ws != null ? this.ws.timestamp - this.ms.timestamp : null);
            } else if (this.ms != null) {
                this.maybeTimestampDuration(source, this.ms, this.ws);
            } else if (this.mr != null) {
                if (this.wr != null) {
                    this.maybeTimestampDuration(source, this.wr, this.mr);
                } else {
                    this.maybeTimestampDuration(source, this.mr, (Annotation)null);
                }
            } else {
                if (this.ws != null) {
                    this.forEndpoint(source, this.ws.endpoint).addAnnotation(this.ws.timestamp, this.ws.value);
                }

                if (this.wr != null) {
                    this.forEndpoint(source, this.wr.endpoint).addAnnotation(this.wr.timestamp, this.wr.value);
                }
            }

        }

        void maybeTimestampDuration(zipkin.Span source, Annotation begin, Annotation end) {
            Builder span2 = this.forEndpoint(source, begin.endpoint);
            if (source.timestamp != null && source.duration != null) {
                span2.timestamp(source.timestamp).duration(source.duration);
            } else {
                span2.timestamp(begin.timestamp);
                if (end != null) {
                    span2.duration(end.timestamp - begin.timestamp);
                }
            }

        }

        void processBinaryAnnotations(zipkin.Span source) {
            Endpoint ca = null;
            Endpoint sa = null;
            Endpoint ma = null;
            int i = 0;

            for(int length = source.binaryAnnotations.size(); i < length; ++i) {
                BinaryAnnotation b = (BinaryAnnotation)source.binaryAnnotations.get(i);
                if (b.type == Type.BOOL) {
                    if ("ca".equals(b.key)) {
                        ca = b.endpoint;
                    } else if ("sa".equals(b.key)) {
                        sa = b.endpoint;
                    } else if ("ma".equals(b.key)) {
                        ma = b.endpoint;
                    } else {
                        this.forEndpoint(source, b.endpoint).putTag(b.key, b.value[0] == 1 ? "true" : "false");
                    }
                } else {
                    Builder currentSpan = this.forEndpoint(source, b.endpoint);
                    switch(b.type) {
                        case BOOL:
                        default:
                            break;
                        case STRING:
                            if (!"lc".equals(b.key) || b.value.length != 0) {
                                currentSpan.putTag(b.key, new String(b.value, Util.UTF_8));
                            }
                            break;
                        case BYTES:
                            currentSpan.putTag(b.key, Util.writeBase64Url(b.value));
                            break;
                        case I16:
                            currentSpan.putTag(b.key, Short.toString(ByteBuffer.wrap(b.value).getShort()));
                            break;
                        case I32:
                            currentSpan.putTag(b.key, Integer.toString(ByteBuffer.wrap(b.value).getInt()));
                            break;
                        case I64:
                            currentSpan.putTag(b.key, Long.toString(ByteBuffer.wrap(b.value).getLong()));
                            break;
                        case DOUBLE:
                            double wrapped = Double.longBitsToDouble(ByteBuffer.wrap(b.value).getLong());
                            currentSpan.putTag(b.key, Double.toString(wrapped));
                    }
                }
            }

            if (this.cs != null && sa != null && !V2SpanConverter.closeEnough(sa, this.cs.endpoint)) {
                this.forEndpoint(source, this.cs.endpoint).remoteEndpoint(sa.toV2());
            }

            if (this.sr != null && ca != null && !V2SpanConverter.closeEnough(ca, this.sr.endpoint)) {
                this.forEndpoint(source, this.sr.endpoint).remoteEndpoint(ca.toV2());
            }

            if (this.ms != null && ma != null && !V2SpanConverter.closeEnough(ma, this.ms.endpoint)) {
                this.forEndpoint(source, this.ms.endpoint).remoteEndpoint(ma.toV2());
            }

            if (this.mr != null && ma != null && !V2SpanConverter.closeEnough(ma, this.mr.endpoint)) {
                this.forEndpoint(source, this.mr.endpoint).remoteEndpoint(ma.toV2());
            }

            if (this.cs == null && this.sr == null && ca != null && sa != null) {
                this.forEndpoint(source, ca).remoteEndpoint(sa.toV2());
            }

        }

        Builder forEndpoint(zipkin.Span source,  Endpoint e) {
            if (e == null) {
                return (Builder)this.spans.get(0);
            } else {
                zipkin2.Endpoint converted = e.toV2();
                int i = 0;

                for(int length = this.spans.size(); i < length; ++i) {
                    Builder next = (Builder)this.spans.get(i);
                    zipkin2.Endpoint nextLocalEndpoint = next.localEndpoint();
                    if (nextLocalEndpoint == null) {
                        next.localEndpoint(converted);
                        return next;
                    }

                    if (V2SpanConverter.closeEnough(V2SpanConverter.toEndpoint(nextLocalEndpoint), e)) {
                        return next;
                    }
                }

                return this.newSpanBuilder(source, converted);
            }
        }

        Builder newSpanBuilder(zipkin.Span source, zipkin2.Endpoint e) {
            Builder result = V2SpanConverter.newBuilder(source).localEndpoint(e);
            this.spans.add(result);
            return result;
        }

        List<Span> build() {
            int length = this.spans.size();
            if (length == 1) {
                return Collections.singletonList(((Builder)this.spans.get(0)).build());
            } else {
                List<Span> result = new ArrayList(length);

                for(int i = 0; i < length; ++i) {
                    result.add(((Builder)this.spans.get(i)).build());
                }

                return result;
            }
        }
    }
}
