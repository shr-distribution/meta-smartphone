# SM-T220: lift PulseAudio's RLIMIT_RTTIME, see 50-sm-t220.conf for why.
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:sm-t220 = " file://50-sm-t220.conf"

do_install:append:sm-t220() {
    install -d ${D}${sysconfdir}/pulse/daemon.conf.d
    install -m 0644 ${UNPACKDIR}/50-sm-t220.conf ${D}${sysconfdir}/pulse/daemon.conf.d/
}
