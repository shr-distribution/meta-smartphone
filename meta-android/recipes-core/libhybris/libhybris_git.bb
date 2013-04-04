DESCRIPTION = "Hybris is a solution that commits hybris, by allowing us to use \
bionic-based HW adaptations in glibc systems"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://COPYING;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRCREV = "0d3b3281aa7dd0276b83efa2a5623fb55462eb41"
PV = "0.1.0+gitr${SRCPV}"
PR = "r1"

SRC_URI = "git://github.com/stskeeps/libhybris;branch=master;protocol=git"
S = "${WORKDIR}/git"

PROVIDES += "virtual/libgles1 virtual/libgles2 virtual/egl"

# We don't ship any android binaries but depend on someone else doing this
RDEPENDS_${PN} += "${VIRTUAL-RUNTIME_android-system-image}"

inherit autotools
