FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"
SRC_URI:append:halium:arm = " file://0001-systemd-hostnamed-disable-network-protection.patch"
