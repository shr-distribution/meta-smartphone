require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "hammerhead"

PV = "20170128-0"

# Fixing QA errors for not matching architecture for the following binaries:
# - /system/etc/firmware/vidc.b00
# - /system/etc/firmware/vidcfw.elf
# - /system/etc/firmware/vidc.mdt
INSANE_SKIP_${PN} = "arch"
# Fixing QA no GNU_HASH error for all binaries
INSANE_SKIP_${PN} += "ldflags"
# Fixing QA relocations in .text error for all binaries
INSANE_SKIP_${PN} += "textrel"

SRC_URI = "http://build.webos-ports.org/cm-wop-12.1/hal-droid-wop-12.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "14829ff4ceefec8387861ab60e9b5c2c"
SRC_URI[sha256sum] = "32b0ee4febc823c3de0b973c95b85de0d7bec511645355e67e8cdc40b712c509"

do_install_prepend() {
    # fixup libGLESv3.so if needed
    if [ -h ${WORKDIR}/system/lib/libGLESv3.so ] ; then
        rm ${WORKDIR}/system/lib/libGLESv3.so
        cp ${WORKDIR}/system/lib/libGLESv2.so ${WORKDIR}/system/lib/libGLESv3.so
    fi

}
