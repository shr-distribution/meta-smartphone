FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI_append_onyx = " \
    file://android-system-filesystems.target \
"

SYSTEMD_SERVICE_${PN}_append_onyx = "\
           android-system-filesystems.target \
           "

do_install_append_onyx() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/android-system-filesystems.target ${D}${systemd_unitdir}/system

    # Somehow during post installation the link for android-system-filesystems.target isn't created
    install -d ${D}${sysconfdir}/systemd/system/android-system.service.requires
    ln -sf ${systemd_unitdir}/system/android-system-filesystems.target \
        ${D}${sysconfdir}/systemd/system/android-system.service.requires/android-system-filesystems.target
}
