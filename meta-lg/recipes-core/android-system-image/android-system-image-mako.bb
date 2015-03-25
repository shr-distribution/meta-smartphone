require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mako"

PV = "20150313-115"

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
SRC_URI[md5sum] = "a24bb600f03d2cc23faea013b74c49a7"
SRC_URI[sha256sum] = "088d6d52933f9ca0b91f0ccab5c52b8fc8b6da42abf6058ffce915c42ee8202e"
