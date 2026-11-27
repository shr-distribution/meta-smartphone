SUMMARY = "MediaTek Wi-Fi driver settings (wifi.cfg) for the FuriPhone FLX1s"
DESCRIPTION = "The gen4m Wi-Fi driver asks for wifi.cfg each time Wi-Fi starts. \
Without one the driver keeps its defaults, and on the FLX1s that left the \
radio deaf to some access points: 5 GHz networks and a mesh unit that a PC \
next to it heard at -59 dBm never showed up in a scan. This is the file \
FuriOS ships for the same phone."
SECTION = "base"
# From github.com/FuriLabs/adaptation-furiphone-radon,
# usr/lib/droid-vendor-overlay/firmware/wifi.cfg (BSD-3-clause, Bardia Moshiri).
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9"

COMPATIBLE_MACHINE = "^radon$"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PV = "1.0"

S = "${UNPACKDIR}"

SRC_URI = "file://wifi.cfg"

# SpeIdxCtrl 0 makes the driver use antenna path WF0 alone; its default, 2,
# sends duplicate streams over WF0 and WF1 (wlanGetAntPathType() in gen4m's
# common/wlan_lib.c). DbdcScanCtrl appears nowhere in the driver source;
# kept because FuriOS ships it, what reads it is unverified.
#
# /lib/firmware rather than /vendor/firmware, where FuriOS overlays it: the
# kernel's direct firmware load looks here first, as it does for
# regulatory.db, and Android's ueventd fallback never searches the LuneOS
# root.
do_install() {
    install -D -m 0644 ${UNPACKDIR}/wifi.cfg ${D}${nonarch_base_libdir}/firmware/wifi.cfg
}

FILES:${PN} = "${nonarch_base_libdir}/firmware/wifi.cfg"
