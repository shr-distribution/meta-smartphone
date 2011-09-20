# RRECOMMENDS_${PN} = "gpsd-conf gpsd-udev"
RRECOMMENDS_${PN} = "gpsd-conf"
PRINC = "1"
# we don't want udev pulled by gpsd-udev to our images and BAD_RECOMMENDATIONS doesn't seem to work in oe-core world :/
