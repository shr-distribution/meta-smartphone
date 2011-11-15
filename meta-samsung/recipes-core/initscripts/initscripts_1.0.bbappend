FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

# NOTE:
# 1. We need an additional initscript to mount devtmpfs as we're coming from an initramfs
# which already has a mounted /dev by CONFIG_DEVTMPFS_MOUNT.
# 2. As we're using android usb composite driver we need to enable rndis support manually
# on startup.

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
