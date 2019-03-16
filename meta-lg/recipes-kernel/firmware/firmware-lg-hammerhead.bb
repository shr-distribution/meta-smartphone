DESCRIPTION = "Linux Adreno firmwares for hammerhead"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

PACKAGE_ARCH = "${MACHINE_ARCH}"

COMPATIBLE_MACHINE = "hammerhead"

PV = "20190316"

SRC_URI = " \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/lineage-16.0/hammerhead/proprietary/vendor/firmware/a330_pfp.fw;name=a330pfp \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/lineage-16.0/hammerhead/proprietary/vendor/firmware/a330_pm4.fw;name=a330pm4 \
    https://bshah.in/hammerhead-firmware.tar.gz;name=adspmodem;subdir=hammerhead-firmware \
"
SRC_URI[a330pfp.md5sum] = "d8b8333e3970aa2913d11ebb9470a7fc"
SRC_URI[a330pfp.sha256sum] = "e4ee76b287961f56e6a606ed5baf7127d9fd9a0d421b516af32fa655f1fdb5de"
SRC_URI[a330pm4.md5sum] = "71ce5efe6aafc8393c135b333d495c3d"
SRC_URI[a330pm4.sha256sum] = "2057bff39ad2ab0b4d4ad755c52bf9d0c139293aa823bde992094ce27304b000"
SRC_URI[adspmodem.md5sum] = "ca602372db590d32ed4d15e97872cfe6"
SRC_URI[adspmodem.sha256sum] = "dcf029fa03d1d67d30c1e7f9290e4230e7d8e8a8f3de5b0eba7bf429bc90151e"

do_install() {
    install -d ${D}/lib/firmware
    install -d ${D}/lib/firmware/qcom
    
    # Adreno a330 firmware
    install -m 0644 ${WORKDIR}/a330_pfp.fw ${D}/lib/firmware/qcom/a330_pfp.fw
    install -m 0644 ${WORKDIR}/a330_pm4.fw ${D}/lib/firmware/qcom/a330_pm4.fw
    
    # modem firmware
	for _i in ${WORKDIR}/hammerhead-firmware/modem.* ${WORKDIR}/hammerhead-firmware/mba.*; do
		install -m 0644 $_i ${D}/lib/firmware/$(basename $_i)
	done
    # adsp firmware
	for _i in ${WORKDIR}/hammerhead-firmware/adsp.*; do
		install -m 0644 $_i ${D}/lib/firmware/$(basename $_i)
	done
}

INSANE_SKIP_${PN} += "arch"
FILES_${PN} = "/lib"
