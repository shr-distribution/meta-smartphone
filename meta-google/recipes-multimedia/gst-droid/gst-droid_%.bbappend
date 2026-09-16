# Opt sargo in to camera-droid-heal.
#
# The heal exists for one sargo-specific defect: closing a handle on the
# sensor's /dev/v4l-subdevN clears is_probe_succeed in cam_sensor_core.c, and
# from then on the vendor provider refuses every CAM_ACQUIRE_DEV, which is why
# roughly half of sargo's boots come up with a black viewfinder. The full
# analysis is in camera-droid-heal.sh.
#
# It is opt-in because the probe it uses to detect the condition is itself a
# camera open through the whole gst-droid -> droidmedia -> CamX path. On a board
# without the defect that is a wasted open at best, and on a board whose
# provider blocks rather than answers it is a oneshot sitting in the boot
# transaction - on MP01 that held the compositor, bootd and sam queued behind it
# for three minutes before the probe timeout was added.
#
# Existence of the file is the switch; its contents are never read.
do_install:append:sargo() {
    install -d ${D}${sysconfdir}/gst-droid
    printf '# Enables camera-droid-heal.service; see camera-droid-heal.sh.\n' \
        > ${D}${CAMERA_DROID_HEAL_CONF}
}

FILES:${PN}:append:sargo = " ${CAMERA_DROID_HEAL_CONF}"
