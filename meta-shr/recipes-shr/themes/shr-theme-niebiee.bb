DESCRIPTION = "Niebiee - extremely blue theme for SHR (metapackage)"
SECTION = "x11/data"
RDEPENDS_${PN} += "e-wm-theme-illume-niebiee elementary-theme-niebiee shr-splash-theme-niebiee phoneui-shr-theme-niebiee"
PV = "0.1"
PR = "r4"
inherit allarch
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

ALLOW_EMPTY_${PN} = "1"

PNBLACKLIST[shr-theme-niebiee] ?= "Runtime depends on blacklisted phoneui-shr-theme-niebiee - the recipe will be removed on 2017-09-01 unless the issue is fixed"

PNBLACKLIST[shr-theme-niebiee] ?= "Runtime depends on blacklisted shr-theme-niebiee - the recipe will be removed on 2017-09-01 unless the issue is fixed"

PNBLACKLIST[shr-theme-niebiee] ?= "Runtime depends on blacklisted shr-theme-niebiee-dev - the recipe will be removed on 2017-09-01 unless the issue is fixed"
