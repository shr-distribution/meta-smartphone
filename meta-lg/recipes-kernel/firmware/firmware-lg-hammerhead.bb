DESCRIPTION = "Linux Adreno firmwares for hammerhead"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

PACKAGE_ARCH = "${MACHINE_ARCH}"

COMPATIBLE_MACHINE = "hammerhead"

PV = "20190316"

SRC_URI = " \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/lineage-16.0/hammerhead/proprietary/vendor/firmware/a330_pfp.fw;name=a330pfp \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/lineage-16.0/hammerhead/proprietary/vendor/firmware/a330_pm4.fw;name=a330pm4 \
    https://github.com/wkennington/linux-firmware/raw/master/brcm/brcmfmac4339-sdio.bin;name=bcrm \
    https://github.com/Tofee/mainline-kernel-firmware/raw/main/hammerhead-firmware.tar.gz;name=adspmodem;subdir=hammerhead-firmware \
    file://brcmfmac4339-sdio.txt \
"
SRC_URI[a330pfp.sha256sum] = "e4ee76b287961f56e6a606ed5baf7127d9fd9a0d421b516af32fa655f1fdb5de"
SRC_URI[a330pm4.sha256sum] = "2057bff39ad2ab0b4d4ad755c52bf9d0c139293aa823bde992094ce27304b000"
SRC_URI[bcrm.sha256sum] = "191ac3c79a1ad81ff664202d9f028d01e13673449b206423a75d5c226d343be5"
SRC_URI[adspmodem.sha256sum] = "dcf029fa03d1d67d30c1e7f9290e4230e7d8e8a8f3de5b0eba7bf429bc90151e"

do_install() {
    install -d ${D}/${nonarch_base_libdir}/firmware
    install -d ${D}/${nonarch_base_libdir}/firmware/qcom
    install -d ${D}/${nonarch_base_libdir}/firmware/brcm
    
    # Adreno a330 firmware
    install -m 0644 ${WORKDIR}/a330_pfp.fw ${D}/${nonarch_base_libdir}/firmware/qcom/a330_pfp.fw
    install -m 0644 ${WORKDIR}/a330_pm4.fw ${D}/${nonarch_base_libdir}/firmware/qcom/a330_pm4.fw
    
    # modem firmware
	for _i in ${WORKDIR}/hammerhead-firmware/modem.* ${WORKDIR}/hammerhead-firmware/mba.*; do
		install -m 0644 $_i ${D}/${nonarch_base_libdir}/firmware/$(basename $_i)
	done
    # adsp firmware
	for _i in ${WORKDIR}/hammerhead-firmware/adsp.*; do
		install -m 0644 $_i ${D}/${nonarch_base_libdir}/firmware/$(basename $_i)
	done
    
    # wifi firmware
	install -m 0644 ${WORKDIR}/brcmfmac4339-sdio.txt ${D}/${nonarch_base_libdir}/firmware/brcm/brcmfmac4339-sdio.txt
	install -m 0644 ${WORKDIR}/brcmfmac4339-sdio.bin ${D}/${nonarch_base_libdir}/firmware/brcm/brcmfmac4339-sdio.bin
}

INSANE_SKIP:${PN} += "arch"
FILES:${PN} = "${nonarch_base_libdir}"
