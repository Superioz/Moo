#!/bin/sh
# If you want to increase the maximum amount of memory change the '-Xmx512M' value (e.g. to '-Xmx1024M')
screen -S cloud -m java -Xmx512M -jar cloud.jar