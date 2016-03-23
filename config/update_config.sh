#!/bin/bash
# script for replacing generic config files by local ones (local config files must be up-to-date!)
mkdir -p generic

# saving generic config files
cp ../anhalytics-annotate/annotate.properties generic/
cp ../anhalytics-commons/commons.properties generic/
cp ../anhalytics-harvest/harvest.properties generic/
cp ../anhalytics-index/index.properties generic/

# writing local config files
cp local/annotate.properties ../anhalytics-annotate/
cp local/commons.properties ../anhalytics-commons/
cp local/harvest.properties ../anhalytics-harvest/
cp local/index.properties ../anhalytics-index/
