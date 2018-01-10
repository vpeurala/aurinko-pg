## TODO

1. Read library version to Gradle build from `org.aurinkopg.GlobalConstants.LIBRARY_VERSION`.
1. Upload to some public Maven repository.
---
1. Initialize the database state in the beginning of test runs, before running a single test. Use Docker for this.
1. Check that the PostgreSQL user used has superuser privileges.
1. Test the Docker workflows.
1. Create a database for Aurinko metadata so that snapshots can be tracked.
1. Test with Java 9.
1. Write some simple usage instructions.
1. Create a Continuous Integration setup (Travis?)
1. Move the database password setting of db user jaanmurtaja to the Dockerfile instead of init.sql.
