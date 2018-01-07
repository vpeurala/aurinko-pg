package org.aurinkopg;

import java.sql.SQLException;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Snapshot {
    private final String name;

    public Snapshot(String name) {
        validateSnapshotName(name);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * See https://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS.
     *
     * @param name a name you would like to use as a snapshot DB name.
     * @throws SQLException if the name does not conform to the rules.
     */
    private void validateSnapshotName(String name) throws IllegalArgumentException {
        Objects.requireNonNull(name, "The name of a Snapshot cannot be null!");
        int lengthInBytes = name.getBytes(UTF_8).length;
        if (lengthInBytes > 63) {
            throw new IllegalArgumentException(
                "Your snapshot name is too long. " +
                    "The maximum length is 63 bytes. " +
                    "You tried to use '" +
                    name +
                    "', which is " +
                    lengthInBytes +
                    " bytes long.");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("You tried to use an empty string as a snapshot name.");
        }
        if (!name.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Your snapshot name '" +
                name +
                "'did not match the pattern of acceptable database names. " +
                "See https://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS for the rules.");
        }
    }
}
