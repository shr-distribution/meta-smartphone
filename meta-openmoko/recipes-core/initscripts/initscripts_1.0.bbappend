THISDIR-OM := "${@os.path.dirname(bb.data.getVar('FILE', d, True))}"
FILESPATH =. "${@base_set_filespath(["${THISDIR-OM}/${PN}"], d)}:"

SRC_URI_append_om-gta02 = " file://g_ether.sh"
PACKAGE_ARCH_om-gta02 = "${MACHINE_ARCH}"

do_install_append() {
        if [ "${MACHINE}" = "om-gta02" ]; then
                install -m 0755 ${WORKDIR}/g_ether.sh           ${D}${sysconfdir}/init.d
                ln -sf ../init.d/g_ether.sh ${D}${sysconfdir}/rcS.d/S02g_ether.sh
        fi
}
