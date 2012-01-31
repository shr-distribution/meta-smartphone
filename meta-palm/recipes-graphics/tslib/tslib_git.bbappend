FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
PRINC := "${@int(PRINC) + 7}"

SRC_URI += "file://include-cy8mrln-header.patch"
EXTRA_OECONF += "--enable-cy8mrln-palmpre"
