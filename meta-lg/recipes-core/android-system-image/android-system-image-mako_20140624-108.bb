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

SRC_URI = "http://build.webos-ports.org/cm-wop-10.1/cm-wop-10.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "98bb1ff38e78acedb9fe7529b7c2ff69"
SRC_URI[sha256sum] = "da866d069c9be9f977a196dfa63373129ed77f9a6ba28a1ab69736b4608946c8"
