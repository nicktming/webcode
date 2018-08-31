package brave.internal;

import brave.Clock;
import brave.Tracer;
import com.google.auto.value.AutoValue;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jvnet.animal_sniffer.IgnoreJRERequirement;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.Endpoint.Builder;

public abstract class Platform implements Clock, Reporter<Span> {
    static final Logger logger = Logger.getLogger(Tracer.class.getName());
    private static final Platform PLATFORM = findPlatform();
    final long createTimestamp = System.currentTimeMillis() * 1000L;
    final long createTick = System.nanoTime();
    volatile Endpoint localEndpoint;

    Platform() {
    }

    public abstract boolean zipkinV1Present();

    public void report(Span span) {
        if (logger.isLoggable(Level.INFO)) {
            if (span == null) {
                throw new NullPointerException("span == null");
            } else {
                logger.info(span.toString());
            }
        }
    }

    public Endpoint localEndpoint() {
        if (this.localEndpoint == null) {
            synchronized(this) {
                if (this.localEndpoint == null) {
                    this.localEndpoint = this.produceLocalEndpoint();
                }
            }
        }

        return this.localEndpoint;
    }

    Endpoint produceLocalEndpoint() {
        Builder builder = Endpoint.newBuilder().serviceName("unknown");

        try {
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            if (nics == null) {
                return builder.build();
            }

            while(true) {
                while(nics.hasMoreElements()) {
                    NetworkInterface nic = (NetworkInterface)nics.nextElement();
                    Enumeration addresses = nic.getInetAddresses();

                    while(addresses.hasMoreElements()) {
                        InetAddress address = (InetAddress)addresses.nextElement();
                        if (address.isSiteLocalAddress()) {
                            builder.ip(address);
                            break;
                        }
                    }
                }

                return builder.build();
            }
        } catch (Exception var6) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "error reading nics", var6);
            }
        }

        return builder.build();
    }

    public static Platform get() {
        return PLATFORM;
    }

    static Platform findPlatform() {
        boolean zipkinV1Present;
        try {
            Class.forName("zipkin.Endpoint");
            zipkinV1Present = true;
        } catch (ClassNotFoundException var2) {
            zipkinV1Present = false;
        }

        Platform jre7 = Platform.Jre7.buildIfSupported(zipkinV1Present);
        return (Platform)(jre7 != null ? jre7 : Platform.Jre6.build(zipkinV1Present));
    }

    public abstract long randomLong();

    public abstract long nextTraceIdHigh();

    public long currentTimeMicroseconds() {
        return (System.nanoTime() - this.createTick) / 1000L + this.createTimestamp;
    }

    static long nextTraceIdHigh(Random prng) {
        long epochSeconds = System.currentTimeMillis() / 1000L;
        int random = prng.nextInt();
        return (epochSeconds & 4294967295L) << 32 | (long)random & 4294967295L;
    }

    @AutoValue
    abstract static class Jre6 extends Platform {
        Jre6() {
        }

        abstract Random prng();

        static Platform.Jre6 build(boolean zipkinV1Present) {
            return new AutoValue_Platform_Jre6(zipkinV1Present, new Random(System.nanoTime()));
        }

        public long randomLong() {
            return this.prng().nextLong();
        }

        public long nextTraceIdHigh() {
            return nextTraceIdHigh(this.prng());
        }
    }

    @AutoValue
    abstract static class Jre7 extends Platform {
        Jre7() {
        }

        static Platform.Jre7 buildIfSupported(boolean zipkinV1Present) {
            try {
                Class.forName("java.util.concurrent.ThreadLocalRandom");
                return new AutoValue_Platform_Jre7(zipkinV1Present);
            } catch (ClassNotFoundException var2) {
                return null;
            }
        }

        @IgnoreJRERequirement
        public long randomLong() {
            return ThreadLocalRandom.current().nextLong();
        }

        @IgnoreJRERequirement
        public long nextTraceIdHigh() {
            return nextTraceIdHigh(ThreadLocalRandom.current());
        }
    }
}
