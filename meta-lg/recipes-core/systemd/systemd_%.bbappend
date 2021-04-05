FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
SRC_URI_append_halium_arm = " file://0001-systemd-hostnamed-disable-network-protection.patch"
