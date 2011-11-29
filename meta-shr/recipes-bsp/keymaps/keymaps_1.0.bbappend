FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
PRINC := "${@int(PRINC) + 2}"

SRC_URI += " file://keymaps.service"

do_install_append() {
  install -d ${D}${base_libdir}/systemd/system/
  install -m 0644 ${WORKDIR}/keymaps.service ${D}${base_libdir}/systemd/system/keymaps.service
}

inherit systemd
SYSTEMD_PACKAGES = "${PN}-systemd"
SYSTEMD_SERVICE = "${PN}.service"

PACKAGES =+ "${PN}-systemd"
FILES_${PN}-systemd += "${base_libdir}/systemd"
RDEPENDS_${PN}-systemd += "${PN}"
