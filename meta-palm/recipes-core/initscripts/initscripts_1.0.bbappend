FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

PRINC := "${@int(PRINC) + 1}"

# NOTE:
# 1. We need an additional initscript to mount devtmpfs as we're coming from an initramfs
# which already has a mounted /dev by CONFIG_DEVTMPFS_MOUNT.
#

do_install_append() {
        if [ "${MACHINE_CLASS}" = "palmpre" ]; then
                install -m 0755 ${WORKDIR}/mountdevtmpfs.sh ${D}${sysconfdir}/init.d
                ln -sf ../init.d/mountdevtmpfs.sh ${D}${sysconfdir}/rcS.d/S03mountdevtmpfs.sh
        fi
}

SRC_URI_append_palmpre = " file://mountdevtmpfs.sh"
PACKAGE_ARCH_palmpre = "${MACHINE_ARCH}"
