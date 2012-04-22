FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

# NOTE:
# 1. We need an additional initscript to mount devtmpfs as we're coming from an initramfs
# which already has a mounted /dev by CONFIG_DEVTMPFS_MOUNT.
# 2. As we're using android usb composite driver we need to enable rndis support manually
# on startup.

SRC_URI_append_crespo = " \
  file://mountdevtmpfs.sh \
  file://mountdevtmpfs.service \
  file://rndissetup.sh \
  file://rndissetup.service \
"

do_install_append() {
        if [ "${MACHINE}" = "crespo" ]; then
                install -d ${D}${bindir}
                install -m 0755 ${WORKDIR}/rndissetup.sh ${D}${bindir}
                install -m 0755 ${WORKDIR}/mountdevtmpfs.sh ${D}${bindir}
        fi
}

INHERIT_append_crespo = " systemd"
SYSTEMD_SERVICE_crespo = "rndissetup.service mountdevtmpfs.service"
