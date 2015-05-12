package net.nostromo.dbutils;

import net.nostromo.utils.aop.LogSize;
import net.nostromo.utils.aop.LogTime;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DbTemplate {

    private final DataSource ds = DbSingleton.getInstance();

    @LogSize
    @LogTime
    public <T> List<T> readAll(final String sql, final DbBeanSetter<T> beanSetter, final Object... params)
            throws DbException {
        try (final Connection con = ds.getConnection();
             final PreparedStatement ps = con.prepareStatement(sql)) {
            for (final Object param : params) {
                ps.setObject(1, param);
            }

            try (final ResultSet rs = ps.executeQuery()) {
                final DbRowSetter<T> rowSetter = new DbRowSetter<>(rs, beanSetter);
                return rowSetter.createList();
            }
        } catch (final SQLException | ReflectiveOperationException ex) {
            throw new DbException(ex.getMessage(), ex);
        }
    }
}
