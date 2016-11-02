#!/bin/bash
echo "Mandolin Installer"

echo "Compiling Mandolin..."
export MAVEN_OPTS=-Xss4m
mvn -q clean compile assembly:single

read -p "Download datasets into ./data/? " -n 1 -r
echo    # (optional) move to a new line
if [[ $REPLY =~ ^[Yy]$ ]]
then
    # do stuff
	echo "Downloading datasets..."
	wget -q https://s3-eu-west-1.amazonaws.com/anonymous-folder/data.zip
	unzip -qq data.zip && rm -rf data.zip
fi

read -p "Download and install PostgreSQL? [Ubuntu systems only] " -n 1 -r
echo    # (optional) move to a new line
if [[ $REPLY =~ ^[Yy]$ ]]
then
    # do stuff
	echo "Downloading PostgreSQL..."
	wget -q http://oscg-downloads.s3.amazonaws.com/packages/postgresql-9.4.8-1-x64-bigsql.deb
	pgdr=`pwd`"/postgres/"
	echo "Installing PostgreSQL in "$pgdr
	dpkg-deb -x postgresql-9.4.8-1-x64-bigsql.deb $pgdr && rm -rf postgresql-9.4.8-1-x64-bigsql.deb
	pgdir=$pgdr"opt/postgresql/pg94" # changing to home
	echo "# GENERAL CONFIGURATION FOR MANDOLIN" > mandolin.properties
	echo "pgsql_home="$pgdir >> mandolin.properties
	echo "pgsql_username="`whoami` >> mandolin.properties
	echo "pgsql_password=" >> mandolin.properties
	echo "pgsql_url=localhost" >> mandolin.properties
else
	echo "# GENERAL CONFIGURATION FOR MANDOLIN" > mandolin.properties
	read -p "PostgreSQL home? " pgdir
	echo "pgsql_home="$pgdir >> mandolin.properties
	read -p "PostgreSQL username? " puname
	echo "pgsql_username="$puname >> mandolin.properties
	read -sp "PostgreSQL password? " ppwd
	echo "pgsql_password="$ppwd >> mandolin.properties
	read -p "PostgreSQL host? " phost
	echo "pgsql_url="$phost >> mandolin.properties
fi

echo "Initializing database..."
cd pgsql && $pgdir/bin/initdb db -E utf8
echo "Starting server..."
$pgdir/bin/pg_ctl start -D db/
echo "Creating DB..."
$pgdir/bin/createdb probkb && cd ..

echo "Done."
