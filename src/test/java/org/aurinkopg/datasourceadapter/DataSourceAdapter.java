package org.aurinkopg.datasourceadapter;

import org.aurinkopg.postgresql.Database;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class DataSourceAdapter implements DataSource {
    private final Database database;
    private PrintWriter logWriter;
    private int loginTimeout;

    public DataSourceAdapter(Database database) {
        this.database = database;
        this.logWriter = new PrintWriter(System.out);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return database.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return database.getConnection();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (DataSourceAdapter.class.equals(iface)) {
            return iface.cast(this);
        } else {
            throw new SQLException(
                "Cannot unwrap " +
                    getClass().getName() +
                    " to " +
                    iface.getName() +
                    ".");
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return DataSourceAdapter.class.equals(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return logWriter;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        this.logWriter = out;
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return loginTimeout;
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        this.loginTimeout = seconds;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException(
            "Feature 'getParentLogger()' is not supported by class '" +
                getClass().getName() +
                "'.");
    }
}
