require recipes-core/android-system-image/android-system-image.inc

PR = "${INC_PR}.1"

COMPATIBLE_MACHINE = "tuna"

SRC_URI = " \
    http://build.webos-ports.org/phablet/maguro/cm-10.1-${PV}-UNOFFICIAL-maguro.zip;name=rootfs \
    http://build.webos-ports.org/phablet/maguro/symbols.tar.bz2;name=symbols \
"
SRC_URI[rootfs.md5sum] = "a39aee9322899ee0b2a83649c822fad2"
SRC_URI[rootfs.sha256sum] = "0427c037f90a229b0672227f59aaaa7dcff65519c83995c634c1de50a3351717"
SRC_URI[symbols.md5sum] = "1b9236eba780ddb03e0b3c0c829c8604"
SRC_URI[symbols.sha256sum] = "844f77ea44ed4622d78c826a3a4fa75c52a51b8f4213f048ba6a64afaf15df18"

do_install_append() {
    install -d ${D}/system/symbols
    cp -rv ${WORKDIR}/symbols ${D}/system
}
