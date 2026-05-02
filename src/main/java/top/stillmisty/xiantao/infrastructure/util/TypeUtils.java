package top.stillmisty.xiantao.infrastructure.util;

public final class TypeUtils {

    private TypeUtils() {
    }

    public static Long toLong(Object value) {
        if (value instanceof Long longVal) return longVal;
        if (value instanceof Integer intVal) return intVal.longValue();
        if (value instanceof Number number) return number.longValue();
        return null;
    }
}
