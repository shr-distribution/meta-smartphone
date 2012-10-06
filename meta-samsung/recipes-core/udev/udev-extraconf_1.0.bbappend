FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH_tuna = "${MACHINE_ARCH}"

SRC_URI_append_tuna = " \
    file://tuna-pulseaudio.rules \
"

do_install_append() {
    if [ "${MACHINE}" = "tuna" ]; then
        install -m 0644 ${WORKDIR}/tuna-pulseaudio.rules ${D}${sysconfdir}/udev/rules.d/tuna-pulseaudio.rules
    fi
}

PRINC := "${@int(PRINC) + 1}"
