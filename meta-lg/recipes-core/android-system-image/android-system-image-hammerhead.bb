require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "hammerhead"

PV = "20170305-19"

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

SRC_URI = "http://build.webos-ports.org/cm-wop-12.1/hal-droid-wop-12.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "6efee2c0ce8ed240f5622eba1aee7bf3"
SRC_URI[sha256sum] = "43a9e186c4ad49ae0653ee938f5ee7476dd97b35190ac8b07d11028b48c8a1ff"

do_install_prepend() {
    # fixup libGLESv3.so if needed
    if [ -h ${WORKDIR}/system/lib/libGLESv3.so ] ; then
        rm ${WORKDIR}/system/lib/libGLESv3.so
        cp ${WORKDIR}/system/lib/libGLESv2.so ${WORKDIR}/system/lib/libGLESv3.so
    fi

}
