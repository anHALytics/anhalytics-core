#!/bin/bash
# script for replacing local config files by generic ones before commit

mkdir -p local

# writing generic config files
cp generic/annotate.properties local/
cp generic/commons.properties local/
cp generic/harvest.properties local/
cp generic/index.properties local/
cp generic/ingest.properties local/
cp generic/test.properties local/
