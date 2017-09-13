[![License](https://img.shields.io/badge/license-GPLv2-blue.svg)](https://github.com/Superioz/MooProject/blob/master/LICENSE) [![Build Status](https://travis-ci.org/Superioz/MooProject.svg?branch=master)](https://travis-ci.org/Superioz/MooProject) 
[![CircleCI](https://circleci.com/gh/Superioz/MooProject/tree/master.svg?style=shield)](https://circleci.com/gh/Superioz/MooProject/tree/master)
[![Wiki](https://img.shields.io/badge/wiki-click%20here-%2333bbff.svg)](https://github.com/Superioz/MooProject/wiki) [![Jenkins](https://img.shields.io/badge/jenkins-click%20here-b21a1a.svg)](http://ci.superioz.de:8080/job/MooProject/)

![Logo](/.github/assets/moo_banner_new.png "Logo")
> Logo made by Karumata | Banner mady by [Slickz](https://www.youtube.com/SlickzDE)

# MooProject
A minecraft network communication system based on [Netty](https://github.com/netty) (Some call it 'Cloud system', which is not quite correct).

This project was originally written for a minecraft network, but after it went down I decided to share it on Github. So use this on your own responsibility.  
**If you find any flaws feel free to open up an [Issue](https://github.com/Superioz/MooProject/issues/new) or a [Pull Request](https://github.com/Superioz/MooProject/compare)**  
**It is recommended to setup this Cloud on an Unix system**

# Getting started
1. Download the .sh files from the [/scripts](https://github.com/Superioz/MooProject/tree/master/.github/scripts) folder (`Raw` -> `Rightlick` -> `Save site`) and save them inside a folder where you want the program to be installed (e.g.: `/home/minecraft/`).  
2. Run **install_requirements.sh** to install Redis and MongoDB (you can skip this step if you've already installed them or if you want to install them yourself).
3. Run **setup_cloud.sh** to create the `/cloud` folder and to download the files from [Jenkins](http://ci.superioz.de:8080/job/MooProject/).
4. Put the **start_cloud.sh** into the `/cloud` folder and run the script to start the cloud once - It'll fail but now you're able to configure the MongoDB connection inside the `/cloud/configuration/config.yml`.  
It could now look like this:
```JSON
{
  "debug": false,
  "redis-config": "redis_config.json",
  "max-ram-usage": 60,
  "netty": {
    "host": "localhost",
    "port": 8000,
    "whitelist": {
      "activated": true,
      "ips": [
        "127.0.0.1"
      ]
    }
  },
  "database": {
    "hostname": "localhost",
    "database": "dbName",
    "port": 27017,
    "user": "user",
    "password": "password"
  }
}
```
5. For configuring the Redis connection go to `redis_config.json`  
For further informations about the `redis_config.json` visit the [Redisson Wiki](https://github.com/redisson/redisson/wiki/2.-Configuration)  
6. If the configuration is correct the Cloud should be able to start successfully now.

# <a name="thunder"></a>Setting up Thunder
Thunder is the bungee part of the cloud, so setting it up is fairly simple.
1. Download the bungee file from the [md-5 Jenkins](https://ci.md-5.net/job/BungeeCord/) server and put it inside a folder of your choice (e.g.: `/home/minecraft/bungee/`).
2. Create the `start.sh` to start the bungee and put it into the same folder as the Bungeecord jar.
  It could look like this:
```SH
#!/bin/sh
screen -S bungeecord -dm java -jar -Xmx1024M yourBungeecordFile.jar
```
3. Start the bungeecord once to create the folders and files.
4. Put the `/cloud/jars/thunder.jar` into the `/plugins` folder.
5. Now start the bungee again and edit the `/plugins/Thunder/config.json` to set the instance name, cloud-ip and cloud-port (If no changes needed leave it as it is). If you want to edit the Redis config, you can do it with the same file as the cloud (`redis_config.json`).
6. Done.

# Setting up Daemons
A daemon is in this setup a program which runs in the background and manages the server dynamically.  

It's basically the same setup as [Thunder](#thunder) but instead of downloading the bungeecord jar get the `/cloud/jars/daemon.jar` and put it inside a folder of your choice and instead of creating a script take the **start_daemon.sh** script. Start the daemon and configure it just like the you did with the Thunder configuration files.

# Setting up patterns
A pattern is a pre-defined server folder which will be used to start multiple servers of one instance. It is recommended to use something like RSync (Thanks @Doppelnull) to synchronize data between the servers. Creating a pattern:
1. Use `/pattern create <...>` inside the Cloud console.
2. After creation in the database go to a daemon and inside the `patterns/nameOfYourCreatedPattern` put all your server files (You know how to obtain them hopefully) inside there (plugins, worlds, server.jar, start.sh, ...). Finish it up by putting the `cloud/jars/lightning.jar` inside the plugins folder as well.  
For different pattern management (deletion, ..) use `/pattern` inside the cloud console, you'll get sub commands as help.
