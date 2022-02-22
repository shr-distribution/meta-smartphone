SUMMARY = "Kernel suspend wakelock blocker diagnostics utility"
HOMEPAGE = "http://kernel.ubuntu.com/~cking/suspend-blocker/"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

DEPENDS = "json-c"

PV = "0.1.22+gitr${SRCPV}"

SRC_URI = "git://kernel.ubuntu.com/cking/suspend-blocker.git;branch=master \
    file://0001-suspend-blocker.c-update-json.h-path.patch \
"
S = "${WORKDIR}/git"

SRCREV = "9aa645305ff4870a3f825c7767937fa6cd5ae6d5"

do_compile() {
    oe_runmake CFLAGS="-DVERSION='\"${PV}\"'" LDFLAGS='${LDFLAGS} -ljson-c -lm'
}

do_install() {
    oe_runmake DESTDIR=${D} install
}
