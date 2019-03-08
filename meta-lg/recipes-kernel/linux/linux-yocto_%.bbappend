COMPATIBLE_MACHINE_hammerhead = "(^hammerhead$)"

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += " \
  file://0001-Fix-Hammerhead-backlight.patch \
  file://defconfig \
"
