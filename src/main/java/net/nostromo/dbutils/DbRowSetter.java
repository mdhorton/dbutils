package net.nostromo.dbutils;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DbRowSetter<T> {

    private final Map<String, String> columnMap = new HashMap<>();
    private final ResultSet rs;
    private final DbBeanSetter<T> beanSetter;

    public DbRowSetter(final ResultSet rs, final DbBeanSetter<T> beanSetter) throws SQLException {
        this.rs = rs;
        this.beanSetter = beanSetter;

        final ResultSetMetaData meta = rs.getMetaData();
        final int columnCount = meta.getColumnCount();

        for (int idx = 0; idx < columnCount; idx++) {
            final String columnLabel = meta.getColumnLabel(idx);
            final String fieldName = parseFieldName(columnLabel);

            if (!beanSetter.fieldExists(fieldName)) continue;
            columnMap.put(columnLabel, fieldName);
        }
    }

    public T createRowObject()
            throws InstantiationException, IllegalAccessException, SQLException, InvocationTargetException {
        if (!rs.next()) return null;

        final T obj = beanSetter.newObject();

        for (final String columnLabel : columnMap.keySet()) {
            final String fieldName = columnMap.get(columnLabel);
            beanSetter.setFieldValue(fieldName, obj, rs.getObject(columnLabel));
        }

        return obj;
    }

    private String parseFieldName(final String columnLabel) {
        final String[] parts = columnLabel.split("_");

        final StringBuilder sb = new StringBuilder(columnLabel.length());
        sb.append(parts[0]);

        for (int idx = 1; idx < parts.length; idx++) {
            sb.append(parts[idx]);
        }

        return sb.toString();
    }
}
