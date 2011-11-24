require recipes-devtools/vala/vala.inc
SRC_URI = "http://downloads.freesmartphone.org/vala-${PV}.tar.bz2"

FILES_${PN} += "${datadir}/vala-0.14/vapi"

SRC_URI[md5sum] = "45592602b25a6664919efeb073535c16"
SRC_URI[sha256sum] = "0ba862f606629bf1c23f0971d743f85823d5340116fc92f98c2d4577af8c8ccf"
