FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
PRINC := "${@int(PRINC) + 8}"

PACKAGE_ARCH_palmpre = "${MACHINE_ARCH}"

SRC_URI += "file://include-cy8mrln-header.patch"
EXTRA_OECONF += "--enable-cy8mrln-palmpre"
