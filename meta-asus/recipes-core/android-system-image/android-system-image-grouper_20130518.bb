require recipes-core/android-system-image/android-system-image.inc

PR = "${INC_PR}.1"

COMPATIBLE_MACHINE = "grouper"

CODENAME = "grouper"
BUILDVERSION = "20150518-15"

SRC_URI = "http://build.webos-ports.org/phablet/maguro/phablet-${CODENAME}-${BUILDVERSION}.tar.bz2"
SRC_URI[rootfs] = "a39aee9322899ee0b2a83649c822fad2"
SRC_URI[rootfs] = "0427c037f90a229b0672227f59aaaa7dcff65519c83995c634c1de50a3351717"
