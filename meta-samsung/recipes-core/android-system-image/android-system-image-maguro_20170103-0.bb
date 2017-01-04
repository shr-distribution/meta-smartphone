require recipes-core/android-system-image/android-system-image.inc

PR = "${INC_PR}.1"

COMPATIBLE_MACHINE = "maguro"

SRC_URI = "http://build.webos-ports.org/cm-wop-12.1/hal-droid-wop-12.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "2b03de9fd4f41df365790c5f9673615b"
SRC_URI[sha256sum] = "61649bb96baf1302e68d15eb94ed54a4ed376e5bcb722bcaf21f8a187e0a2e86"

do_install_prepend() {
    # fixup libGLESv3.so if needed
    if [ -h ${WORKDIR}/system/lib/libGLESv3.so ] ; then
        rm ${WORKDIR}/system/lib/libGLESv3.so
        cp ${WORKDIR}/system/lib/libGLESv2.so ${WORKDIR}/system/lib/libGLESv3.so
    fi

}
