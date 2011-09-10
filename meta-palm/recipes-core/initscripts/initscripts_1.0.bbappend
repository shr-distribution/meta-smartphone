FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

PRINC = "1"

do_install_append() {
        if [ "${MACHINE}" = "palmpre" ]; then
                install -m 0755 ${WORKDIR}/mountdevtmpfs.sh ${D}${sysconfdir}/init.d
                ln -sf ../init.d/mountdevtmpfs.sh ${D}${sysconfdir}/rcS.d/S03mountdevtmpfs.sh
        fi
}

SRC_URI_append_palmpre = " file://mountdevtmpfs.sh"
PACKAGE_ARCH_palmpre = "${MACHINE_ARCH}"
