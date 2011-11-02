FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
SRC_URI_append_om-gta02 = "file://glamo.patch"

EXTRA_OECONF_append_om-gta02 = " --enable-glamo"
PRINC = "1"
