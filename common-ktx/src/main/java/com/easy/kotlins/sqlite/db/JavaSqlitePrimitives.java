package com.easy.kotlins.sqlite.db;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

class JavaSqlitePrimitives {
    private JavaSqlitePrimitives() {}

    static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS;
    
    static {
        PRIMITIVES_TO_WRAPPERS = new HashMap<Class<?>, Class<?>>();
        PRIMITIVES_TO_WRAPPERS.put(boolean.class, Boolean.class);
        PRIMITIVES_TO_WRAPPERS.put(byte.class, Byte.class);
        PRIMITIVES_TO_WRAPPERS.put(char.class, Character.class);
        PRIMITIVES_TO_WRAPPERS.put(double.class, Double.class);
        PRIMITIVES_TO_WRAPPERS.put(float.class, Float.class);
        PRIMITIVES_TO_WRAPPERS.put(int.class, Integer.class);
        PRIMITIVES_TO_WRAPPERS.put(long.class, Long.class);
        PRIMITIVES_TO_WRAPPERS.put(short.class, Short.class);
        PRIMITIVES_TO_WRAPPERS.put(void.class, Void.class);
    }

}
