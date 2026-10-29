DESCRIPTION = "Simple init script to boot on an Android device"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PACKAGE_ARCH = "${MACHINE_ARCH}"
PACKAGES = "${PN}"

RDEPENDS:${PN} = " iproute2 busybox-mdev e2fsprogs e2fsprogs-resize2fs parted"

# Nothing but local files, so nothing ever lands in the default
# S = "${UNPACKDIR}/${BP}" and do_unpack warns about it.
S = "${UNPACKDIR}"

SRC_URI = " \
  file://init.sh \
  file://init_functions.sh \
"

FILES:${PN} = "/init /init_functions.sh"

do_install() {
    install -m 0755 ${UNPACKDIR}/init.sh ${D}/init
    install -m 0644 ${UNPACKDIR}/init_functions.sh ${D}/init_functions.sh
    # The root debug shells only with INITRAMFS_DEBUG_SHELL = "1"
    # (meta-mainline/conf/layer.conf).
    sed -i -e 's/@INITRAMFS_DEBUG_SHELL@/${INITRAMFS_DEBUG_SHELL}/' ${D}/init_functions.sh
    grep -q '^initramfs_debug_shell="[01]"$' ${D}/init_functions.sh || \
        bbfatal "init_functions.sh: INITRAMFS_DEBUG_SHELL must be 0 or 1, is '${INITRAMFS_DEBUG_SHELL}'"
}
