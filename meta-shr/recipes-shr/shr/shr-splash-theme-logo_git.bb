DESCRIPTION = "SHR splash screen - SHR logo theme"
SECTION = "x11/data"

SRCREV = "de6a366bf753d691f80a45e2f103c8a7b3d94b8c"
PV = "0.1+gitr${SRCPV}"
PR = "${INC_PR}.1"

SRC_URI = "git://git.shr-project.org/repo/shr-themes.git;protocol=http;branch=master \
"
S = "${WORKDIR}/git/shr-splash/${PN}"

SS = "${S}"
SS_append_nokia900 = "/nokia900"
SS_append_om-gta01 = "/om-gta01"
SS_append_om-gta02 = "/om-gta02"
SS_append_htcdream = "/htcdream"
SS_append_palmpre  = "/palmpre"

ALTERNATIVE_PRIORITY = 3

require shr-splash-theme.inc
# only this recipe is MACHINE specific
PACKAGE_ARCH = "${MACHINE_ARCH}"
