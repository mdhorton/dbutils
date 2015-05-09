package net.nostromo.dbutils;

import net.nostromo.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

public class DbBeanSetter<T> {

    private static final Logger LOG = LoggerFactory.getLogger(DbBeanSetter.class);

    private final Map<String, MethodItem> methodMap = new HashMap<>();
    private final Class<T> clazz;

    public DbBeanSetter(final Class<T> clazz) {
        this.clazz = clazz;
        initMethodMap();
    }

    public void setFieldValue(final String fieldName, final T obj, final Object value)
            throws InvocationTargetException, IllegalAccessException {
        final MethodItem methodItem = methodMap.get(fieldName);
        if (methodItem == null) return;

        final Method method = methodItem.getMethod();
        final Class<?> type = methodItem.getType();
        final Object converted = convert(type, value);

        if (LOG.isTraceEnabled()) {
            LOG.trace("{} -> {} -> {} ({}) -> {} ({})", fieldName, type, value.getClass(), value,
                    converted.getClass(), converted);
        }

        method.invoke(obj, converted);
    }

    public T newObject() throws IllegalAccessException, InstantiationException {
        return clazz.newInstance();
    }

    public boolean fieldExists(final String fieldName) {
        return methodMap.containsKey(fieldName);
    }

    private Object convert(final Class<?> target, final Object value) {
        if (target == String.class) return string(value);
        else if (value instanceof BigDecimal) return bigDecimal(target, value);
        else if (value instanceof Timestamp) return timestamp(target, value);
        return value;
    }

    private Object string(final Object value) {
        if (value instanceof BigDecimal) return ((BigDecimal) value).toPlainString();
        return value.toString();
    }

    private Object bigDecimal(final Class<?> type, final Object value) {
        if (type != BigDecimal.class) {
            final BigDecimal o = (BigDecimal) value;

            if (type == BigInteger.class) return o.toBigInteger();
            if (type == double.class || type == Double.class) return o.doubleValue();
            if (type == float.class || type == Float.class) return o.floatValue();
            if (type == long.class || type == Long.class) return o.longValue();
            if (type == int.class || type == Integer.class) return o.intValue();
            if (type == short.class || type == Short.class) return o.shortValue();
            if (type == byte.class || type == Byte.class) return o.byteValue();
        }

        return value;
    }

    private Object timestamp(final Class<?> type, final Object value) {
        if (type != Timestamp.class) {
            final Timestamp o = (Timestamp) value;

            if (type == LocalDateTime.class) {
                final Instant instant = Instant.ofEpochMilli(o.getTime());
                return LocalDateTime.ofInstant(instant, Utils.NY);
            }

            if (type == long.class || type == Long.class) return o.getTime();
        }

        return value;
    }

    private void initMethodMap() {
        final Map<String, Field> fieldMap = new HashMap<>();
        for (final Field field : getAllFields(clazz, null)) {
            fieldMap.put(field.getName(), field);
        }

        for (final Method method : getAllMethods(clazz, null)) {
            // must have void return type
            if (!method.getReturnType().equals(Void.TYPE)) continue;

            // must have exactly 1 parameter
            final Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length != 1) continue;

            // must be a setter
            final String name = methodSetterCheck(method.getName());
            if (name == null) continue;

            // must have a matching field
            final Field field = fieldMap.get(name);
            if (field == null) continue;

            // field type must match setter
            if (!field.getType().equals(paramTypes[0])) continue;

            methodMap.put(name, new MethodItem(method, paramTypes[0]));
        }
    }

    private List<Field> getAllFields(final Class<?> startParent, final Class<?> parent) {
        final List<Field> fields = new ArrayList<>();
        Collections.addAll(fields, startParent.getDeclaredFields());

        final Class<?> superParent = startParent.getSuperclass();
        if (superParent != null && (parent == null || !superParent.equals(parent))) {
            fields.addAll(getAllFields(superParent, parent));
        }

        return fields;
    }

    private List<Method> getAllMethods(final Class<?> startParent, final Class<?> parent) {
        final List<Method> methods = new ArrayList<>();
        Collections.addAll(methods, startParent.getDeclaredMethods());

        final Class<?> superParent = startParent.getSuperclass();
        if (superParent != null && (parent == null || !superParent.equals(parent))) {
            methods.addAll(getAllMethods(superParent, parent));
        }

        return methods;
    }

    private String methodSetterCheck(final String name) {
        if (!name.startsWith("set")) return null;
        final int len = name.length();
        if (len < 4) return null;

        final String first = name.substring(3, 4).toLowerCase();
        return len == 4 ? first : first + name.substring(4);
    }

    private class MethodItem {

        private final Method method;
        private final Class<?> type;

        public MethodItem(final Method method, final Class<?> type) {
            this.method = method;
            this.type = type;
        }

        public Method getMethod() {
            return method;
        }

        public Class<?> getType() {
            return type;
        }
    }
}
