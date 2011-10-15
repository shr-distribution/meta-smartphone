FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
SRC_URI_armv4t += "file://glamo.patch"

EXTRA_OECONF_append = " --enable-glamo-experimental-api"
