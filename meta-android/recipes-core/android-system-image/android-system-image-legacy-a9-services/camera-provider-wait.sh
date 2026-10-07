#!/system/bin/sh
# Start the camera provider only once mm-qcamera-daemon has been up, with the same pid, for 10 s.
#
# The HAL asks the daemon for the camera capabilities as soon as it starts. When the daemon is
# not ready, QCamera3HardwareInterface::initStaticMetadata takes an error path that jumps into
# a shared cleanup without its saved stack pointer (it then runs `ldr sp, [r6, #24]` with a
# zero slot) and the provider dies with SIGSEGV. A provider dying in the middle of the daemon's
# handshake can in turn leave the msm camera driver with session->lock held, which hangs the
# next process that opens /dev/video* in D state.
#
# The daemon itself dies with SIGSEGV 5-6 s after each of its first starts of a boot and is
# restarted by init, so "running" is not enough: wait for a pid that stays.
#
# LD_PRELOAD is set here and not with `setenv` in the rc: the rc's environment also reaches this
# script's shell, a system binary that cannot load a vendor library, and it would exit 1.
stable=0
last=
n=0
while [ $stable -lt 10 ] && [ $n -lt 120 ]; do
    pid=$(pidof mm-qcamera-daemon)
    if [ -n "$pid" ] && [ "$pid" = "$last" ]; then
        stable=$((stable + 1))
    else
        stable=0
    fi
    last=$pid
    sleep 1
    n=$((n + 1))
done
LD_PRELOAD=/vendor/lib/libpermissioncache_shim.so exec /vendor/bin/hw/android.hardware.camera.provider@2.4-service
