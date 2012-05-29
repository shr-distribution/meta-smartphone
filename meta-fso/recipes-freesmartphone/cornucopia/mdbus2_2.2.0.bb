require ${BPN}.inc

PR = "${INC_PR}.0"

# work around for bad filename and directory
SRC_URI = "http://downloads.freesmartphone.org/sources/tools/${PN}/mdbus-${PV}.tar.bz2"
S = "${WORKDIR}/mdbus-${PV}"

SRC_URI[md5sum] = "a7f9e568d02ac1bea3c526ad4cdc83b4"
SRC_URI[sha256sum] = "6d58fec1e6045dc10eb4b838b7cafb5322da533afdb3abc489e1de04442e5c99"
