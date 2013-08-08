SUMMARY = "Android header files of phablet environment (http://phablet.ubuntu.com/gitweb)"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

SRC_URI = "git://github.com/webOS-ports/phablet-headers.git;branch=master;protocol=git"
SRCREV = "88a79036498e27df31d80826b921f2be1a39bf8b"
S = "${WORKDIR}/git"

PROVIDES += "virtual/android-headers"

do_install() {
    install -d ${D}${includedir}/android
    cp -rv ${S}/* ${D}${includedir}/android/
}
