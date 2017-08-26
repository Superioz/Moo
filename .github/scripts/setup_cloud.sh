#!/bin/sh
# Some color constants
YELLOW='\033[1;33m'
RED='\033[0;31m'
GREEN='\033[0;32m'
NONE='\033[0m'

cloud_folder="cloud"
cloud_logs_folder="logs"
cloud_config_folder="configuration"
cloud_jars_folder="jars"

# ========================
# 
# ========================
SetupCloud (){
    echo "${YELLOW}Creating folders .. ${NONE}"
    mkdir $cloud_folder && cd $cloud_folder
    mkdir $cloud_config_folder && mkdir $cloud_logs_folder && mkdir $cloud_jars_folder
    
    # Current folder: cloud
    echo "${YELLOW}Fetching jars .. ${NONE}"
    sh ../fetch_jars.sh $cloud_jars_folder
    cp $cloud_jars_folder/cloud.jar cloud.jar
    echo "${GREEN}Finished setting up the cloud. For next steps read the Instructions on the Github page.${NONE}"
}

echo "${YELLOW}Setup the cloud .. ${NONE}"
sleep 2

# if the cloud folder already exists
if [ -d "${cloud_folder}" ]; then
    echo "${RED}Cloud has been setup before. To reinstall delete the cloud folder at \"$cloud_folder${NONE}\""
else
    SetupCloud
fi