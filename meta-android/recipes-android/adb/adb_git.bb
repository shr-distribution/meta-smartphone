DESCRIPTION = "Android ADB utility"
SECTION = "devel"
LICENSE = "Android"
LIC_FILES_CHKSUM = "file://COPYING;md5=c19179f3430fd533888100ab6616e114"

SRCREV = "e57f7bafb0e19bb3780132883cef910daa5efaeb"
PV = "2.3.7+gitr${SRCPV}"
PR = "r2"

SRC_URI = " \
  ${FREESMARTPHONE_GIT}/utilities.git;protocol=git;branch=master \
  file://adbd.init"
S = "${WORKDIR}/git/android/adb"

inherit autotools update-rc.d

INITSCRIPT_NAME = "adbd"
INITSCRIPT_PARAMS = "defaults 20"

do_install_append() {
    install -d ${D}${sysconfdir}/init.d/
    install -m 0755 ${WORKDIR}/adbd.init ${D}${sysconfdir}/init.d/adbd

    # to get "adb shell" working
    install -d ${D}/system/bin
    ln -sf /bin/sh ${D}/system/bin/sh
}

FILES_${PN} += "/system/bin/sh"
