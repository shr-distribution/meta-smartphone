FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
SRC_URI += "file://glamo.patch"

EXTRA_OECONF_append = " --enable-glamo-experimental-api"
