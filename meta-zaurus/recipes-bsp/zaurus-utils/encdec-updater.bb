DESCRIPTION = "A tool to encode and decode the Sharp Zaurus updater.sh script"
SECTION = "console/utils"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://encsh.c;endline=15;md5=d73360c98c2885be19245f2a8c728f38"
PR = "r2"

SRC_URI = "http://distro.ibiblio.org/pub/linux/distributions/pdaxrom/download/1.1.0beta4/src/survive-1.1.0.tar.bz2"

SRC_URI[md5sum] = "b6cb17168a424a5d757309dce4a81841"
SRC_URI[sha256sum] = "0d02edc12509f6dc98cded37c68238f939c522cbd398fd527785a1e7e3bcfcea"

S = "${WORKDIR}/survive-1.1.0"

do_compile() {
        ${CC} ${LDFLAGS} -o encdec-updater encsh.c
}

do_install() {
        install -d ${D}${bindir}
        install -m 0755 encdec-updater ${D}${bindir}/
}

BBCLASSEXTEND = "native"

COMPATIBLE_MACHINE = "(poodle|c7x0|spitz|akita|tosa)"

NATIVE_INSTALL_WORKS = "1"
