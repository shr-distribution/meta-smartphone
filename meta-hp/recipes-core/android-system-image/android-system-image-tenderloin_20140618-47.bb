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

SRC_URI = "http://build.webos-ports.org/phablet/cm-wop-11.0-${MACHINE}-${PV}.tar.bz2"
SRC_URI[md5sum] = "e02b72ea99d6cb876ecd8a1d7536241f"
SRC_URI[sha256sum] = "8192902968f4c48d8dddc67ef401f1354962b7de34c1aaf7dd7e38ae6c6ffb68"
