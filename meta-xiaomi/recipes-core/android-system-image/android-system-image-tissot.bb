require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tissot"

PV = "20180914-22"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "e403b9dedccfb6b12dfbbbcc0791d2a9"
SRC_URI[sha256sum] = "460d237dae8edb4fce3f1540e59c6b8c1466854a4757e1625169bbe97706769f"
