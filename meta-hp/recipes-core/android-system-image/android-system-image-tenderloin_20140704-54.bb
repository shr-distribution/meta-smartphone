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
SRC_URI[md5sum] = "f7ef305d4c5ad7325df9e4c1b2a0700f"
SRC_URI[sha256sum] = "0fc45bfb357841d3b090413db781c739228e6fa41903d8a110f944f70aca9c37"
