#!/bin/sh

echo "Waiting for container to come up ..."
lxc-wait -n android -s RUNNING

# Wait until the container is far enough along that libhybris has something to
# talk to, then let the rest of the stack start.
#
# There is no single property that says this on every device. This used to wait
# for sys.init_boot_completed, which a lot of Halium 9 bases never set at all;
# it was then changed to require a livedisplay service, which is a LineageOS HAL
# that plenty of bases simply do not ship - sargo has no livedisplay anywhere in
# system or vendor, so the loop span forever, android-system.service hit its
# start-post timeout and failed, and the device sat at the boot logo with a fully
# working Android container underneath. tenderloin had already run into this and
# forked this whole script just to wait on gralloc instead.
#
# So accept any of the signals that mean the graphics side is alive, rather than
# naming one HAL and hoping every device has it. What LuneOS actually needs is
# the composer/allocator, so hwcomposer or gralloc running is the real condition;
# livedisplay and sys.init_boot_completed stay as fallbacks for bases where those
# did work.
#
# The wait is bounded. The old loop was infinite, so a device that never produced
# the property could only fail by systemd killing start-post, which reports as a
# timeout with no indication of what was being waited for. Giving up here instead
# lets the rest of the stack try, and leaves the reason in the journal.
waited=0
timeout=120

while [ $waited -lt $timeout ]; do
	props=$(/usr/bin/getprop)

	if echo "$props" | grep -E "^\[init\.svc\.(vendor\.)?hwcomposer" | grep -q "\[running\]" ; then
		echo "Android hwcomposer service running"
		break
	fi

	if echo "$props" | grep -E "^\[init\.svc\.(vendor\.)?gralloc" | grep -q "\[running\]" ; then
		echo "Android gralloc service running"
		break
	fi

	if echo "$props" | grep "init.svc.*livedisplay" | grep -q "\[running\]" ; then
		echo "Android Livedisplay service running"
		break
	fi

	if [ "$(getprop sys.init_boot_completed)" = "1" ] ; then
		echo "Android reports boot completed"
		break
	fi

	echo "Checking for Android graphics services ..."
	sleep 1
	waited=$((waited + 1))
done

if [ $waited -ge $timeout ]; then
	echo "WARNING: no Android graphics service appeared within ${timeout}s;"
	echo "WARNING: continuing anyway - expect no display."
	echo "WARNING: services reported by the container were:"
	/usr/bin/getprop | grep "init.svc" | sed 's/^/WARNING:   /'
fi

sleep 1
