package org.aurinkopg;

import java.util.Objects;

/**
 * All necessary parameters for connecting to a PostgreSQL server.
 */
public class ConnectionInfo {
    private final String host;
    private final Integer port;
    private final String pgUsername;
    private final String pgPassword;
    private final String database;

    /**
     * Create a ConnectionInfo object.
     *
     * @param host       the DNS hostname or the IP address of the database server. Cannot be null.
     * @param port       the port on the server where the database service is exposed. Cannot be null.
     * @param pgUsername database username (NOT the operating system username!) of the user as which you want to connect. Cannot be null.
     * @param pgPassword database password (NOT the operating system password!) of the user as which you want to connect. Can be null, since not all PostgreSQL users authenticate via a password.
     * @param database   one PostgreSQL database server can contain multiple databases; this is the name of the database to which you want to connect. Cannot be null.
     */
    public ConnectionInfo(
        String host,
        int port,
        String pgUsername,
        String pgPassword,
        String database) {
        Objects.requireNonNull(host, "ConnectionInfo.host cannot be null!");
        Objects.requireNonNull(port, "ConnectionInfo.port cannot be null!");
        Objects.requireNonNull(pgUsername, "ConnectionInfo.pgUsername cannot be null!");
        Objects.requireNonNull(database, "ConnectionInfo.database cannot be null!");
        this.host = host;
        this.port = port;
        this.pgUsername = pgUsername;
        this.pgPassword = pgPassword;
        this.database = database;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPgUsername() {
        return pgUsername;
    }

    public String getPgPassword() {
        return pgPassword;
    }

    public String getDatabase() {
        return database;
    }

    public String getJdbcUrl() {
        return null;
    }

    @Override
    public String toString() {
        return "ConnectionInfo{" +
            "host='" + host + '\'' +
            ", port=" + port +
            ", pgUsername='" + pgUsername + '\'' +
            ", pgPassword='" + pgPassword + '\'' +
            ", database='" + database + '\'' +
            ", jdbcUrl='" + getJdbcUrl() + '\'' +
            '}';
    }

    /**
     * Use this class to create instances of {@link ConnectionInfo} objects.
     */
    public static class Builder {
        private String host = "localhost";
        private Integer port = 5432;
        private String pgUsername = "postgres";
        private String pgPassword;
        private String database;

        /**
         * Set the host. Default is "localhost". Cannot be null.
         *
         * @param host the DNS hostname or the IP address of the database server.
         * @return the builder.
         */
        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        /**
         * Set the port. Default is 5432. Cannot be null.
         *
         * @param port the port on the server where the database service is exposed.
         * @return the builder.
         */
        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        /**
         * Set the database username. Default is "postgres". Cannot be null.
         *
         * @param pgUsername database username (NOT the operating system username!) of the user as which you want to connect.
         * @return the builder.
         */
        public Builder setPgUsername(String pgUsername) {
            this.pgUsername = pgUsername;
            return this;
        }

        /**
         * Set the database password. There is no default. Can be null, since not all PostgreSQL users authenticate via a password.
         *
         * @param pgPassword database password (NOT the operating system password!) of the user as which you want to connect.
         * @return the builder.
         */
        public Builder setPgPassword(String pgPassword) {
            this.pgPassword = pgPassword;
            return this;
        }

        /**
         * Set the database. There is no default. Cannot be null.
         *
         * @param database one PostgreSQL database server can contain multiple databases; this is the name of the database to which you want to connect.
         * @return the builder.
         */
        public Builder setDatabase(String database) {
            this.database = database;
            return this;
        }

        /**
         * Create a ConnectionInfo instance.
         *
         * @return a ConnectionInfo from the parameters given to this Builder.
         */
        public ConnectionInfo build() {
            Objects.requireNonNull(this.host, "ConnectionInfo.host cannot be null!");
            Objects.requireNonNull(this.port, "ConnectionInfo.port cannot be null!");
            Objects.requireNonNull(this.pgUsername, "ConnectionInfo.pgUsername cannot be null!");
            Objects.requireNonNull(this.database, "ConnectionInfo.database cannot be null!");
            return new ConnectionInfo(
                this.host,
                this.port,
                this.pgUsername,
                this.pgPassword,
                this.database);
        }
    }
}
