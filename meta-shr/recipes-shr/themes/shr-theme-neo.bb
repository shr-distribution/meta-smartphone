DESCRIPTION = "nEo shr theme - a very fast, high contrast shr theme"
SECTION = "x11/data"
HOMEPAGE = "http://jmccloud.jm.funpic.de"
AUTHOR = "Jesus McCloud <bernd.pruenster@gmail.com"
RDEPENDS_${PN} = "phoneui-shr-theme-neo elementary-theme-neo e-wm-theme-illume-neo gtk-theme-neo gpe-theme-neo icon-theme-neo"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PV = "0.2"
PR = "r5"
inherit allarch

ALLOW_EMPTY_${PN} = "1"

PNBLACKLIST[shr-theme-neo] ?= "Runtime depends on blacklisted shr-theme-neo-dev - the recipe will be removed on 2017-09-01 unless the issue is fixed"

PNBLACKLIST[shr-theme-neo] ?= "Runtime depends on blacklisted shr-theme-neo - the recipe will be removed on 2017-09-01 unless the issue is fixed"

PNBLACKLIST[shr-theme-neo] ?= "Runtime depends on blacklisted e-wm-theme-illume-neo - the recipe will be removed on 2017-09-01 unless the issue is fixed"
