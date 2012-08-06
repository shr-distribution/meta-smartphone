FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
EXTRA_DEPS = ""
EXTRA_DEPS_om-gta04 = "gta04-gps-handler"
EXTRA_DEPS_om-gta02 = "omhacks"
RDEPENDS_${PN} += "${EXTRA_DEPS}"

PACKAGE_ARCH_om-gta04 = "${MACHINE_ARCH}"
PACKAGE_ARCH_om-gta02 = "${MACHINE_ARCH}"

SRC_URI_append_om-gta02 = " \
    file://device-hook \
"

do_install_append() {
    if [ "${MACHINE}" = "om-gta02" ]; then
        install -d ${D}${sysconfdir}/gpsd/
        install -m 0755  ${WORKDIR}/device-hook ${D}${sysconfdir}/gpsd/device-hook
    fi
}

PRINC := "${@int(PRINC) + 5}"
