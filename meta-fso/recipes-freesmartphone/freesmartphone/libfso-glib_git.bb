require ${BPN}.inc

DEFAULT_PREFERENCE = "-1"

SRCREV = "f93a9297bc3627699c4a68c6382708731d62705a"
PV = "2012.05.24.1+gitr${SRCPV}"

SRC_URI = "${FREESMARTPHONE_GIT}/${BPN}.git;protocol=git;branch=master"
S = "${WORKDIR}/git"
