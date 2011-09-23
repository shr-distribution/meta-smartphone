require aurora-base.inc

DESCRIPTION = "This is the default theme for the Aurora UI"
SRCREV = "${AURORA_SRCREV}"

PV = "0.1.0+gitr${SRCPV}"
PR = "${INC_PR}.0"

SRC_URI = "${FREESMARTPHONE_GIT}/aurora.git;protocol=git;branch=master"
SRCREV = "${AURORA_SRCREV}"
S = "${WORKDIR}/git/aurora-theme"

inherit autotools

# We don't have any other packages than the ${PN} as we ship only static content.
PACKAGES = "${PN}"
FILES_${PN} = "${datadir}/aurora"
