DESCRIPTION = "C library of useful functions to control OpenMoko hardware."
AUTHOR = "Enrico Zini <enrico@enricozini.org>"
HOMEPAGE = "http://git.debian.org/?p=pkg-fso/omhacks.git;a=blob;f=README"
SECTION = "openmoko/tools"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=751419260aa954499f7abaabaa882bbe"
PV = "0.15.1+gitr${SRCPV}"

SRC_URI = "git://git.debian.org/pkg-fso/omhacks.git;protocol=git;branch=master \
           file://0001-CMakeLists-drop-help2man-usage.patch \
          "

SRCREV = "f17902c0b199307bea63acce550e1c70907dd6c4"
S = "${WORKDIR}/git"

inherit cmake

FILES_${PN} += "${prefix}/lib/pm-utils"
FILES_${PN}-dbg += "${prefix}/lib/pm-utils/sleep.d/.debug"

# http://errors.yoctoproject.org/Errors/Details/145597/
PNBLACKLIST[omhacks] ?= "Not compatible with gcc7 - the recipe will be removed on 2017-09-01 unless the issue is fixed"
