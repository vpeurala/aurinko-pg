package org.aurinkopg;

/**
 * All necessary parameters for connecting to a PostgreSQL server.
 */
public class ConnectionInfo {
    private final String host;
    private final int port;
    private final String pgUsername;
    private final String pgPassword;
    private final String database;

    /**
     * Create a ConnectionInfo object.
     *
     * @param host       the DNS hostname or the IP address of the database server.
     * @param port       the port on the server where the database service is exposed.
     * @param pgUsername database username (NOT the operating system username!) of the user as which you want to connect.
     * @param pgPassword database password (NOT the operating system password!) of the user as which you want to connect.
     * @param database   one PostgreSQL database server can contain multiple databases; this is the name of the database to which you want to connect.
     */
    public ConnectionInfo(String host, int port, String pgUsername, String pgPassword, String database) {
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

    public static class Builder {
        private String host;
        private int port;
        private String pgUsername;
        private String pgPassword;
        private String database;

        /**
         * Set the host.
         *
         * @param host the DNS hostname or the IP address of the database server.
         * @return the builder.
         */
        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        /**
         * Set the port.
         *
         * @param port the port on the server where the database service is exposed.
         * @return the builder.
         */
        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        /**
         * Set the database username.
         *
         * @param pgUsername database username (NOT the operating system username!) of the user as which you want to connect.
         * @return the builder.
         */
        public Builder setPgUsername(String pgUsername) {
            this.pgUsername = pgUsername;
            return this;
        }

        /**
         * Set the database password.
         *
         * @param pgPassword database password (NOT the operating system password!) of the user as which you want to connect.
         * @return the builder.
         */
        public Builder setPgPassword(String pgPassword) {
            this.pgPassword = pgPassword;
            return this;
        }

        /**
         * Set the database.
         *
         * @param database one PostgreSQL database server can contain multiple databases; this is the name of the database to which you want to connect.
         * @return the builder.
         */
        public Builder setDatabase(String database) {
            this.database = database;
            return this;
        }
    }
}
