Mandolin
========

*Markov Logic Networks for Discovering Links.*

## Requirements

* Java 1.8+
* Maven
* PostgreSQL 9.4.x
* Gurobi solver - [Get academic license](http://www.gurobi.com/academia/academia-center)
* Unzip

## Quick start

* Download and decompress [Mandolin-0.4.0.zip](https://github.com/mommi84/Mandolin/..........) <-- this ZIP will contain all necessary files
* Run `bash install.sh`

## Experiments

* **TODO**

```bash
java -Xmx1g -jar target/Mandolin-VERSION-jar-with-dependencies.jar plain 
```

Discovered links can be found at `./eval/mandolin-test/discovered_*.nt`, where `*` is a threshold.

## Manual install

* Clone project:

```bash
git clone https://github.com/mommi84/Mandolin.git
cd Mandolin
```

* Get PostgreSQL 9.4.x - [Ubuntu/Debian binaries](http://oscg-downloads.s3.amazonaws.com/packages/postgresql-9.4.8-1-x64-bigsql.deb)

### Alternative 1

* Launch `bash install.sh -c`

### Alternative 2

* Insert PostgreSQL setting parameters into a file `./mandolin.properties`. Example:

```properties
# GENERAL CONFIGURATION FOR MANDOLIN
pgsql_home=/usr/local/Cellar/postgresql/9.4.1
pgsql_username=tom
pgsql_password=
pgsql_url=localhost
```

* Download [data](https://s3-eu-west-1.amazonaws.com/anonymous-folder/data.zip)

* Compile project:

```bash
export MAVEN_OPTS=-Xss4m
mvn clean compile assembly:single
```

## Database handler

After using Mandolin, stop the DB instance with:

```bash
sh pgsql-stop.sh
```

The instance can be restarted with:

```bash
sh pgsql-start.sh
```

## License(s)

**Mandolin** is licensed under GNU General Public License v2.0.
**AMIE** is licensed under Creative Commons Attribution-NonComercial license v3.0.
**ProbKB** is licensed under the BSD license.
**RockIt** is licensed under the MIT License.