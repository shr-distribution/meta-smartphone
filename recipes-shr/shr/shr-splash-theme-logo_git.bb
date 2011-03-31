DESCRIPTION = "SHR splash screen - SHR logo theme"
SECTION = "x11/data"
LICENSE = "MIT BSD"
LIC_FILES_CHKSUM = "file://${WORKDIR}/LICENSE;md5=f523ab5986cc79b52a90d2ac3d5454a2"

SRCREV = "cb1391fc4afec7158e34629ea557c6ea486cfc05"
PV = "0.1+gitr${SRCPV}"
PR = "${INC_PR}.1"

SRC_URI = "git://git.shr-project.org/repo/shr-themes.git;protocol=http;branch=master \
           file://LICENSE \
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
