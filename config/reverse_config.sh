#!/bin/bash
# script for replacing local config files by generic ones before commit

# saving local config files
cp ../anhalytics-frontend/src/main/webapp/js/resource/config.js local/
cp ../anhalytics-annotate/annotate.properties local/
cp ../anhalytics-commons/commons.properties local/
cp ../anhalytics-harvest/harvest.properties local/
cp ../anhalytics-index/index.properties local/

# writing generic config files
cp generic/config.js ../anhalytics-frontend/src/main/webapp/js/resource/
cp generic/annotate.properties ../anhalytics-annotate/
cp generic/commons.properties ../anhalytics-commons/
cp generic/harvest.properties ../anhalytics-harvest/
cp generic/index.properties ../anhalytics-index/ 
