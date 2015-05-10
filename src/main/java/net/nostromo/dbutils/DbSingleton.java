package net.nostromo.dbutils;

import net.nostromo.utils.PropertiesSingleton;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;

public class DbSingleton {

    private static class Singleton {
        private static final DataSource INSTANCE = dataSource();

        private static DataSource dataSource() {
            final PropertiesSingleton props = PropertiesSingleton.getInstance();
            final BasicDataSource bds = new BasicDataSource();

            bds.setDriverClassName(props.get("db.driver"));
            bds.setUrl(props.get("db.url"));
            bds.setUsername(props.get("db.username"));
            bds.setPassword(props.get("db.password"));

            bds.setInitialSize(props.getInt("dbcp.initial-size"));
            bds.setMinIdle(props.getInt("dbcp.min-idle"));
            bds.setMaxTotal(props.getInt("dbcp.max-total"));
            bds.setMaxWaitMillis(props.getLong("dbcp.max-wait"));
            bds.setDefaultAutoCommit(props.getBoolean("dbcp.default-auto-commit"));
            bds.setTestOnBorrow(props.getBoolean("dbcp.test-on-borrow"));
            bds.setValidationQuery(props.get("dbcp.validation-query"));
            bds.setMaxConnLifetimeMillis(props.getLong("dbcp.max-con-lifetime"));

            return bds;
        }
    }

    public static DataSource getInstance() {
        return Singleton.INSTANCE;
    }
}
