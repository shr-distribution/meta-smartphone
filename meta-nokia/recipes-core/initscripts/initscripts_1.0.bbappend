THISDIR-NOKIA := "${@os.path.dirname(bb.data.getVar('FILE', d, True))}"
FILESPATH =. "${@base_set_filespath(["${THISDIR-NOKIA}/${P}"], d)}:"

EXTRA_DEPS = ""
EXTRA_DEPS_nokia900 = "phonet-utils"
RDEPENDS_${PN} += "${EXTRA_DEPS}"

do_install_append() {
        if [ "${MACHINE}" = "nokia900" ]; then
                install -m 0755 ${WORKDIR}/nokia-n900-cmt-gpio.sh ${D}${sysconfdir}/init.d
                ln -sf ../init.d/nokia-n900-cmt-gpio.sh ${D}${sysconfdir}/rcS.d/S40nokia-n900-cmt-gpio.sh
        fi
}

SRC_URI_append_nokia900 = " file://nokia-n900-cmt-gpio.sh"
PACKAGE_ARCH_nokia900 = "${MACHINE_ARCH}"
