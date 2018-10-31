require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tissot"

PV = "20181031-27"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"

SRC_URI[md5sum] = "5466c19dcd3271a4b80499cd070d162a"
SRC_URI[sha256sum] = "607df1549b46913554a4ab8e244d4636f58528184f0c9edb4ecc49bfd5768e83"
