require recipes-devtools/vala/vala.inc

DEPENDS += "gtk+"

SRC_URI = "http://downloads.freesmartphone.org/vala-${PV}.tar.bz2"

FILES_${PN} += "${datadir}/vala-0.14/vapi"

SRC_URI[md5sum] = "54eb9fc6f24ab1cf8ab799271696079a"
SRC_URI[sha256sum] = "e9658ea4142d8ead5ce2aef20bdec6ab02e4b27f5524fc567780d6af5093f061"
