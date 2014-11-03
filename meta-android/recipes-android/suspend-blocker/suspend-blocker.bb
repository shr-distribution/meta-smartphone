SUMMARY = "Kernel suspend wakelock blocker diagnostics utility"
HOMEPAGE = "http://kernel.ubuntu.com/~cking/suspend-blocker/"
LICENSE = "GPL-2.0"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

DEPENDS += "cjson"

PV = "0.1.22+gitr${SRCPV}"
PR = "r0"

SRC_URI = "git://kernel.ubuntu.com/cking/suspend-blocker.git;protocol=git;branch=master"
S = "${WORKDIR}/git"

SRCREV = "9aa645305ff4870a3f825c7767937fa6cd5ae6d5"

do_compile() {
    oe_runmake CFLAGS="-DVERSION='\"${BASE_PV}\"'" LDFLAGS='-ljson-c -ljson -lm'
}

do_install() {
    oe_runmake DESTDIR=${D} install
}
