package net.nostromo.dbutils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class DbBeanSetter<T> {

    private final Map<String, Method> methodMap = new HashMap<>();
    private final Class<T> clazz;

    public DbBeanSetter(final Class<T> clazz) {
        this.clazz = clazz;
        initMethodMap();
    }

    public void setFieldValue(final String fieldName, final T obj, final Object value)
            throws InvocationTargetException, IllegalAccessException {
        final Method method = methodMap.get(fieldName);
        if (method == null) return;
        method.invoke(obj, value);
    }

    public T newObject() throws IllegalAccessException, InstantiationException {
        return clazz.newInstance();
    }

    public boolean fieldExists(final String fieldName) {
        return methodMap.containsKey(fieldName);
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

            methodMap.put(name, method);
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
}
