DESCRIPTION = "illume2 SHR config"
SECTION = "e/utils"
DEPENDS = "eet"
LICENSE = "MIT BSD"
LIC_FILES_CHKSUM = "file://${WORKDIR}/LICENSE;md5=f523ab5986cc79b52a90d2ac3d5454a2"
RDEPENDS_${PN} = "shr-e-gadgets"
SRCREV = "6f795f4e1120c2804f3c2f69873ccd36ae47e5ef"
PV = "1.2+gitr${SRCPV}"
PR = "r10"

inherit e

SRC_URI = "git://git.shr-project.org/repo/shr-themes.git;protocol=http;branch=master \
           file://0001-e-wm-config-illume2-shr-drop-conf_imc-was-merged-wit.patch \
           file://0002-e-wm-config-illume2-shr-drop-conf_engine-was-merged-.patch \
           file://0003-e-wm-config-illume2-shr-drop-conf_profiles-was-merge.patch \
           file://0004-e-wm-config-illume2-shr-drop-conf_transitions-conf_f.patch \
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

