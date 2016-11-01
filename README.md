Mandolin
========

*Markov Logic Networks for Discovering Links.*

## Requirements

* Java 1.8+
* Maven
* PostgreSQL 9.4.x - [Ubuntu/Debian binaries](http://oscg-downloads.s3.amazonaws.com/packages/postgresql-9.4.8-1-x64-bigsql.deb)
* Gurobi solver - [Get academic license](http://www.gurobi.com/academia/academia-center)
* Unzip

## Quick start

* Download and decompress file [ZIPFILENAME]
* Edit `mandolin.properties` with PostgreSQL settings.
* Run `sh quickstart.sh`

## Experiments

* Run `sh demo.sh` for a quick test.

## Install from sources

Clone and compile project:

```bash
git clone https://github.com/mommi84/Mandolin.git
cd Mandolin
sh compile.sh
```

Update file `mandolin.properties` with the host parameters. Mind the missing `/` at the end of the paths.

### Initialize database

To be called once for all. If the directory `./pgsql/db/` exists, skip this step.

```bash
sh pgsql-init.sh
sh pgsql-start.sh
sh pgsql-create.sh
```

## Usage

If the database was not started before, run:

```bash
sh pgsql-start.sh
```

then, run:

```bash
java -Xmx8g -jar target/Mandolin-VERSION-jar-with-dependencies.jar plain eval/mandolin-test src/test/resources/AKSW-one-out.nt http://mandolin.aksw.org/example/topic 95 10 95 false false false
```

Discovered links can be found at `./eval/mandolin-test/discovered_*.nt`, where `*` is a threshold.

## Q&A

### How to install PostgreSQL on Ubuntu without root access?

```bash
wget http://oscg-downloads.s3.amazonaws.com/packages/postgresql-9.4.8-1-x64-bigsql.deb
dpkg-deb -x postgresql-9.4.8-1-x64-bigsql.deb ~/postgres/
```

Afterwards, update file `mandolin.properties` with:

```
pgsql_home=/home/USER/postgres/opt/postgresql/pg94
```

## License(s)

**Mandolin** is licensed under GNU General Public License v2.0.
**AMIE** is licensed under Creative Commons Attribution-NonComercial license v3.0.
**ProbKB** is licensed under the BSD license.
**RockIt** is licensed under the MIT License.