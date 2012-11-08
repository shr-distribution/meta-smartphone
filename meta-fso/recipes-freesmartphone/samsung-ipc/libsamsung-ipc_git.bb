require ${BPN}.inc

DEFAULT_PREFERENCE = "-1"

LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

SRCREV = "1c46f2f4e6d462799878844344860e836cae4b3f"

PV = "0.2.0+gitr${SRCPV}"

SAMSUNGIPC_BRANCH ?= "master"
SRC_URI = "git://github.com/morphis/libsamsung-ipc.git;protocol=git;branch=${SAMSUNGIPC_BRANCH}"
S = "${WORKDIR}/git"
