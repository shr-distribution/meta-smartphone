FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
SRC_URI_armv4t += "file://glamo.patch"
EXTRA_DRIDRIVERS_armv4t += "glamo,"
