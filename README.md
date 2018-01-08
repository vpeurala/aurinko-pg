# Aurinko

A snapshot tool for [PostgreSQL](https://www.postgresql.org/) and Java (or other JVM-based languages).

## Requirements

* Java 8 or newer
* PostgreSQL client library

## Influenced by Stellar

Aurinko is very much influenced by [Stellar](https://github.com/fastmonkeys/stellar) (https://github.com/fastmonkeys/stellar), which does similar things but is written in Python. Stellar obviously has a lot more features, since it has been in development since 2014.

Some of the goals of Aurinko are different from Stellar's: for example, Stellar also supports [MySQL](https://www.mysql.com/), while Aurinko is never going to support any other databases than [PostgreSQL](https://www.postgresql.org/).

A funny coincidence is that Stellar and Aurinko are both from Helsinki, Finland. Stellar is written mainly by *Teemu Kokkonen* and *Pekka Pöyry* who work at [Fast Monkeys](http://www.fastmonkeys.com/) (plus a community of 10+ contributors), while Aurinko is written by *Ville Peurala* who works at [Wunderdog](https://wunder.dog).

## TODO

* Initialize the database state in the beginning of test runs, before running a single test. Use Docker for this.
* Versioning policy so that version is specified in only one place.
* Check that the PostgreSQL user used has superuser privileges.
* Test the Docker workflows.
