# build package busybox-mdev and busybox's telnetd, needed by initramfs scripts
FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"

SRC_URI += " \
    file://mdev.cfg \
    file://mdev-partlabel.sh \
    file://telnetd.cfg \
"

do_install:append() {
    install -m 0755 ${WORKDIR}/mdev-partlabel.sh ${D}${sysconfdir}/mdev
}
