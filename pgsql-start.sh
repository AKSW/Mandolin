#!/usr/bin/env bash
echo "\n\tStarting PostgreSQL database... Run 'sh pgsql-stop.sh' to terminate it.\n" && cd pgsql && sh start.sh && cd ..
