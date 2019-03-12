# build package busybox-mdev, needed by initramfs scripts
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += " \
	    file://mdev.cfg \
            file://mdev-partname.sh \
           "

do_install_append() {
    install -m 0755 ${WORKDIR}/mdev-partname.sh ${D}${sysconfdir}/mdev
}
