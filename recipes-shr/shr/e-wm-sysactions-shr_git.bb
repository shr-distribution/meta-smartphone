DESCRIPTION = "illume SHR sysactions config"
SECTION = "e/utils"
LICENSE = "MIT BSD"
LIC_FILES_CHKSUM = "file://${WORKDIR}/LICENSE;md5=f523ab5986cc79b52a90d2ac3d5454a2"
SRCREV = "bbcec18f0ebd47e4f6eea88b9b774edf7400e752"
PV = "1.2+gitr${SRCPV}"
PR = "r6"
PACKAGE_ARCH = "all"

RCONFLICTS_${PN} = "e-wm-sysactions"

SRC_URI = "git://git.shr-project.org/repo/shr-themes.git;protocol=http \
           file://LICENSE"

S = "${WORKDIR}/git/e-wm/${PN}"

FILES_${PN} = "${sysconfdir}/enlightenment"

do_install() {
    install -d ${D}${sysconfdir}/enlightenment/
    install -m 0755 ${S}/sysactions.conf ${D}${sysconfdir}/enlightenment/sysactions.conf
    install -m 0755 ${S}/suspend.sh ${D}${sysconfdir}/enlightenment/suspend.sh
    install -m 0755 ${S}/lock.sh ${D}${sysconfdir}/enlightenment/lock.sh
}
