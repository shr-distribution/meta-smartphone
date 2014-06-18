FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI_append_tenderloin = " file://init-tenderloin.sh"

COMPATIBLE_MACHINE_tenderloin = "tenderloin"

do_install_append_tenderloin() {
    rm ${D}/init
    install -m 0755 ${WORKDIR}/init-tenderloin.sh ${D}/init
}
