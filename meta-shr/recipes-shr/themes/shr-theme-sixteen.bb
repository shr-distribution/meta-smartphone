DESCRIPTION = "Sixteen SHR theme"
SECTION = "x11/data"
RDEPENDS_${PN} += "e-wm-theme-illume-sixteen elementary-theme-sixteen"
PV = "0.1"
PR = "r3"
inherit allarch
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

ALLOW_EMPTY_${PN} = "1"

PNBLACKLIST[shr-theme-sixteen] ?= "Runtime depends on blacklisted e-wm-theme-illume-sixteen - the recipe will be removed on 2017-09-01 unless the issue is fixed"

PNBLACKLIST[shr-theme-sixteen] ?= "Runtime depends on blacklisted shr-theme-sixteen-dev - the recipe will be removed on 2017-09-01 unless the issue is fixed"

PNBLACKLIST[shr-theme-sixteen] ?= "Runtime depends on blacklisted shr-theme-sixteen - the recipe will be removed on 2017-09-01 unless the issue is fixed"

PNBLACKLIST[shr-theme-sixteen] ?= "Runtime depends on blacklisted elementary-theme-sixteen - the recipe will be removed on 2017-09-01 unless the issue is fixed"
