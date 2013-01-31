require ${BPN}.inc

PR = "${INC_PR}.0"

# ugly but works
SRC_VER = "${@bb.data.getVar('PV',d,1).replace('0.11.999+0.12.0-rc', '0.12.0-rc')}"
SRC_URI = "http://downloads.freesmartphone.org/sources/cornucopia/${SHRT_VER}/${PN}-${SRC_VER}.tar.bz2"

SRC_URI[md5sum] = "6e8fb8e16676c17fcbf92851a8c0704c"
SRC_URI[sha256sum] = "080c4929917e660600a8eeb4164e80b881521f31c626577bda53ae86288a2e60"
