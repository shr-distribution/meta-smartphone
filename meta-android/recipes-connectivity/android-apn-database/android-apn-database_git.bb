DESCRIPTION = "Android APN Database"
SECTION = "network"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://etc/apns-full-conf.xml;beginline=2;endline=18;md5=54cbe40b444b82eb6a61614ce7d98dfb"

SRCREV = "10fa2dc40b8e37409bec84267628df8c19838b99"
PV = "4.2.2+gitr${SRCPV}"

SRC_URI = "git://android.googlesource.com/device/sample;protocol=https;branch=master"
S = "${WORKDIR}/git"

inherit allarch

do_install() {
    install -d ${D}${datadir}/android-apn-database
    install -m 0644 ${S}/etc/apns-full-conf.xml ${D}${datadir}/android-apn-database/apns.xml
}

PACKAGES = "${PN}"
FILES:${PN} = "${datadir}/android-apn-database"
