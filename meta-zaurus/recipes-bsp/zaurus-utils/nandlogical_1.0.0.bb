DESCRIPTION = "Nandlogical for Sharp mtd1"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://nandlogical.c;endline=15;md5=77804e7fdf625e56869f32d9aec762a2"
PR = "r6"

SRC_URI = "http://distro.ibiblio.org/pub/linux/distributions/pdaxrom/download/1.1.0beta4/src/survive-1.1.0.tar.bz2 \
           file://oobblock-to-writesize.patch"

SRC_URI[md5sum] = "b6cb17168a424a5d757309dce4a81841"
SRC_URI[sha256sum] = "0d02edc12509f6dc98cded37c68238f939c522cbd398fd527785a1e7e3bcfcea"

S = "${WORKDIR}/survive-1.1.0"

do_compile () {
        ${CC} ${CFLAGS} ${LDFLAGS} nandlogical.c -o nandlogical
}

do_install () {
        install -d ${D}${bindir}/
        install -m 0755 nandlogical ${D}${bindir}/
}

COMPATIBLE_MACHINE = "(poodle|c7x0|akita|spitz|tosa)"
