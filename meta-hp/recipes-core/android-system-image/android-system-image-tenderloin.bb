require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tenderloin"

PV = "20170914-5"

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

# Skip already-stripped check, because it's prebuilt outside OE so we cannot easily prevent stripping it here
INSANE_SKIP_${PN} += "already-stripped"

SRC_URI = "http://build.webos-ports.org/halium-wop-12.1/halium-wop-12.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "7060dc1ec09726f3c92691c6fa5f4a97"
SRC_URI[sha256sum] = "ccf066ffb0d318b8b88a407325756ab2c1b01d048adfb275c8ba4f906b667618"
