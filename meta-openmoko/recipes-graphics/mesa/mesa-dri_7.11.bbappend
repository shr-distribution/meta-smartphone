FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
SRC_URI_append_arm = " file://glamo.patch"
DRIDRIVERS_append_arm = ",glamo"
PRINC = "1"
