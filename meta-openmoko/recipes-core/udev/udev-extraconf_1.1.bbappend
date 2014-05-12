FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH_om-gta02 = "${MACHINE_ARCH}"
PACKAGE_ARCH_om-gta04 = "${MACHINE_ARCH}"

SRC_URI_append_om-gta02 = " \
    file://gps.rules \
    file://gps.sh \
"

SRC_URI_append_om-gta04 = " \
    file://charger.rules \
    file://hso.rules \
    file://input.rules \
"

do_install_append() {
    if [ "${MACHINE}" = "om-gta04" ]; then
        install -m 0644 ${WORKDIR}/charger.rules     ${D}${sysconfdir}/udev/rules.d/charger.rules
        install -m 0644 ${WORKDIR}/hso.rules         ${D}${sysconfdir}/udev/rules.d/hso.rules
        install -m 0644 ${WORKDIR}/input.rules       ${D}${sysconfdir}/udev/rules.d/input.rules
    fi

    if [ "${MACHINE}" = "om-gta02" ]; then
        install -m 0644 ${WORKDIR}/gps.rules ${D}${sysconfdir}/udev/rules.d/gps.rules
        install -m 0755 ${WORKDIR}/gps.sh ${D}${sysconfdir}/udev/scripts/gps.sh
    fi
}
