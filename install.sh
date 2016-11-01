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
	pgdir=`pwd`"/postgres/"
	echo "Installing PostgreSQL in "$pgdir
	dpkg-deb -x postgresql-9.4.8-1-x64-bigsql.deb $pgdir && rm -rf postgresql-9.4.8-1-x64-bigsql.deb
	echo "# GENERAL CONFIGURATION FOR MANDOLIN" > mandolin.properties
	echo "pgsql_home="$pgdir"opt/postgresql/pg94" >> mandolin.properties
	echo "pgsql_username="`whoami` >> mandolin.properties
	echo "pgsql_password=" >> mandolin.properties
	echo "pgsql_url=localhost" >> mandolin.properties
else
	echo "# GENERAL CONFIGURATION FOR MANDOLIN" > mandolin.properties
	echo "pgsql_home=" >> mandolin.properties
	echo "pgsql_username=" >> mandolin.properties
	echo "pgsql_password=" >> mandolin.properties
	echo "pgsql_url=localhost" >> mandolin.properties
	echo "Please insert PostgreSQL settings into file 'mandolin.properties'."
fi

echo "Done."
