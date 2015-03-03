#!/bin/sh

#Do android specific init requirement first
setprop bluetooth.hciattach true

#Maximum number of attempts to try and init 
#the hci device. 
max_retries=10 

for idx in $(seq 1 ${max_retries}); do 
	echo 1 > /sys/module/hci_smd/parameters/hcismd_set 
	if [ -e /sys/class/bluetooth/hci0 ] ;then
		#hci device has been initialized
		exit 0 
	fi
	sleep 1 
done 

#max retries exhausted. Something is wrong
exit 1 
