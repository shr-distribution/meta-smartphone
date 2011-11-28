require recipes-devtools/vala/vala.inc

DEPENDS += "gtk+"

SRC_URI = "http://downloads.freesmartphone.org/vala-${PV}.tar.bz2"

FILES_${PN} += "${datadir}/vala-0.14/vapi"

SRC_URI[md5sum] = "915632d78474b9c5cf312be386615d6a"
SRC_URI[sha256sum] = "21251297796ead2bb29bff8217eb5b038a35c6c2786b66f592bf34e46ce82d33"
