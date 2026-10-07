FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# Nothing but local files, so nothing lands in the default
# S = "${UNPACKDIR}/${BP}" and do_qa_unpack warns about it. Scoped per machine,
# as in meta-xiaomi: this bbappend is parsed for every build.
S:sm-t220 = "${UNPACKDIR}"

SRC_URI:append:sm-t220 = " \
    file://mtk-audio-param-fixup.service \
    file://mtk-audio-param-fixup.sh \
"

do_install:append:sm-t220() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/mtk-audio-param-fixup.service ${D}${systemd_unitdir}/system

    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/mtk-audio-param-fixup.sh ${D}${bindir}
}

SYSTEMD_SERVICE:${PN}:append:sm-t220 = " mtk-audio-param-fixup.service"

# SM-T520: Bluetooth through bluez's hciattach on /dev/ttySAC0, as hammerhead-halium does.
#
# The Android Bluetooth HAL (what bluebinder talks to) opens that UART, powers the BCM4339 on and
# then aborts with "OnDataReady: Unimplemented packet type 12" before it sends HCI_Reset, so
# bluebinder times out every minute and hci0 stays a virtual device at 00:00:00:00:00:00. The
# controller itself answers: hciattach got as far as setting its UART speed to 3 Mbit/s. bluebinder
# is left out of the image for this machine (packagegroup-luneos-extended) and this unit attaches the
# controller, with the address stored in the tablet's EFS and the vendor's firmware patch.
#
# The files are called bcm-hciattach.* because another layer's bbappend (meta-xiaomi) puts its directory
# first in the search path for every machine, and its Qualcomm hciattach.service and hciattach.sh won over
# these when they had the same names.
S:sm-t520 = "${UNPACKDIR}"

SRC_URI:append:sm-t520 = " \
    file://bcm-hciattach.service \
    file://bcm-hciattach.sh \
    file://bcm-hciattach-resume.service \
    file://70-bcm-hciattach.rules \
    file://60-sm-t520-loop-noprobe.rules \
    file://boot-cpufreq-boost.service \
    file://boot-cpufreq-boost-end.service \
    file://wlan0-mac-settle.service \
"

do_install:append:sm-t520() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/bcm-hciattach.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/bcm-hciattach-resume.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/boot-cpufreq-boost.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/boot-cpufreq-boost-end.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/wlan0-mac-settle.service ${D}${systemd_unitdir}/system

    # Switching Bluetooth off in Settings blocks the controller's rfkill switch, which cuts its power; this
    # attaches it again when it is unblocked.
    install -d ${D}${nonarch_base_libdir}/udev/rules.d
    install -m 0644 ${UNPACKDIR}/70-bcm-hciattach.rules ${D}${nonarch_base_libdir}/udev/rules.d/
    install -m 0644 ${UNPACKDIR}/60-sm-t520-loop-noprobe.rules ${D}${nonarch_base_libdir}/udev/rules.d/

    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/bcm-hciattach.sh ${D}${bindir}

    # hciattach looks for <chip name>.hcd in the firmware directory. The controller calls itself
    # BCM4335C0 (the Nexus 5 does too); the patch is the one the vendor's bt_vendor.conf names.
    install -d ${D}${nonarch_base_libdir}/firmware
    ln -sf /android/vendor/firmware/bcm4335.hcd ${D}${nonarch_base_libdir}/firmware/BCM4335C0.hcd
}

FILES:${PN}:append:sm-t520 = " ${nonarch_base_libdir}/firmware/BCM4335C0.hcd ${nonarch_base_libdir}/udev/rules.d/70-bcm-hciattach.rules ${nonarch_base_libdir}/udev/rules.d/60-sm-t520-loop-noprobe.rules ${systemd_unitdir}/system/bcm-hciattach-resume.service"
SYSTEMD_SERVICE:${PN}:append:sm-t520 = " bcm-hciattach.service"

# The Exynos 5420 boots on the ondemand governor, which on this kernel (iks-cpufreq) spent most of the boot at
# 250 to 800 MHz of the 1.9 GHz it has: the container's init and the services behind it are a long chain of
# small processes on a CPU that is the bottleneck. Holding the CPUs at 1.9 GHz from the start of sysinit until the
# UI is up took the UI target (lsm-ready) from about 55 s to about 41 s and the Nyx target from 47-58 s to 37.6 s
# (5 Oct 2026, battery temperature 38.5 C afterwards).
#
# Through /sys/power/cpufreq_min_limit, the cpufreq driver's PM QoS floor, not by switching scaling_governor to
# performance and back: switching the governor races with the Exynos dynamic CPU hotplug thread, and on 6 Oct 2026
# the write of "ondemand" hung in D state, holding the cpufreq lock, with WebAppMgr blocked behind it and the UI
# stuck at the boot logo. With the floor the governor never stops or starts.
FILES:${PN}:append:sm-t520 = " ${systemd_unitdir}/system/boot-cpufreq-boost.service ${systemd_unitdir}/system/boot-cpufreq-boost-end.service"
SYSTEMD_SERVICE:${PN}:append:sm-t520 = " boot-cpufreq-boost.service boot-cpufreq-boost-end.service"

# The bcmdhd driver registers wlan0 with a random MAC address and only takes the one in nvram_net.txt when the
# interface is first brought up and the firmware is loaded, which connman does well after it has seen the device.
# connman names every Wi-Fi service after the MAC it saw first (wifi_<mac>_<ssid>_...), so a saved network was keyed to
# an address that never came back: five different ones sat in /var/lib/connman after five boots, and the tablet
# never rejoined a network by itself. Bringing the interface up and down once before connman starts lets the
# address settle first. (5 Oct 2026)
FILES:${PN}:append:sm-t520 = " ${systemd_unitdir}/system/wlan0-mac-settle.service"
SYSTEMD_SERVICE:${PN}:append:sm-t520 = " wlan0-mac-settle.service"
