DESCRIPTION = "Several scripts and config files to archive compatibilty with Android"
SECTION = "base"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

SRC_URI = "file://android-system-devs.rules"

inherit allarch

do_install() {
    install -d ${D}${sysconfdir}/udev/rules.d
    install -m 0644 ${WORKDIR}/android-system-devs.rules ${D}${sysconfdir}/udev/rules.d
}
