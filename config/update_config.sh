#!/bin/bash
# script for replacing generic config files by local ones (local config files must be up-to-date!)

# saving generic config files
cp ../anhalytics-frontend/src/main/webapp/js/resource/config.js generic/
cp ../anhalytics-annotate/annotate.properties generic/
cp ../anhalytics-commons/commons.properties generic/
cp ../anhalytics-harvest/harvest.properties generic/
cp ../anhalytics-index/index.properties generic/

# writing local config files
cp local/config.js ../anhalytics-frontend/src/main/webapp/js/resource/
cp local/annotate.properties ../anhalytics-annotate/
cp local/commons.properties ../anhalytics-commons/
cp local/harvest.properties ../anhalytics-harvest/
cp local/index.properties ../anhalytics-index/ 
