package net.nostromo.dbutils;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbTemplate {

    private final Connection con;

    public DbTemplate(final Connection con) {
        this.con = con;
    }

    public <T> T query(final String sql, final DbBeanSetter<T> beanSetter, final Object[] params)
            throws SQLException, InstantiationException, IllegalAccessException, InvocationTargetException {
        final PreparedStatement ps = con.prepareStatement(sql);

        for (final Object param : params) {
            ps.setObject(1, param);
        }

        try (final ResultSet rs = ps.executeQuery()) {
            final DbRowSetter<T> rowSetter = new DbRowSetter<>(rs, beanSetter);
            final T obj = rowSetter.createRowObject();
            if (obj == null) throw new RuntimeException("no rows found");
            if (rs.next()) throw new RuntimeException("multiple rows found");
            return obj;
        }
    }
}
