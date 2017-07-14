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

SRC_URI = "http://build.webos-ports.org/halium-wop-12.1/hal-droid-wop-12.1-20170714-tenderloin.tar.bz2"
SRC_URI[md5sum] = "c1b91241951904ba4e72227188987efd"
SRC_URI[sha256sum] = "286e982f34308c2a69fc203f1ed60447040760f7a1b41fd3a81702f0a9cf7aa0"
