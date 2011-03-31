DESCRIPTION = "A gtk theme that looks like e17"
LICENSE = "MIT BSD"
LIC_FILES_CHKSUM = "file://${WORKDIR}/LICENSE;md5=f523ab5986cc79b52a90d2ac3d5454a2"
SRCREV = "1564a06ebf8680a2824f5d0db0616279e72c5296"
PV = "0.1.1+gitr${SRCPV}"
PR = "r8"
PACKAGE_ARCH = "all"

SRC_URI = "git://git.shr-project.org/repo/shr-themes.git;protocol=http;branch=master \
           file://LICENSE \
"

S = "${WORKDIR}/git/gtk/${PN}"

require gtk-theme.inc
