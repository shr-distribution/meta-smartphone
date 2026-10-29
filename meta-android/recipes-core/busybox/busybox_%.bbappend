# build package busybox-mdev, needed by initramfs scripts
FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"

SRC_URI += " \
    file://mdev.cfg \
    file://mdev-partlabel.sh \
"

# telnetd only for the initramfs debug shells (INITRAMFS_DEBUG_SHELL,
# meta-android/conf/layer.conf). This busybox is the rootfs's too, so telnetd
# was in every image as well, although only the initramfs uses it.
SRC_URI += "${@oe.utils.conditional('INITRAMFS_DEBUG_SHELL', '1', 'file://telnetd.cfg', '', d)}"

do_install:append() {
    install -m 0755 ${UNPACKDIR}/mdev-partlabel.sh ${D}${sysconfdir}/mdev
}
