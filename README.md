Mandolin
========

*Markov Logic Networks for Discovering Links.*

## Requirements

* Java 1.8
* Maven
* PostgreSQL 9.4.x - [Linux binaries](http://oscg-downloads.s3.amazonaws.com/packages/postgresql-9.4.8-1-x64-bigsql.deb)
* Unzip

## Binaries

* Download and decompress file [ZIPFILENAME]
* Edit `mandolin.properties` with PostgreSQL paths
* Run `sh mandolin-demo.sh`

## Install from sources

Clone and compile project:

```bash
git clone https://github.com/mommi84/Mandolin.git
cd Mandolin
export MAVEN_OPTS=-Xss2m
mvn clean compile assembly:single
```

Update file `mandolin.properties` with the host parameters. Mind the missing `/` at the end of the paths.

## Usage

```bash
java -Xmx8g -jar ...
```

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