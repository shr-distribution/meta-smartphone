DESCRIPTION = "Config files for generic phone ui framework"
HOMEPAGE = "http://shr-project.org/"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=4fbd65380cdd255951079008b364516c"
SECTION = "libs"
SRCREV = "fed19687cc6ea8750536eb69804213bd41d03d5a"
PV = "0.0.2+gitr${SRCPV}"
PR = "r5"

INHIBIT_DEFAULT_DEPS = "1"

SRC_URI = " \
 git://git.shr-project.org/repo/libphone-ui.git;protocol=http;branch=master \
"

S = "${WORKDIR}/git"

do_install() {
        # Check for machine specific conf.
        CONF_PATH="${S}/conf"
        CONF_PATH_MACHINE="${CONF_PATH}"
        if [ -d "${CONF_PATH}/${MACHINE}" ] ; then
                CONF_PATH_MACHINE="${CONF_PATH}/${MACHINE}"
        fi
        install -d ${D}${sysconfdir}
        install -m 0644 ${CONF_PATH_MACHINE}/libphoneui.conf ${D}${sysconfdir}/libphoneui.conf
}

PACKAGES = "${PN}"
PACKAGE_ARCH = "${MACHINE_ARCH}"

FILES_${PN} = " \
  ${sysconfdir}/libphoneui.conf \
"

CONFFILES_${PN} = "\
  ${sysconfdir}/libphoneui.conf \
"
