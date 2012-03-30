DESCRIPTION = "Config files for reference implementation of the freesmartphone.org framework APIs"
HOMEPAGE = "http://www.freesmartphone.org"
AUTHOR = "FreeSmartphone.Org Development Team"
SECTION = "console/network"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=751419260aa954499f7abaabaa882bbe"
SRCREV = "7867e63de20cf6fb8d62fae8ab56eb50aa2bebf8"
PV = "0.9.5.9+gitr${SRCPV}"
PR = "r20"
PE = "1"

inherit distutils python-dir

SRC_URI = "${FREESMARTPHONE_GIT}/framework.git;protocol=git;branch=master \
"

S = "${WORKDIR}/git"

do_install_append() {
  # we care only about ${D}${sysconfdir}/freesmartphone
  rm -rf ${D}${libdir}
  rm -rf ${D}${bindir}
  rm -rf ${D}${datadir}
  rmdir ${D}${prefix} || true # only when it's empty, because e.g. for micro ${D}${prefix} == ${D}
  rm -rf ${D}${sysconfdir}/dbus-1
  rm -rf ${D}${sysconfdir}/init.d

  install -d ${D}${sysconfdir}/freesmartphone/opim/

  frameworkd_install_machine_specific_configs
}

# machines with enabled ogsmd
do_install_append_a780() {
  frameworkd_install_ogsmd_configs
}
do_install_append_eten-m800() {
  frameworkd_install_ogsmd_configs
}

frameworkd_install_ogsmd_configs() {
        # Install machine specific files
        install -d ${D}${sysconfdir}/freesmartphone/ogsmd
        install -m 0644 ${S}/etc/freesmartphone/ogsmd/cell.db ${D}${sysconfdir}/freesmartphone/ogsmd
        install -m 0644 ${S}/etc/freesmartphone/ogsmd/la.db ${D}${sysconfdir}/freesmartphone/ogsmd
        install -m 0644 ${S}/etc/freesmartphone/ogsmd/networks.tab ${D}${sysconfdir}/freesmartphone/ogsmd
}
frameworkd_install_machine_specific_configs() {
        # Check for machine specific conf.
        CONF_PATH="${S}/etc"
        CONF_PATH_MACHINE="${CONF_PATH}"
        if [ -d "${CONF_PATH}/${MACHINE}" ] ; then
                CONF_PATH_MACHINE="${CONF_PATH}/${MACHINE}"
        elif [ -d "${CONF_PATH}/${MACHINE_CLASS}" ] ; then
                CONF_PATH_MACHINE="${CONF_PATH}/${MACHINE_CLASS}"
        fi
        # Install machine specific files
        install -d ${D}${sysconfdir}
        install -m 0644 ${CONF_PATH_MACHINE}/frameworkd.conf ${D}${sysconfdir}

        # Check for machine specific conf.
        CONF_PATH="${S}/etc/freesmartphone/oevents"
        CONF_PATH_MACHINE="${CONF_PATH}"
        if [ -d "${CONF_PATH}/${MACHINE}" ] ; then
                CONF_PATH_MACHINE="${CONF_PATH}/${MACHINE}"
        elif [ -d "${CONF_PATH}/${MACHINE_CLASS}" ] ; then
                CONF_PATH_MACHINE="${CONF_PATH}/${MACHINE_CLASS}"
        fi
	install -d ${D}${sysconfdir}/freesmartphone/oevents/
        install -m 0644 ${CONF_PATH_MACHINE}/rules.yaml ${D}${sysconfdir}/freesmartphone/oevents/
}

PACKAGE_ARCH = "${MACHINE_ARCH}"

# - add wmiconfig for wireless configuration
RDEPENDS_${PN} = "fso-sounds"
RDEPENDS_${PN}_append_om-gta02 = " wmiconfig"

FILES_${PN} = "\
  ${sysconfdir}/frameworkd.conf \
  ${sysconfdir}/freesmartphone \
"
CONFFILES_${PN} = "\
  ${sysconfdir}/frameworkd.conf \
  ${sysconfdir}/freesmartphone/opreferences/conf/phone/silent.yaml \
  ${sysconfdir}/freesmartphone/opreferences/conf/phone/default.yaml \
  ${sysconfdir}/freesmartphone/opreferences/conf/phone/vibrate.yaml \
  ${sysconfdir}/freesmartphone/opreferences/conf/phone/ring.yaml \
  ${sysconfdir}/freesmartphone/opreferences/conf/profiles/default.yaml \
  ${sysconfdir}/freesmartphone/opreferences/conf/rules/silent.yaml \
  ${sysconfdir}/freesmartphone/opreferences/conf/rules/default.yaml \
  ${sysconfdir}/freesmartphone/opreferences/conf/rules/vibrate.yaml \
  ${sysconfdir}/freesmartphone/opreferences/conf/rules/ring.yaml \
  ${sysconfdir}/freesmartphone/oevents/rules.yaml \
"
