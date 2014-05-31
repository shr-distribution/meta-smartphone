require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mako"

# Fixing QA errors for not matching architecture for the following binaries:
# - /system/etc/firmware/vidc.b00
# - /system/etc/firmware/vidcfw.elf
# - /system/etc/firmware/vidc.mdt
INSANE_SKIP_${PN} = "arch"
# Fixing QA no GNU_HASH error for all binaries
INSANE_SKIP_${PN} += "ldflags"

SRC_URI = "http://build.webos-ports.org/phablet/phablet-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "8f3b9c81270faaca04dd2927a4e2da1a"
SRC_URI[sha256sum] = "febf69adfd645f83db30436e21aa1fb70c0172f0f3d6a8f9898b09c685629169"
