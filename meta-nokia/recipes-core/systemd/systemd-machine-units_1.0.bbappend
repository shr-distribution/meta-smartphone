FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

EXTRA_DEPS = ""
EXTRA_DEPS_nokia900 = "phonet-utils"
RDEPENDS_${PN} += "${EXTRA_DEPS}"

SRC_URI_append_nokia900 = " \
  file://nokia-n900-cmt-gpio.sh \
  file://nokia-n900-cmt-gpio.service \
"

do_install_append() {
        if [ "${MACHINE}" = "nokia900" ]; then
                install -d ${D}${bindir}
                install -m 0755 ${WORKDIR}/nokia-n900-cmt-gpio.sh ${D}${bindir}
        fi
}

INHERIT_append_nokia900 = " systemd"
SYSTEMD_SERVICE_nokia900 = "nokia-n900-cmt-gpio.service"
