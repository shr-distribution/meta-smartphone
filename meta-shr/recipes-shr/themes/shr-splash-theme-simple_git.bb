DESCRIPTION = "SHR splash screen - simple SHR theme"
SECTION = "x11/data"

SRCREV = "4e6035ef5452b2f0a128dff91ddeb8335a3aafec"
PV = "1.2+gitr${SRCPV}"
PR = "${INC_PR}.0"

SRC_URI = "git://github.com/shr-distribution/shr-themes;protocol=https;branch=master"
S = "${WORKDIR}/git/shr-splash/${PN}"

ALTERNATIVE_PRIORITY[shr-splash-theme] ?= "2"

inherit allarch

require shr-splash-theme.inc
