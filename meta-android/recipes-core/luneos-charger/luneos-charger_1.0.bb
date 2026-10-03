SUMMARY = "Off-mode charging screen for the Halium initramfs"
DESCRIPTION = "Shows the LuneOS logo, an animated battery and the charge level \
when a charger is plugged into a powered-off phone (androidboot.mode=charger), \
and lights the notification LED red while charging and green when charged. \
The power key wakes the screen; holding it reboots into LuneOS. Run by \
initramfs-scripts-halium's init.sh, which reboots or powers off on its exit \
code."
SECTION = "base"
# The code is Apache-2.0. logo.png is the LuneOS logo from
# webOS-ports/org.webosports.app.photos (Apache-2.0); text.png is text rendered
# from Noto Sans (SIL OFL 1.1, which places no conditions on rendered output).
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

PV = "1.0"

# Nothing but local files, so nothing ever lands in the default
# S = "${UNPACKDIR}/${BP}" and do_unpack warns about it.
S = "${UNPACKDIR}"

# gen_assets.py is not run here (it needs Pillow, which nothing meta-android
# depends on provides); it is shipped so the artwork can be regenerated - see
# the script for how.
SRC_URI = " \
    file://luneos-charger.c \
    file://gen_assets.py \
    file://logo.png \
    file://text.png \
    file://text.idx \
"

# zlib decodes the PNG artwork.
DEPENDS = "zlib"

do_compile() {
    ${CC} ${CFLAGS} ${LDFLAGS} -DASSET_DIR='"${datadir}/luneos-charger"' \
        -o ${B}/luneos-charger ${UNPACKDIR}/luneos-charger.c -lz
}

do_install() {
    install -d ${D}${sbindir} ${D}${datadir}/luneos-charger
    install -m 0755 ${B}/luneos-charger ${D}${sbindir}/luneos-charger
    install -m 0644 ${UNPACKDIR}/logo.png ${UNPACKDIR}/text.png \
        ${UNPACKDIR}/text.idx ${D}${datadir}/luneos-charger/
}
