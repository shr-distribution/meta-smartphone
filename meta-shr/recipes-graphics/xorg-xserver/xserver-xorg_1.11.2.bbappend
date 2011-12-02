FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
PRINC := "${@int(PRINC) + 1}"
SRC_URI += "file://randr-support.patch"
