DESCRIPTION = "SHR splash screen - extremely blue Niebiee theme"
SECTION = "x11/data"

SRCREV = "4e6035ef5452b2f0a128dff91ddeb8335a3aafec"
PV = "1.2+gitr${SRCPV}"
PR = "${INC_PR}.0"

SRC_URI = "git://git.shr-project.org/repo/shr-themes.git;protocol=http;branch=master"
S = "${WORKDIR}/git/shr-splash/${PN}"

inherit allarch

require shr-splash-theme.inc
