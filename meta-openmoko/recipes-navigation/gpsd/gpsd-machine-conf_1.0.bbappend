FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

EXTRA_DEPS = ""
EXTRA_DEPS_om-gta02 = "omhacks"
RDEPENDS_${PN} += "${EXTRA_DEPS}"

PACKAGE_ARCH = "${MACHINE_ARCH}"

SRC_URI_om-gta04 = " \
    file://gpsd-machine \
"
SRC_URI_om-gta02 = " \
    file://gpsd-machine \
    file://device-hook \
"

inherit update-alternatives

ALTERNATIVE_${PN}_om-gta02 = "gspd-defaults"
ALTERNATIVE_${PN}_om-gta04 = "gspd-defaults"
ALTERNATIVE_LINK_NAME[gspd-defaults] = "${sysconfdir}/default/gpsd"
ALTERNATIVE_TARGET[gspd-defaults] = "${sysconfdir}/default/gpsd.machine"
ALTERNATIVE_PRIORITY[gspd-defaults] = "15"

# we're moving device-hook from gpsd bbappend (gpsd-conf package)
RREPLACES_${PN} += "gpsd-conf"

do_install() {
    if [ "${MACHINE}" = "om-gta02" -o "${MACHINE}" = "om-gta04" ]; then
        install -d ${D}/${sysconfdir}/default
        install -m 0644 ${WORKDIR}/gpsd-machine ${D}/${sysconfdir}/default/gpsd.machine
    fi

    if [ "${MACHINE}" = "om-gta02" ]; then
        install -d ${D}${sysconfdir}/gpsd/
        install -m 0755  ${WORKDIR}/device-hook ${D}${sysconfdir}/gpsd/device-hook
    fi
}
