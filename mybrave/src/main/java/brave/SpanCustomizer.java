package brave;

public interface SpanCustomizer {
    SpanCustomizer name(String var1);

    SpanCustomizer tag(String var1, String var2);

    SpanCustomizer annotate(String var1);

    SpanCustomizer annotate(long var1, String var3);
}
