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

SRC_URI = "http://build.webos-ports.org/cm-wop-11.0/cm-wop-11.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "50ec3c38efa0b7aba0c5144347af5ab6"
SRC_URI[sha256sum] = "7ca001389fe6eee903f731ea2d97117abd07aa91be7419e34bba010d38b44968"
