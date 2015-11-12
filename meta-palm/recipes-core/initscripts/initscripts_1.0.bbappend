FILESEXTRAPATHS_prepend_palmpre := "${THISDIR}/${PN}:"

# NOTE:
# 1. We need an additional initscript to mount devtmpfs as we're coming from an initramfs
# which already has a mounted /dev by CONFIG_DEVTMPFS_MOUNT.
#

do_install_append_palmpre() {
    install -m 0755 ${WORKDIR}/mountdevtmpfs.sh ${D}${sysconfdir}/init.d
    ln -sf ../init.d/mountdevtmpfs.sh ${D}${sysconfdir}/rcS.d/S03mountdevtmpfs.sh
}

SRC_URI_append_palmpre = " file://mountdevtmpfs.sh"
PACKAGE_ARCH_palmpre = "${MACHINE_ARCH}"
