package brave.internal;

import brave.internal.Platform.Jre7;

final class AutoValue_Platform_Jre7 extends Jre7 {
    private final boolean zipkinV1Present;

    AutoValue_Platform_Jre7(boolean zipkinV1Present) {
        this.zipkinV1Present = zipkinV1Present;
    }

    public boolean zipkinV1Present() {
        return this.zipkinV1Present;
    }

    public String toString() {
        return "Jre7{zipkinV1Present=" + this.zipkinV1Present + "}";
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof Jre7) {
            Jre7 that = (Jre7)o;
            return this.zipkinV1Present == that.zipkinV1Present();
        } else {
            return false;
        }
    }

    public int hashCode() {
        int h = 1;
        h = h * 1000003;
        h ^= this.zipkinV1Present ? 1231 : 1237;
        return h;
    }
}
