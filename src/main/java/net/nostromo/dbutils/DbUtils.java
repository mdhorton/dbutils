package net.nostromo.dbutils;

import net.nostromo.common.NostromoProperties;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;

public class DbUtils {

    public static DataSource createDataSource(final NostromoProperties props) {
        final BasicDataSource ds = new BasicDataSource();

        ds.setDriverClassName(props.get("db.driver"));
        ds.setUrl(props.get("db.url"));
        ds.setUsername(props.get("db.username"));
        ds.setPassword(props.get("db.password"));

        ds.setInitialSize(props.getInt("dbcp.initial-size"));
        ds.setMinIdle(props.getInt("dbcp.min-idle"));
        ds.setMaxTotal(props.getInt("dbcp.max-total"));
        ds.setMaxWaitMillis(props.getLong("dbcp.max-wait"));
        ds.setDefaultAutoCommit(props.getBoolean("dbcp.default-auto-commit"));
        ds.setTestOnBorrow(props.getBoolean("dbcp.test-on-borrow"));
        ds.setValidationQuery(props.get("dbcp.validation-query"));
        ds.setMaxConnLifetimeMillis(props.getLong("dbcp.max-con-lifetime"));

        return ds;
    }
}
