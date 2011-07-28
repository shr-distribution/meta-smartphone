DESCRIPTION = "Nandlogical for Sharp mtd1"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://nandlogical.c;md5=a734efd470f4d5be324e5d408a1f38a5"
PR = "r5"

SRC_URI = "file://nandlogical.c"

S = "${WORKDIR}"

do_compile () {
        ${CC} ${CFLAGS} ${LDFLAGS} nandlogical.c -o nandlogical
}
do_install () {
        install -d ${D}${bindir}/
        install -m 0755 nandlogical ${D}${bindir}/
}

COMPATIBLE_MACHINE = "(poodle|c7x0|akita|spitz|tosa)"
