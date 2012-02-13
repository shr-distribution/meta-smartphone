DESCRIPTION = "A generic framework for phone ui"
HOMEPAGE = "http://shr-project.org/"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=4fbd65380cdd255951079008b364516c"
SECTION = "libs"
SRCREV = "9192e0ead05552760ccf63026c75ca3c3721bffe"
PV = "0.0.1+gitr${SRCPV}"
PR = "r4"

DEPENDS="glib-2.0 libshr-glib libfso-glib libfsoframework libphone-utils alsa-lib"

SRC_URI = " \
 git://git.shr-project.org/repo/libphone-ui.git;protocol=http;branch=master \
"

S = "${WORKDIR}/git"

inherit autotools pkgconfig

do_install_append() {
	libphone_ui_install_machine_specific_configs
}

libphone_ui_install_machine_specific_configs() {
        # Check for machine specific conf.
        CONF_PATH="${S}/conf"
        CONF_PATH_MACHINE="${CONF_PATH}"
        if [ -d "${CONF_PATH}/${MACHINE}" ] ; then
                CONF_PATH_MACHINE="${CONF_PATH}/${MACHINE}"
        fi
        install -d ${D}${sysconfdir}
        install -m 0644 ${CONF_PATH_MACHINE}/libphoneui.conf ${D}${sysconfdir}/libphoneui.conf
}

PACKAGES =+ "${PN}-config"
PACKAGE_ARCH = "${MACHINE_ARCH}"

RDEPENDS_${PN} = "${PN}-config"

FILES_${PN}-config = " \
  ${sysconfdir}/libphoneui.conf \
"

CONFFILES_${PN}-config = "\
  ${sysconfdir}/libphoneui.conf \
"
