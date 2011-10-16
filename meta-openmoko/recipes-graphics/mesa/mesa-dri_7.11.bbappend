FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
SRC_URI_armv4t += "file://glamo.patch"
DRIDRIVERS_append_armv4t = ",glamo"
