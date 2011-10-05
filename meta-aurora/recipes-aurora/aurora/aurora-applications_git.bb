require aurora-base.inc

DESCRIPTION = "This are the default applications for the Aurora UI"
SRCREV = "${AURORA_SRCREV}"
DEPENDS = "qt4-x11-free"

PV = "0.1.0+gitr${SRCPV}"
PR = "${INC_PR}.1"

SRC_URI = "${FREESMARTPHONE_GIT}/aurora.git;protocol=git;branch=master"
SRCREV = "${AURORA_SRCREV}"
S = "${WORKDIR}/git/aurora-applications"

inherit autotools

# We don't have any other packages than the ${PN} as we ship only static content.
PACKAGES = "${PN}"
FILES_${PN} = "${datadir}/aurora"
