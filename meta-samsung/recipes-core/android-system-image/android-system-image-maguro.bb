require recipes-core/android-system-image/android-system-image.inc

PR = "${INC_PR}.1"

COMPATIBLE_MACHINE = "maguro"

PV = "20170305-19"

SRC_URI = "http://build.webos-ports.org/cm-wop-12.1/hal-droid-wop-12.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "e83ca11e8844ab5d664ee4ab628de587"
SRC_URI[sha256sum] = "de1cbcbde7b533a442df0352ce8477c59d5195fca4e194aa10f8c575c0a89008"

do_install_prepend() {
    # fixup libGLESv3.so if needed
    if [ -h ${WORKDIR}/system/lib/libGLESv3.so ] ; then
        rm ${WORKDIR}/system/lib/libGLESv3.so
        cp ${WORKDIR}/system/lib/libGLESv2.so ${WORKDIR}/system/lib/libGLESv3.so
    fi

}
