require ${BPN}.inc

DEFAULT_PREFERENCE = "-1"

SRCREV = "7867e63de20cf6fb8d62fae8ab56eb50aa2bebf8"
PV = "0.10.99+gitr${SRCPV}"
PR = "r1"

SRC_URI = "git://github.com/freesmartphone/framework;protocol=https;branch=master \
  file://0001-Add-config-for-some-qemu-machines.patch \
"

S = "${WORKDIR}/git"
