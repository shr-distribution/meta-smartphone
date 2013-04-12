require recipes-core/android-system-image/android-system-image.inc

PR = "${INC_PR}.0"

COMPATIBLE_MACHINE = "tuna"

SRC_URI = "http://build.webos-ports.org/phablet/maguro/cm-10.1-${PV}-UNOFFICIAL-maguro.zip"
SRC_URI[md5sum] = "a39aee9322899ee0b2a83649c822fad2"
SRC_URI[sha256sum] = "0427c037f90a229b0672227f59aaaa7dcff65519c83995c634c1de50a3351717"
