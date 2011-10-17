DESCRIPTION = "SHR splash screen - OM2009 theme"
SECTION = "x11/data"

SRCREV = "de6a366bf753d691f80a45e2f103c8a7b3d94b8c"
PV = "1.0+gitr${SRCPV}"
PR = "${INC_PR}.0"

SRC_URI = "git://git.shr-project.org/repo/shr-themes.git;protocol=http;branch=master"
S = "${WORKDIR}/git/shr-splash/${PN}"

ALTERNATIVE_PRIORITY = 4

require shr-splash-theme.inc

