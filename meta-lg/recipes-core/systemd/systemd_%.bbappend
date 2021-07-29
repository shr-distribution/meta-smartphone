FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"
SRC_URI:append_halium:arm = " file://0001-systemd-hostnamed-disable-network-protection.patch"
