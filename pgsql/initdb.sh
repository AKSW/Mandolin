#!/usr/bin/env bash
initdb db -E utf8
sh start.sh
createdb probkb
