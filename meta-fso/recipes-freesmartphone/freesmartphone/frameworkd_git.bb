require ${BPN}.inc

DEFAULT_PREFERENCE = "-1"

SRCREV = "032b9381c59350d79c2a06f2b4aa2bee6a4d36cc"
PV = "0.10.99+gitr${SRCPV}"
PR = "r2"

SRC_URI = "${FREESMARTPHONE_GIT}/framework.git;protocol=git;branch=master \
           file://oeventsd-use-opimd-signals.patch \
           file://0001-oeventsd-workaround-buggy-kernel-to-get-full-vibrati.patch \
"

S = "${WORKDIR}/git"
