require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "hammerhead"

PV = "20171130-5"

# Fixing QA errors for not matching architecture for the following binaries:
# - /system/etc/firmware/vidc.b00
# - /system/etc/firmware/vidcfw.elf
# - /system/etc/firmware/vidc.mdt
INSANE_SKIP_${PN} = "arch"
# Fixing QA no GNU_HASH error for all binaries
INSANE_SKIP_${PN} += "ldflags"
# Fixing QA relocations in .text error for all binaries
INSANE_SKIP_${PN} += "textrel"
# Skip already-stripped check, because it's prebuilt outside OE so we cannot easily prevent stripping it here
INSANE_SKIP_${PN} += "already-stripped"

SRC_URI = "http://build.webos-ports.org/halium-wop-5.1/halium-wop-5.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "155471e00dc12e61745274898103fbd1"
SRC_URI[sha256sum] = "64c6d4c48678d22a6d24ecf8be2e3bdd1867d15ea27c7db03dccbe2d6866cb4f"

do_install_prepend() {
    # fixup libGLESv3.so if needed
    if [ -h ${WORKDIR}/system/lib/libGLESv3.so ] ; then
        rm ${WORKDIR}/system/lib/libGLESv3.so
        cp ${WORKDIR}/system/lib/libGLESv2.so ${WORKDIR}/system/lib/libGLESv3.so
    fi

}
