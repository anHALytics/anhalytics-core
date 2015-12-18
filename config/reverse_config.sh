#!/bin/bash
# script for replacing local config files by generic ones before commit

mkdir -p local

# saving local config files
cp ../anhalytics-annotate/src/main/resources/annotate.properties local/
cp ../anhalytics-commons/src/main/resources/commons.properties local/
cp ../anhalytics-harvest/src/main/resources/harvest.properties local/
cp ../anhalytics-index/src/main/resources/index.properties local/

# writing generic config files
cp generic/annotate.properties ../anhalytics-annotate/src/main/resources/
cp generic/commons.properties ../anhalytics-commons/src/main/resources/
cp generic/harvest.properties ../anhalytics-harvest/src/main/resources/
cp generic/index.properties ../anhalytics-index/src/main/resources/
