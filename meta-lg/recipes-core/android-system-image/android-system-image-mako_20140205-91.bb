require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mako"

# Fixing QA errors for not matching architecture for the following binaries:
# - /system/etc/firmware/vidc.b00
# - /system/etc/firmware/vidcfw.elf
# - /system/etc/firmware/vidc.mdt
INSANE_SKIP_${PN} = "arch"
# Fixing QA no GNU_HASH error for all binaries
INSANE_SKIP_${PN} += "ldflags"

SRC_URI = "http://build.webos-ports.org/phablet/phablet-${MACHINE}-${PV}.tar.bz2"
SRC_URI[md5sum] = "0fc12140e189298fa216da21c29a31c4"
SRC_URI[sha256sum] = "257d00f784c06c5e691b4cedb8954b88ecc4b16e4a2c3c794d2a6f855fad9cd2"
