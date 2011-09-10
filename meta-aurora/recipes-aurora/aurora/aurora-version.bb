LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PV = "${DISTRO_VERSION}"

PACKAGES = "${PN}"
PACKAGE_ARCH = "${MACHINE_ARCH}"

do_install() {
	mkdir -p ${D}${sysconfdir}
	echo "Aurora ${DISTRO_VERSION}" > ${D}${sysconfdir}/aurora-version
	echo "Built from branch: ${METADATA_BRANCH}" >> ${D}${sysconfdir}/aurora-version
	echo "Revision: ${METADATA_REVISION}" >> ${D}${sysconfdir}/aurora-version
}
