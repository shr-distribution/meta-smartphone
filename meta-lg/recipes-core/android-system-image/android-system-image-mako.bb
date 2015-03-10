require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mako"

PV = "20150309-85"

# Fixing QA errors for not matching architecture for the following binaries:
# - /system/etc/firmware/vidc.b00
# - /system/etc/firmware/vidcfw.elf
# - /system/etc/firmware/vidc.mdt
# - /system/lib/libGLESv3.so
INSANE_SKIP_${PN} = "arch"
# Fixing QA no GNU_HASH error for all binaries
INSANE_SKIP_${PN} += "ldflags"
# Fixing QA relocations in .text error for all binaries
INSANE_SKIP_${PN} += "textrel"
# Fixing symlinks to .so files in non -dev packages
INSANE_SKIP_${PN} += "dev-so"

SRC_URI = "http://build.webos-ports.org/cm-wop-11.0/cm-wop-11.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "d05d8376f480c37319b5350796882c86"
SRC_URI[sha256sum] = "c9ed8c5321f4a2775f1054f76cae4777539eb2d1f8bf1b951c8be361e84c3e10"
