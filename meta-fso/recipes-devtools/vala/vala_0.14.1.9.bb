require recipes-devtools/vala/vala.inc

DEPENDS += "gtk+"

SRC_URI = "http://downloads.freesmartphone.org/vala-${PV}.tar.bz2"

FILES_${PN} += "${datadir}/vala-0.14/vapi"

SRC_URI[md5sum] = "07299e2fd152c78a0fff83ee063e68df"
SRC_URI[sha256sum] = "f3ce24a93df556ac96a81dfadae3b927d9e343ae3c1477402b66555aa8331570"
