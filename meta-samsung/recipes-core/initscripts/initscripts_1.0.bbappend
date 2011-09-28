FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

PRINC = "2"

do_install_append() {
    if [ "${MACHINE}" = "crespo" ]; then
        install -m 0755 ${WORKDIR}/rndissetup.sh ${D}${sysconfdir}/init.d
        ln -sf ../init.d/rndissetup.sh ${D}${sysconfdir}/rcS.d/S03rndissetup.sh

        install -m 0755 ${WORKDIR}/mountdevtmpfs.sh ${D}${sysconfdir}/init.d
        ln -sf ../init.d/mountdevtmpfs.sh ${D}${sysconfdir}/rcS.d/S02mountdevtmpfs.sh
    fi
}

SRC_URI_append_crespo = " \
    file://rndissetup.sh \
    file://mountdevtmpfs.sh \
"
PACKAGE_ARCH_crespo = "${MACHINE_ARCH}"
