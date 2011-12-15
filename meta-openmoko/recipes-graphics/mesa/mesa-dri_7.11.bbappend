FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
SRC_URI_append_armv4 = " file://glamo.patch"
DRIDRIVERS_append_armv4 = ",glamo"
PRINC := "${@int(PRINC) + 2}"
