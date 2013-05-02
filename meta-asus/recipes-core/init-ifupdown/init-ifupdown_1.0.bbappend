FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
PRINC := "${@int(PRINC) + 1}"

PACKAGE_ARCH_grouper = "${MACHINE_ARCH}"
