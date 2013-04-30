require recipes-core/android-system-image/android-system-image.inc

PR = "${INC_PR}.1"

COMPATIBLE_MACHINE = "grouper"

SRC_URI = " \
    http://build.webos-ports.org/phablet/grouper/cm-10.1-${PV}-UNOFFICIAL-grouper.zip \
    http://build.webos-ports.org/phablet/grouper/symbols.tar.bz2;name=symbols \
"
SRC_URI[md5sum] = "b1f9c52e200cf1512d70c4b6fec38b56"
SRC_URI[sha256sum] = "bf929dbba2ed86fb87b1e5dd90ec3c4d874c85104439b3838f8b540f040bc201"
SRC_URI[symbols.md5sum] = "8744756b1ebc7945988557cd5bb6c3e1"
SRC_URI[symbols.sha256sum] = "a8b8e073ba34e1668c325b91470277c5570efc03b5d5e0a6ca05663aebec4821"

do_install_append() {
    install -d ${D}/system/symbols
    cp -rv ${WORKDIR}/symbols ${D}/system
}
