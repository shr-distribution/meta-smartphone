require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mako"

# Fixing QA errors for not matching architecture for the following binaries:
# - /system/etc/firmware/vidc.b00
# - /system/etc/firmware/vidcfw.elf
# - /system/etc/firmware/vidc.mdt
INSANE_SKIP_${PN} = "arch"
# Fixing QA no GNU_HASH error for all binaries
INSANE_SKIP_${PN} += "ldflags"
# Fixing QA relocations in .text error for all binaries
INSANE_SKIP_${PN} += "textrel"
# Fix symlinks in non -dev/-dbg/-nativesdk packages
INSANE_SKIP_${PN} += "dev-so"

SRC_URI = "http://build.webos-ports.org/cm-wop-11.0/cm-wop-11.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "de333b447d260064ea1562cb34967fa9"
SRC_URI[sha256sum] = "27accea7192c113e709c8e92db98d07a982892fcca7961bc17e5c97cf5cf8ef0"
