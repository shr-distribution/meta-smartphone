DESCRIPTION = "illume SHR sysactions config"
SECTION = "e/utils"
LICENSE = "MIT BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=f523ab5986cc79b52a90d2ac3d5454a2"
SRCREV = "de05935d27f652f247d806f6f8a290455a546b47"
PV = "1.2+gitr${SRCPV}"
inherit allarch

# build only one e-wm-sysactions provider
EXCLUDE_FROM_WORLD = "1"
RCONFLICTS_${PN} = "e-wm-sysactions"
RPROVIDES_${PN} = "e-wm-sysactions"
RREPLACES_${PN} = "e-wm-sysactions"

SRC_URI = "git://github.com/shr-distribution/shr-themes;protocol=https \
"
S = "${WORKDIR}/git/e-wm/${PN}"

FILES_${PN} = "${sysconfdir}/enlightenment"

do_install() {
    install -d ${D}${sysconfdir}/enlightenment/
    install -m 0755 ${S}/sysactions.conf ${D}${sysconfdir}/enlightenment/sysactions.conf
    install -m 0755 ${S}/suspend.sh ${D}${sysconfdir}/enlightenment/suspend.sh
    install -m 0755 ${S}/lock.sh ${D}${sysconfdir}/enlightenment/lock.sh

    # security reasons, e-wm checks that in runtime
    # xinit[418]: ERROR: CONFIGURATION FILE HAS BAD PERMISSIONS
    chmod 600 ${D}/${sysconfdir}/enlightenment/sysactions.conf
    chmod 600 ${D}/${sysconfdir}/enlightenment/suspend.sh
    chmod 600 ${D}/${sysconfdir}/enlightenment/lock.sh
}
