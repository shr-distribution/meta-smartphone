require ${BPN}.inc

## palmpre
#  palmpre_quirks.so
#  accelerometer_kxsd9.so
#  proximity_palmpre.so (dropped in f3bd874dc0422558bd709ee706d02f28d09a6d59)
#  vibrator_omapvibe.so
#  backlight_omappanel.so
RDEPENDS_${PN}_palmpre += "fsodeviced-module-accelerometer-kxsd9"
RDEPENDS_${PN}_palmpre += "fsodeviced-module-palmpre-quirks"
RDEPENDS_${PN}_palmpre += "fsodeviced-module-backlight-omappanel"
RDEPENDS_${PN}_palmpre += "fsodeviced-module-vibrator-omapvibe"
