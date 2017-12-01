require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mido"

PV = "20171130-1"

SRC_URI = "http://build.webos-ports.org/halium-wop-7.1/halium-wop-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "c11f7f51688c99069aa0602f02c32812"
SRC_URI[sha256sum] = "6a1672f31cc4bc9b97512c4be71e4929aa5c27bc262666896dfb5777b85ed711"

do_install_prepend() {
    # fixup libGLESv3.so if needed
    if [ -h ${WORKDIR}/system/lib/libGLESv3.so ] ; then
        rm ${WORKDIR}/system/lib/libGLESv3.so
        cp ${WORKDIR}/system/lib/libGLESv2.so ${WORKDIR}/system/lib/libGLESv3.so
    fi

}
