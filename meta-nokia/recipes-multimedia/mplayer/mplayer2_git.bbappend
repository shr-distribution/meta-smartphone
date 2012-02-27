FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI_append_armv7a = " file://0001-video-out-for-omapfb-support.patch"
