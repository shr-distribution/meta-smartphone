#!/system/bin/sh
# Start the TouchPad's camera provider so that HP's liboemcamera.so can be loaded at 0x67700000, the
# address it is prelinked to (it has no relative relocations, so it works nowhere else).
#
# With the default top-down layout the provider's own libraries are mapped from below the stack
# downwards and often reach that span before the camera HAL loads the blob. An unlimited stack makes
# the 3.4 kernel use the legacy bottom-up layout (arch/arm/mm/mmap.c, mmap_is_legacy), which maps from
# about 0x30000000 upwards and stays far below it. liboemcamera_reserve.so then claims the span, and the
# HAL loads the blob into the claim.
#
# LD_PRELOAD is set here and not with `setenv` in the rc: the rc's environment also reaches this shell,
# a system binary that cannot load vendor libraries.
ulimit -s unlimited
LD_PRELOAD="/vendor/lib/liboemcamera_reserve.so /vendor/lib/libpermissioncache_shim.so" \
    exec /vendor/bin/hw/android.hardware.camera.provider@2.4-service
