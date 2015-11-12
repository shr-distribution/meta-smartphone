FILESEXTRAPATHS_prepend_om-gta02 := "${THISDIR}/${PN}:"

SRC_URI_append_om-gta02 = " file://g_ether.sh"
PACKAGE_ARCH_om-gta02 = "${MACHINE_ARCH}"

do_install_append_om-gta02() {
    install -m 0755 ${WORKDIR}/g_ether.sh           ${D}${sysconfdir}/init.d
    ln -sf ../init.d/g_ether.sh ${D}${sysconfdir}/rcS.d/S02g_ether.sh
}
