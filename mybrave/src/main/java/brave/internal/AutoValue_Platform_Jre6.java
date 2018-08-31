package brave.internal;

import brave.internal.Platform.Jre6;
import java.util.Random;

final class AutoValue_Platform_Jre6 extends Jre6 {
    private final boolean zipkinV1Present;
    private final Random prng;

    AutoValue_Platform_Jre6(boolean zipkinV1Present, Random prng) {
        this.zipkinV1Present = zipkinV1Present;
        if (prng == null) {
            throw new NullPointerException("Null prng");
        } else {
            this.prng = prng;
        }
    }

    public boolean zipkinV1Present() {
        return this.zipkinV1Present;
    }

    Random prng() {
        return this.prng;
    }

    public String toString() {
        return "Jre6{zipkinV1Present=" + this.zipkinV1Present + ", prng=" + this.prng + "}";
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Jre6)) {
            return false;
        } else {
            Jre6 that = (Jre6)o;
            return this.zipkinV1Present == that.zipkinV1Present() && this.prng.equals(that.prng());
        }
    }

    public int hashCode() {
        int h = 1;
        h = h * 1000003;
        h ^= this.zipkinV1Present ? 1231 : 1237;
        h *= 1000003;
        h ^= this.prng.hashCode();
        return h;
    }
}
