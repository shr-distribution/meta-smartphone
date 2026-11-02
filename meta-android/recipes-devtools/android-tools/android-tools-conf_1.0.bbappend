FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# Binding the gadget again after a cable unplug: see android-gadget-rebind.
SRC_URI += " \
    file://android-gadget-rebind \
    file://70-android-gadget-rebind.rules \
"

# android-tools-conf from blacksail (meta-luneos-backports-6.1, which
# android-tools 35.0.2 needs) is the configfs one: besides
# android-gadget-setup it ships android-gadget-start, android-gadget-cleanup
# and an android-tools-adbd.service drop-in that runs the three. Those work
# on a gadget called "adb"; the android-gadget-setup above builds "g1" and
# binds the UDC itself. With them, adbd's ExecStartPost writes to
# /sys/kernel/config/usb_gadget/adb/UDC, which is not there, the start fails
# and systemd restarts adbd over and over; the gadget is set up a second
# time, without the functions, on every start; and ExecStopPost kills adbd
# and unmounts /dev/usb-ffs/adb. Leave only android-gadget-setup, as the
# android-tools-conf of wrynose had it.
do_install:append() {
    rm -f ${D}${bindir}/android-gadget-start ${D}${bindir}/android-gadget-cleanup
    rm -f ${D}${systemd_unitdir}/system/android-tools-adbd.service.d/10-adbd-configfs.conf
    rmdir --ignore-fail-on-non-empty ${D}${systemd_unitdir}/system/android-tools-adbd.service.d
    install -d ${D}${bindir} ${D}${nonarch_base_libdir}/udev/rules.d
    install -m 0755 ${UNPACKDIR}/android-gadget-rebind ${D}${bindir}/
    install -m 0644 ${UNPACKDIR}/70-android-gadget-rebind.rules ${D}${nonarch_base_libdir}/udev/rules.d/
}

FILES:${PN} += "${nonarch_base_libdir}/udev/rules.d/70-android-gadget-rebind.rules"
