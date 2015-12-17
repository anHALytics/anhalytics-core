#!/bin/bash
# script for replacing generic config files by local ones (local config files must be up-to-date!)
mkdir -p generic

# saving generic config files
cp ../anhalytics-frontend/src/main/webapp/js/resource/config.js generic/
cp ../anhalytics-annotate/src/main/resources/annotate.properties generic/
cp ../anhalytics-commons/src/main/resources/commons.properties generic/
cp ../anhalytics-harvest/src/main/resources/harvest.properties generic/
cp ../anhalytics-index/src/main/resources/index.properties generic/

# writing local config files
cp local/config.js ../anhalytics-frontend/src/main/webapp/js/resource/
cp local/annotate.properties ../anhalytics-annotate/src/main/resources/
cp local/commons.properties ../anhalytics-commons/src/main/resources/
cp local/harvest.properties ../anhalytics-harvest/src/main/resources/
cp local/index.properties ../anhalytics-index/src/main/resources/
