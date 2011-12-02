FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
SRC_URI_append_arm = " file://glamo.patch"
PRINC := "${@int(PRINC) + 1}"

EXTRA_OECONF_append = " --enable-glamo-experimental-api"
