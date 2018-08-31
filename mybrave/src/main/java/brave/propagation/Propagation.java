package brave.propagation;

import brave.internal.Nullable;
import brave.propagation.TraceContext.Extractor;
import brave.propagation.TraceContext.Injector;
import java.util.List;

public interface Propagation<K> {
    Propagation<String> B3_STRING = B3Propagation.FACTORY.create(Propagation.KeyFactory.STRING);

    List<K> keys();

    <C> Injector<C> injector(Propagation.Setter<C, K> var1);

    <C> Extractor<C> extractor(Propagation.Getter<C, K> var1);

    public interface Getter<C, K> {
        @Nullable
        String get(C var1, K var2);
    }

    public interface Setter<C, K> {
        void put(C var1, K var2, String var3);
    }

    public interface KeyFactory<K> {
        Propagation.KeyFactory<String> STRING = Propagation$KeyFactory$$Lambda$1.lambdaFactory$();

        K create(String var1);
    }

    public abstract static class Factory {
        public static final Propagation.Factory B3;

        public Factory() {
        }

        public boolean supportsJoin() {
            return false;
        }

        public boolean requires128BitTraceId() {
            return false;
        }

        public abstract <K> Propagation<K> create(Propagation.KeyFactory<K> var1);

        static {
            B3 = B3Propagation.FACTORY;
        }
    }
}

