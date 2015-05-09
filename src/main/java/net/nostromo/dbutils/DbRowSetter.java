package net.nostromo.dbutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbRowSetter<T> {

    private final static Logger LOG = LoggerFactory.getLogger(DbRowSetter.class);

    private final Map<String, String> columnMap = new HashMap<>();
    private final ResultSet rs;
    private final DbBeanSetter<T> beanSetter;

    public DbRowSetter(final ResultSet rs, final DbBeanSetter<T> beanSetter) throws SQLException {
        this.rs = rs;
        this.beanSetter = beanSetter;

        final ResultSetMetaData meta = rs.getMetaData();
        final int columnCount = meta.getColumnCount();

        for (int idx = 1; idx <= columnCount; idx++) {
            final String columnLabel = meta.getColumnLabel(idx);
            final String fieldName = parseFieldName(columnLabel);

            LOG.trace("{} -> {}", columnLabel, fieldName);

            if (!beanSetter.fieldExists(fieldName)) continue;
            columnMap.put(columnLabel, fieldName);
        }
    }

    public List<T> createList() throws SQLException, ReflectiveOperationException {
        final List<T> list = new ArrayList<>();

        while (rs.next()) {
            list.add(createRowObject());
        }

        return list;
    }

    public T createRow() throws SQLException, ReflectiveOperationException {
        if (!rs.next()) return null;
        return createRowObject();
    }

    private T createRowObject() throws SQLException, ReflectiveOperationException {
        final T obj = beanSetter.newObject();

        for (final String columnLabel : columnMap.keySet()) {
            final String fieldName = columnMap.get(columnLabel);
            final Object value = rs.getObject(columnLabel);
            beanSetter.setFieldValue(fieldName, obj, value);
        }

        return obj;
    }

    private String parseFieldName(final String columnLabel) {
        final String[] parts = columnLabel.split("_");

        final StringBuilder sb = new StringBuilder(columnLabel.length());
        sb.append(parts[0]);

        for (int idx = 1; idx < parts.length; idx++) {
            final String part = parts[idx];
            sb.append(part.substring(0, 1).toUpperCase());
            if (part.length() > 1) sb.append(part.substring(1));
        }

        return sb.toString();
    }
}
