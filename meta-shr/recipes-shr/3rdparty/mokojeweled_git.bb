DESCRIPTION = "A bejeweled clone in Elementary"
HOMEPAGE = "http://gitorious.org/mokosuite2/mokojeweled"
AUTHOR = "Daniele Ricci"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=f27defe1e96c2e1ecd4e0c9be8967949"
DEPENDS = "elementary edje-native"
SECTION = "x11/games"

PV = "0.1+gitr${SRCPV}"
PR = "r5"
SRCREV = "fb43d53ea3ca2cd3760c3d4296b6a7e96573dd17"

SRC_URI = "git://gitorious.org/mokosuite2/mokojeweled.git;protocol=git \
           file://0001-theme.edc-fix-edje_cc-build.patch \
           file://0001-menu-fix-s-homogenous-homogeneous-g.patch \
           file://0001-adapt-to-newer-elementary-1.0-APIs.patch \
"
S = "${WORKDIR}/git"

CFLAGS += "-DOPENMOKO"
EXTRA_OECONF = " --with-edje-cc=${STAGING_BINDIR_NATIVE}/edje_cc"

do_configure_prepend() {
        autopoint --force
}

inherit gettext autotools-brokensep
