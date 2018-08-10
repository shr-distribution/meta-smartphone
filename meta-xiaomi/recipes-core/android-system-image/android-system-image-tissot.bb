require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tissot"

PV = "20180809-20"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "918bb234cfeffd86e6a99cacb6d43eca"
SRC_URI[sha256sum] = "610fc86db3fc9914f2242ab4b1c831652356db5d0e0d386affceb7c756f7f331"
