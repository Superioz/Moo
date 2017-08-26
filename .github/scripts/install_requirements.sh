#!/bin/sh
# Some color constants
YELLOW='\033[1;33m'
RED='\033[0;31m'
GREEN='\033[0;32m'
NONE='\033[0m'

# Other constants, change if needed
redis_stable_url="http://download.redis.io/redis-stable.tar.gz"
redis_folder="/opt/redis"
redis_start_at_boot=true #redis will always start at server boot

mongodb_url_part="ubuntu xenial" #ubuntu 16.04
#mongodb_url_part="ubuntu trusty" #ubuntu 14.04
#mongodb_url_part="ubuntu precise" #ubuntu 12.04
mongodb_arch="arch=amd64,arm64" #ubuntu 16.04
#mongodb_arch="arch=amd64" #ubuntu 12.04 & 14.04
mongodb_url="http://repo.mongodb.org/apt/${mongodb_url_part}/mongodb-org/3.4 multiverse" #url for download

# confirmation function (source @stackoverflow)
confirm() {
    # call with a prompt string or use a default
    read -r -p "${1:-Are you sure? [Y/n]} " response
    case "$response" in
        [yY][eE][sS]|[yY]) 
            true
            ;;
        *)
            false
            ;;
    esac
}


# ========================
# Update packages
# ========================
if confirm "Do you want to update the packages beforehand? [Y/n]"; then
    echo "${YELLOW}Update packages ..${NONE}"
    sleep 2
    sudo apt-get update
else
    echo "${RED}Skipping packages ..${NONE}"
fi

# =============================================================================
# Redis Installation
# =============================================================================
SetupRedis() {
    sudo apt-get install build-essential #installing compiler
    sudo apt-get install tcl8.5 #interpreter

    # download file and unzip/untar the file
    echo "${YELLOW}Download redis files ..${NONE}"
    mkdir "$redis_folder" && cd "$redis_folder"
    wget $redis_stable_url
    tar xvzf redis-stable.tar.gz
    cd redis-stable

    # compile and make
    echo "${YELLOW}Make redis .. (great again!)${NONE}"
    make
    make test #test files
    sudo make install #install system-wide

    # finally install
    echo "${YELLOW}Install redis ..${NONE}"
    cd utils
    sudo ./install_server.sh #built-in script, thanks god

    # start redis at boot?
    if [ "$redis_start_at_boot" = true ]; then
        sudo update-rc.d redis-server defaults
    fi

    # done
    echo "${GREEN}Redis setup done.${NONE}"
}

if confirm "Do you want to install redis? [Y/n]"; then
    echo "${YELLOW}Setup redis ..${NONE}"
    sleep 2

    # check if redis is already installed
    # if not - setup it
    if [ -d "$redis_folder" ]; then
        echo "${RED}Redis has been installed before. To reinstall delete the redis folder at \"$redis_folder${NONE}\""
    else
        SetupRedis
    fi
else
    echo "${RED}Skipping redis ..${NONE}"
fi

# =============================================================================
# MongoDB Installation
# =============================================================================
SetupMongo (){
    # setup packages
    echo "${YELLOW}Import public key ..${NONE}"
    sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 0C49F3730359A14518585931BC711F9BA15703C6
    
    echo "${YELLOW}Create list file ..${NONE}"
    echo "deb [ ${mongodb_arch} ] ${mongodb_url}" | sudo tee /etc/apt/sources.list.d/mongodb-org-3.4.list
    
    echo "${YELLOW}Reload packages ..${NONE}"
    sudo apt-get update
    
    echo "${YELLOW}Install mongodb ..${NONE}"
    sudo apt-get install -y mongodb-org
    
    echo "${YELLOW}Starting mongodb ..${NONE}"
    sudo service mongod start
    
    # done
    echo "${GREEN}Mongodb setup done.${NONE}"
}

if confirm "Do you want to install mongodb? [Y/n]"; then
    echo "${YELLOW}Setup mongodb .. ${NONE}"
    sleep 2

    # if mongo is already installed apt-get should stop while installing, as well as the other methods
    SetupMongo
else
    echo "${RED}Skipping mongodb ..${NONE}"
fi

# DONE
echo "${GREEN}DONE.${NONE}"