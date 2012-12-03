DESCRIPTION = "nEo GTK theme - a very fast, high contrast GTK theme"
HOMEPAGE = "http://jmccloud.jm.funpic.de"
AUTHOR = "Jesus McCloud <bernd.pruenster@gmail.com"
RDEPENDS_${PN} += "icon-theme-neo"
RSUGGESTS_${PN} = "elementary-theme-neo e-wm-theme-illume-neo gpe-theme-neo icon-theme-neo"
inherit allarch
LICENSE = "unknown"

SRCREV = "4e6035ef5452b2f0a128dff91ddeb8335a3aafec"
PV = "0.3+gitr${SRCPV}"
PR = "r8"

SRC_URI = "git://git.shr-project.org/repo/shr-themes.git;protocol=http;branch=master"

S = "${WORKDIR}/git/gtk/${PN}/"

require gtk-theme.inc
