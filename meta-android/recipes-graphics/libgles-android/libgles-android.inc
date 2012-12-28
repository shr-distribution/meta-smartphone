DESCRIPTION = "Android Properietary OpenGL ES implementation utilised with libhybris"
LICENSE = "Apache-2.0 & Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

PACKAGE_ARCH = "${MACHINE_ARCH}"

PROVIDES += "virtual/libgles1 virtual/libgles2 virtual/egl"
RREPLACES_${PN} = "libhybris"
RREPLACES_${PN}-dev = "libhybris-dev"

# We need the nonfree parts to be able to do anything but we still want them separated
# from the free parts for easier tracking.
RDEPENDS_${PN} = "${PN}-nonfree"

SRC_URI = "git://github.com/morphis/libhybris;protocol=git;branch=master;name=hybris"
S = "${WORKDIR}/git"

INC_PR = "r0"
SRCREV_hybris = "f38749d213522115b5a74267ff7e7748fa5590e9"

inherit autotools

PACKAGES =+ "${PN}-nonfree"
