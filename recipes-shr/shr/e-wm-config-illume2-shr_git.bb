DESCRIPTION = "illume2 SHR config"
SECTION = "e/utils"
DEPENDS = "eet"
LICENSE = "MIT BSD"
LIC_FILES_CHKSUM = "file://${WORKDIR}/LICENSE;md5=f523ab5986cc79b52a90d2ac3d5454a2"
RDEPENDS_${PN} = "shr-e-gadgets"
SRCREV = "dc613c5b7c70347ff0a7278bd512b077adec6262"
PV = "1.2+gitr${SRCPV}"
PR = "r9"

inherit e

SRC_URI = "git://git.shr-project.org/repo/shr-themes.git;protocol=http;branch=master \
           file://LICENSE \
"

S = "${WORKDIR}/git/e-wm/${PN}"

PACKAGE_ARCH_palmpre = "${MACHINE_ARCH}"

EXTRA_OECONF = "\
  --with-eet-eet=${STAGING_BINDIR_NATIVE}/eet \
"

EXTRA_OECONF_palmpre = "\
  --with-eet-eet=${STAGING_BINDIR_NATIVE}/eet \
  --enable-machine-palmpre \
"

FILES_${PN} = "${datadir}/enlightenment/data/config/illume2-shr"

ESYSACTIONS ?= "e-wm-sysactions"
EMENU ?= "e-wm-menu"

RRECOMMENDS_${PN} = "\
  ${ESYSACTIONS} \
  ${EMENU} \
  illume-keyboard-default-alpha \
  illume-keyboard-numeric-alt \
  illume-keyboard-default-terminal \
"

