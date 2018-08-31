package brave.propagation;

import brave.Tracing;
import brave.propagation.Propagation.Getter;
import brave.propagation.Propagation.KeyFactory;
import brave.propagation.Propagation.Setter;
import brave.propagation.TraceContext.Extractor;
import brave.propagation.TraceContext.Injector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
//import javax.annotation.Nullable;

public final class ExtraFieldPropagation<K> implements Propagation<K> {
    final Propagation<K> delegate;
    final List<K> keys;
    final Map<String, K> nameToKey;

    public static brave.propagation.Propagation.Factory newFactory(brave.propagation.Propagation.Factory delegate, String... names) {
        return new ExtraFieldPropagation.Factory(delegate, Arrays.asList(names));
    }

    public static brave.propagation.Propagation.Factory newFactory(brave.propagation.Propagation.Factory delegate, Collection<String> names) {
        return new ExtraFieldPropagation.Factory(delegate, names);
    }

    //@Nullable
    public static String current(String name) {
        Tracing tracing = Tracing.current();
        if (tracing == null) {
            return null;
        } else {
            TraceContext context = tracing.currentTraceContext().get();
            return context == null ? null : get(context, name);
        }
    }

    //@Nullable
    public static String get(TraceContext context, String name) {
        if (context == null) {
            throw new NullPointerException("context == null");
        } else if (name == null) {
            throw new NullPointerException("name == null");
        } else {
            name = name.toLowerCase(Locale.ROOT);
            Iterator var2 = context.extra().iterator();

            Object extra;
            do {
                if (!var2.hasNext()) {
                    return null;
                }

                extra = var2.next();
            } while(!(extra instanceof ExtraFieldPropagation.Extra));

            return ((ExtraFieldPropagation.Extra)extra).get(name);
        }
    }

    ExtraFieldPropagation(Propagation<K> delegate, Map<String, K> nameToKey) {
        this.delegate = delegate;
        this.nameToKey = nameToKey;
        List<K> keys = new ArrayList(delegate.keys());
        keys.addAll(nameToKey.values());
        this.keys = Collections.unmodifiableList(keys);
    }

    public List<K> keys() {
        return this.keys;
    }

    public <C> Injector<C> injector(Setter<C, K> setter) {
        return new ExtraFieldPropagation.ExtraFieldInjector(this.delegate.injector(setter), setter, this.nameToKey);
    }

    public <C> Extractor<C> extractor(Getter<C, K> getter) {
        Extractor<C> extractorDelegate = this.delegate.extractor(getter);
        return new ExtraFieldPropagation.ExtraFieldExtractor(extractorDelegate, getter, this.nameToKey);
    }

    static final class ExtraFieldExtractor<C, K> implements Extractor<C> {
        final Extractor<C> delegate;
        final Getter<C, K> getter;
        final Map<String, K> names;

        ExtraFieldExtractor(Extractor<C> delegate, Getter<C, K> getter, Map<String, K> names) {
            this.delegate = delegate;
            this.getter = getter;
            this.names = names;
        }

        public TraceContextOrSamplingFlags extract(C carrier) {
            TraceContextOrSamplingFlags result = this.delegate.extract(carrier);
            ExtraFieldPropagation.Extra extra = null;
            Iterator var4 = this.names.entrySet().iterator();

            while(var4.hasNext()) {
                Entry<String, K> field = (Entry)var4.next();
                String maybeValue = this.getter.get(carrier, field.getValue());
                if (maybeValue != null) {
                    if (extra == null) {
                        extra = new ExtraFieldPropagation.One();
                    } else if (extra instanceof ExtraFieldPropagation.One) {
                        ExtraFieldPropagation.One one = (ExtraFieldPropagation.One)extra;
                        extra = new ExtraFieldPropagation.Many();
                        ((ExtraFieldPropagation.Extra)extra).put(one.name, one.value);
                    }

                    ((ExtraFieldPropagation.Extra)extra).put((String)field.getKey(), maybeValue);
                }
            }

            if (extra == null) {
                return result;
            } else {
                return result.toBuilder().addExtra(extra).build();
            }
        }
    }

    static final class ExtraFieldInjector<C, K> implements Injector<C> {
        final Injector<C> delegate;
        final Setter<C, K> setter;
        final Map<String, K> nameToKey;

        ExtraFieldInjector(Injector<C> delegate, Setter<C, K> setter, Map<String, K> nameToKey) {
            this.delegate = delegate;
            this.setter = setter;
            this.nameToKey = nameToKey;
        }

        public void inject(TraceContext traceContext, C carrier) {
            Iterator var3 = traceContext.extra().iterator();

            while(var3.hasNext()) {
                Object extra = var3.next();
                if (extra instanceof ExtraFieldPropagation.Extra) {
                    ((ExtraFieldPropagation.Extra)extra).setAll(carrier, this.setter, this.nameToKey);
                    break;
                }
            }

            this.delegate.inject(traceContext, carrier);
        }
    }

    static final class Many extends ExtraFieldPropagation.Extra {
        final LinkedHashMap<String, String> fields = new LinkedHashMap();

        Many() {
        }

        void put(String name, String value) {
            this.fields.put(name, value);
        }

        String get(String name) {
            return (String)this.fields.get(name);
        }

        <C, K> void setAll(C carrier, Setter<C, K> setter, Map<String, K> nameToKey) {
            Iterator var4 = this.fields.entrySet().iterator();

            while(var4.hasNext()) {
                Entry<String, String> field = (Entry)var4.next();
                K key = nameToKey.get(field.getKey());
                if (key != null) {
                    setter.put(carrier, nameToKey.get(field.getKey()), (String)field.getValue());
                }
            }

        }

        public String toString() {
            return "ExtraFieldPropagation" + this.fields;
        }
    }

    static final class One extends ExtraFieldPropagation.Extra {
        String name;
        String value;

        One() {
        }

        void put(String name, String value) {
            this.name = name;
            this.value = value;
        }

        String get(String name) {
            return name.equals(this.name) ? this.value : null;
        }

        <C, K> void setAll(C carrier, Setter<C, K> setter, Map<String, K> nameToKey) {
            K key = nameToKey.get(this.name);
            if (key != null) {
                setter.put(carrier, key, this.value);
            }
        }

        public String toString() {
            return "ExtraFieldPropagation{" + this.name + "=" + this.value + "}";
        }
    }

    abstract static class Extra {
        Extra() {
        }

        abstract void put(String var1, String var2);

        abstract String get(String var1);

        abstract <C, K> void setAll(C var1, Setter<C, K> var2, Map<String, K> var3);
    }

    static final class Factory extends brave.propagation.Propagation.Factory {
        final brave.propagation.Propagation.Factory delegate;
        final List<String> names;

        Factory(brave.propagation.Propagation.Factory delegate, Collection<String> names) {
            if (delegate == null) {
                throw new NullPointerException("field == null");
            } else if (names == null) {
                throw new NullPointerException("names == null");
            } else if (names.isEmpty()) {
                throw new NullPointerException("names.length == 0");
            } else {
                this.delegate = delegate;
                this.names = new ArrayList();
                Iterator var3 = names.iterator();

                while(var3.hasNext()) {
                    String name = (String)var3.next();
                    this.names.add(name.toLowerCase(Locale.ROOT));
                }

            }
        }

        public boolean supportsJoin() {
            return this.delegate.supportsJoin();
        }

        public boolean requires128BitTraceId() {
            return this.delegate.requires128BitTraceId();
        }

        public final <K> Propagation<K> create(KeyFactory<K> keyFactory) {
            Map<String, K> names = new LinkedHashMap();
            Iterator var3 = this.names.iterator();

            while(var3.hasNext()) {
                String name = (String)var3.next();
                names.put(name, keyFactory.create(name));
            }

            return new ExtraFieldPropagation(this.delegate.create(keyFactory), names);
        }
    }
}
