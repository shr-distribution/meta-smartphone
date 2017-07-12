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
SRC_URI[md5sum] = "be29055be38fc540f92dc5b1664e8890"
SRC_URI[sha256sum] = "ce8fe227a888d59426406ce2baf6374bc44cc6092b429c997dfe1e9c15b884cf"
