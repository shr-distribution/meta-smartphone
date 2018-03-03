require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tenderloin"

PV = "20180303-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-5.1/halium-luneos-5.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "35683ec36e97fa593271091e5745bee4"
SRC_URI[sha256sum] = "02efd7c8d182b6d44e9936a4b3277d3b7e2cb7396becec8e61f0ed76ad463a1b"
