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

SRC_URI = "file:///media/LuneOS/Android/mako/cm-wop-12.1-20161120-0-mako.tar.bz2"
SRC_URI[md5sum] = "807cf31e0e9c2465711cea12d5de802f"
SRC_URI[sha256sum] = "75a42cb073a103644ac2b654962b5785fec593e47b454e0339510490f6b8f2d1"
