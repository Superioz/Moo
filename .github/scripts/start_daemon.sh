#!/bin/sh
# If you want to increase the maximum amount of memory change the '-Xmx420M' value (e.g. to '-Xmx1024M')
screen -S daemon -m java -Xmx420M -jar daemon.jar