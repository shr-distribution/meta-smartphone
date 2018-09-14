require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "^rosy$"

PV = "20180914-22"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "098c90decae0aa870f1416cfa02261a4"
SRC_URI[sha256sum] = "e45a67266c5f95c2b130b8067b17e8ce8f21dd894890521a62326a0db54c921d"
