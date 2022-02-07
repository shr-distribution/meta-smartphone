DESCRIPTION = "A gtk theme that looks like e17"
LICENSE = "MIT BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=f523ab5986cc79b52a90d2ac3d5454a2"
SRCREV = "4e6035ef5452b2f0a128dff91ddeb8335a3aafec"
PV = "0.1.1+gitr${SRCPV}"
PR = "r8"

SRC_URI = "git://github.com/shr-distribution/shr-themes;protocol=https;branch=master \
"

S = "${WORKDIR}/git/gtk/${PN}"

require gtk-theme.inc
