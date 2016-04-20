#!/bin/bash
# script for replacing generic config files by local ones (local config files must be up-to-date!)
mkdir -p generic

# writing local config files
cp local/annotate.properties generic/
cp local/commons.properties generic/
cp local/harvest.properties generic/
cp local/index.properties generic/
cp local/kb.properties generic/
cp local/test.properties generic/
