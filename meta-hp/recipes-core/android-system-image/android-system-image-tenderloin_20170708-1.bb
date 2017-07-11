require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tenderloin"

# Fixing QA errors for not matching architecture for the following binaries:
# - /system/etc/firmware/q6.mdt
INSANE_SKIP_${PN} = "arch"

# Fixing QA errors for binary with a .so symlink not in -dev/-dbg packages for the
# following binaries:
# - /system/lib/libGLESv3.so
INSANE_SKIP_${PN} += "dev-so"

# Fixing QA errors for binaries without GNU_HASH
INSANE_SKIP_${PN} += "ldflags"

# Fixing QA errors for binaries with relocations in .text
INSANE_SKIP_${PN} += "textrel"

SRC_URI = "http://build.webos-ports.org/halium-wop-12.1/hal-droid-wop-12.1-20170708-tenderloin.tar.bz2"
SRC_URI[md5sum] = "58b115245d992c89069f4cc19d29f93e"
SRC_URI[sha256sum] = "1523d834251bc5b15af8e331a2ea15f1bfd50f7ca1501aa7e89c635f5d98b0fc"
