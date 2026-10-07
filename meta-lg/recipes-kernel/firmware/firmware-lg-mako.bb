DESCRIPTION = "Firmware for the LG Nexus 4 (mako): WCNSS (WiFi/BT), DSPs, video, NFC"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

PACKAGE_ARCH = "${MACHINE_ARCH}"

COMPATIBLE_MACHINE = "^mako$"

PV = "20190316"

# Qualcomm/Broadcom blobs from the LineageOS 16.0 proprietary vendor tree, the
# WiFi (prima) config and calibration from LineageOS' mako device tree - the
# same split postmarketOS' firmware-lg-mako uses. Both are pinned to a commit so
# the checksums below stay valid if the branches move.
#
# The Adreno 320 microcode (qcom/a300_pfp.fw, a300_pm4.fw) is deliberately not
# installed here: linux-firmware-qcom-adreno-a3xx already ships it and two
# packages owning one path breaks the rootfs.
VENDOR_REV = "55da620cdee6eec27215e9e1ba499dc57856fe2b"
DEVICE_REV = "5b211791f83b63a22c0d6055d256de71b484345b"

SRC_URI = " \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/wcnss.b00;name=wcnss_b00 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/wcnss.b01;name=wcnss_b01 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/wcnss.b02;name=wcnss_b02 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/wcnss.b04;name=wcnss_b04 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/wcnss.b05;name=wcnss_b05 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/wcnss.mdt;name=wcnss_mdt \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/dsps.b00;name=dsps_b00 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/dsps.b01;name=dsps_b01 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/dsps.b02;name=dsps_b02 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/dsps.b03;name=dsps_b03 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/dsps.b04;name=dsps_b04 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/dsps.b05;name=dsps_b05 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/dsps.mdt;name=dsps_mdt \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/q6.b00;name=q6_b00 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/q6.b01;name=q6_b01 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/q6.b03;name=q6_b03 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/q6.b04;name=q6_b04 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/q6.b05;name=q6_b05 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/q6.b06;name=q6_b06 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/q6.mdt;name=q6_mdt \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/tzapps.b00;name=tzapps_b00 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/tzapps.b01;name=tzapps_b01 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/tzapps.b02;name=tzapps_b02 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/tzapps.b03;name=tzapps_b03 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/tzapps.mdt;name=tzapps_mdt \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/vidc.b00;name=vidc_b00 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/vidc.b01;name=vidc_b01 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/vidc.b02;name=vidc_b02 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/vidc.b03;name=vidc_b03 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/vidc.mdt;name=vidc_mdt \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/vidc_1080p.fw;name=vidc_1080p_fw \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/bcm2079x_firmware.ncd;name=bcm2079x_firmware_ncd \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/bcm2079x_pre_firmware.ncd;name=bcm2079x_pre_firmware_ncd \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/keymaster/keymaster.b00;name=keymaster_b00 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/keymaster/keymaster.b01;name=keymaster_b01 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/keymaster/keymaster.b02;name=keymaster_b02 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/keymaster/keymaster.b03;name=keymaster_b03 \
    https://github.com/TheMuppets/proprietary_vendor_lge/raw/${VENDOR_REV}/mako/proprietary/vendor/firmware/keymaster/keymaster.mdt;name=keymaster_mdt \
    https://github.com/LineageOS/android_device_lge_mako/raw/${DEVICE_REV}/wifi/WCNSS_qcom_cfg.ini;name=WCNSS_qcom_cfg_ini \
    https://github.com/LineageOS/android_device_lge_mako/raw/${DEVICE_REV}/wifi/WCNSS_cfg.dat;name=WCNSS_cfg_dat \
    https://github.com/LineageOS/android_device_lge_mako/raw/${DEVICE_REV}/wifi/WCNSS_qcom_wlan_nv.bin;name=WCNSS_qcom_wlan_nv_bin \
"

SRC_URI[wcnss_b00.sha256sum] = "e664bca26a15570ff43ed2a33a42c5ed99cf11986b018f771a7f60169ac5c9c0"
SRC_URI[wcnss_b01.sha256sum] = "5beac201325b8b20c8ac0482a5f28e5a7da039ad32eac371b1168d7f7d4407dc"
SRC_URI[wcnss_b02.sha256sum] = "10692b81e15d5ecec57673bb11a6d6de3880ea5b9902f1bf9001696255eb61a4"
SRC_URI[wcnss_b04.sha256sum] = "94faaf051b1ccbc2ee6cb1d6f5652dc12a25bd24bb6d8c3581de2ecbb0476901"
SRC_URI[wcnss_b05.sha256sum] = "830804f6935ea8691466ecb87a03e4a9be64d8929fcbbf9fab9c3e87299f3348"
SRC_URI[wcnss_mdt.sha256sum] = "f101ee1ba7a169800867ad4d4b09f9625f7f9bff3eaf0164d95c86a5d58c1eda"
SRC_URI[dsps_b00.sha256sum] = "fda5deb27d1447b89402f543ba3f2d970011f5a828ae2472f126df147edc25cf"
SRC_URI[dsps_b01.sha256sum] = "bc66ae40531017b178b3d237289051ea7dd40fa898123a1732ccc9c15fae4280"
SRC_URI[dsps_b02.sha256sum] = "7a837879b1c5126f76cb550d237dc724c10f75534c4ffd94d4a1b88586c32e26"
SRC_URI[dsps_b03.sha256sum] = "62e99bef466fedd04d859c7769b1f7e16c9d02f762553519313e4de3af745313"
SRC_URI[dsps_b04.sha256sum] = "d22c3d0ec550969dc730f13e6cdc8d3a451a07df1d14cec02712dc003ab340e6"
SRC_URI[dsps_b05.sha256sum] = "6389f5c18b88266ae2a41ae64dbb006d08604ae34ceb0e641377b2fa6c501509"
SRC_URI[dsps_mdt.sha256sum] = "996a01c96885cd041300d442c13856439eb7aee86aa57c11d7521bee833195e0"
SRC_URI[q6_b00.sha256sum] = "e43bb5ab3d8c5f24cb491f423b3d837db204ee6cdb01e5eea8846d0487fb666d"
SRC_URI[q6_b01.sha256sum] = "07aa04c0c2d5228619fc17ac436ed0b51ae638afb1d1c8b995d156fb74cca2d2"
SRC_URI[q6_b03.sha256sum] = "c27a816ef345ca67659dcc654d5566efc6899f705b4a86d45c9dc40910ac45ca"
SRC_URI[q6_b04.sha256sum] = "cb8068f01daeee9efd4e5c84ae97d6a3209f7915a811d1229acb2fddc7b4a4c5"
SRC_URI[q6_b05.sha256sum] = "06815487a35b30b904d7ff77832a1ced6c1caf9e659b235c10ce3e56428db2e5"
SRC_URI[q6_b06.sha256sum] = "ad9c2c65dbf6052de459273a9664c0a81604a4ab72b55ec7a27bf0720f3dc5b8"
SRC_URI[q6_mdt.sha256sum] = "f7f5e7ef237ad425d48d76ec4e01511d42ca17e7a93f6e895e3ad7c7899d9498"
SRC_URI[tzapps_b00.sha256sum] = "e9680406158d5fafd44d147da32399f62e1226a3b963cf6327e02d2741766617"
SRC_URI[tzapps_b01.sha256sum] = "e07ebc66a74692c240d6af95b591bf5723f90d8afc57c479795d36f0c50bf5c6"
SRC_URI[tzapps_b02.sha256sum] = "8f4e626ceaec58d191923695513f601ce1e7d44034647affebb8c8f9703cc592"
SRC_URI[tzapps_b03.sha256sum] = "9a03fe650303864c9b4b92711e004a45c40ff89d95baffaf6e514ba7b7663951"
SRC_URI[tzapps_mdt.sha256sum] = "1d43a6094069f09f749cfe83075ef309e0ace2bb712444d8e00fe9fbad2e185e"
SRC_URI[vidc_b00.sha256sum] = "87b393d31edbaf4aecb26021aa3298f35248da54154d54ef4565910193c4df99"
SRC_URI[vidc_b01.sha256sum] = "6909e9507dbc2d913f48c178ffbfac8ddbd12b0401e7f312737a46f234f6643e"
SRC_URI[vidc_b02.sha256sum] = "9e14deab4a61a9737f891a194519350a6c09b4bdd0a93cbabab74c449b803b20"
SRC_URI[vidc_b03.sha256sum] = "85abd019ba36c10926d96d74e189d2429b1bdad6bc374bd51e8219982656e13e"
SRC_URI[vidc_mdt.sha256sum] = "0d0ee836a57d839d5b0d3f8e79ee6b35a46e750f1354c5159b4057489c189599"
SRC_URI[vidc_1080p_fw.sha256sum] = "a51a6e00d48314f03c813850e22236062dc2239be1476e8288c478468f424727"
SRC_URI[bcm2079x_firmware_ncd.sha256sum] = "f1a23c17eab0fcbc6da84f3404391c5788c97ec2b57d1475413bf51b899b4fec"
SRC_URI[bcm2079x_pre_firmware_ncd.sha256sum] = "9589042523c020f24bb8e8ee38b744ab7edce4405afa5f4866933ce35104025e"
SRC_URI[keymaster_b00.sha256sum] = "e4d33a25aea29e2dbd18a6d73444b64ae44b3d54464cba2233302060568be6fe"
SRC_URI[keymaster_b01.sha256sum] = "cda9f000b927ea03797507bdda68ad7a00026d0fd2e57720644f7cd807229343"
SRC_URI[keymaster_b02.sha256sum] = "d1ab4b5e0fa79f19b6f6df0bc47bf38c2b2a1e7e4f41233f48c77e7f26a40f54"
SRC_URI[keymaster_b03.sha256sum] = "56b532e78b65d224cb9637f5b4d1604b9faa8404e987a326e569dc8b10bb1588"
SRC_URI[keymaster_mdt.sha256sum] = "42ac340be7278ba0218b083617e751210a10dbbbb68a7c6457025640675d36d7"
SRC_URI[WCNSS_qcom_cfg_ini.sha256sum] = "7e0a7c912ed0436b35b61d85bf45102438becb93c6291f527a7d6dfe89879df8"
SRC_URI[WCNSS_cfg_dat.sha256sum] = "8bcd6656caa24d81acff4069d6db88b60a5a15b22e806da1bd473c1848070ecf"
SRC_URI[WCNSS_qcom_wlan_nv_bin.sha256sum] = "d206db9d9e4659a314166d9191483c0bd6f7532f6f55a125bf24a97b3a9a04f6"

S = "${UNPACKDIR}"

do_install() {
    fwdir=${D}${nonarch_base_libdir}/firmware

    install -d $fwdir $fwdir/keymaster $fwdir/wlan/prima

    # Remoteproc / PIL images are loaded by name from the firmware root.
    for f in wcnss.b00 wcnss.b01 wcnss.b02 wcnss.b04 wcnss.b05 wcnss.mdt dsps.b00 dsps.b01 dsps.b02 dsps.b03 dsps.b04 dsps.b05 dsps.mdt q6.b00 q6.b01 q6.b03 q6.b04 q6.b05 q6.b06 q6.mdt tzapps.b00 tzapps.b01 tzapps.b02 tzapps.b03 tzapps.mdt vidc.b00 vidc.b01 vidc.b02 vidc.b03 vidc.mdt vidc_1080p.fw bcm2079x_firmware.ncd bcm2079x_pre_firmware.ncd; do
        install -m 0644 ${UNPACKDIR}/$f $fwdir/$f
    done
    for f in keymaster.b00 keymaster.b01 keymaster.b02 keymaster.b03 keymaster.mdt; do
        install -m 0644 ${UNPACKDIR}/$f $fwdir/keymaster/$f
    done

    # wcn36xx asks for these under wlan/prima/.
    for f in WCNSS_qcom_cfg.ini WCNSS_cfg.dat WCNSS_qcom_wlan_nv.bin; do
        install -m 0644 ${UNPACKDIR}/$f $fwdir/wlan/prima/$f
    done
}

# The .mdt/.bNN files are ELF segments of Hexagon/ARM firmware, not host code.
INSANE_SKIP:${PN} += "arch"
FILES:${PN} = "${nonarch_base_libdir}"
