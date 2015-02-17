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
SRC_URI[md5sum] = "95127d248084767cdf37bead94bc867b"
SRC_URI[sha256sum] = "0cc584c0df312fdb7968329ff0eccec6884ea3546fd076d24895febdad8cf970"
