DESCRIPTION = "Parse the Android 'super' dynamic partition layout into a dmsetup concise table"
HOMEPAGE = "https://github.com/droidian/parse-android-dynparts"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"
SECTION = "base"

# Dynamic partitions are mandatory from Android 10: system/vendor/product become
# logical partitions inside 'super', described by a custom (non-LVM) header. The
# initramfs/host has to map them with dm-linear before anything can be mounted.
# This is the tool Droidian and UBports both use to emit the dmsetup table.

DEPENDS = "openssl"

SRC_URI = "git://github.com/droidian/parse-android-dynparts.git;branch=droidian;protocol=https"

PV = "0.1+git${SRCPV}"
SRCREV = "b776ff276f20b91c8df4e2c84cf314ba7b7a3fb5"

S = "${UNPACKDIR}/${BB_GIT_DEFAULT_DESTSUFFIX}"

inherit cmake

# No runtime deps: the tool only prints a dmsetup concise table on stdout,
# it does not talk to device-mapper itself. The caller needs dmsetup.
