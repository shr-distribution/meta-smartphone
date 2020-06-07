DESCRIPTION = "Tiny Android system to leverage android hardware drivers through libhybris"
LICENSE = "Apache-2.0 & Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

PACKAGE_ARCH = "${MACHINE_ARCH}"

DEPENDS = "android-tools-native e2fsprogs-native"

RPROVIDES_${PN} += "virtual/android-system-image"

INC_PR = "r6"

do_install() {
    install -d ${D}/android
    install -d ${D}/userdata
    install -d ${D}${localstatedir}/lib/lxc/android

    # convert the sparse android image into a regular ext4 image
    if file ${WORKDIR}/system.img | grep -v ": Linux rev 1.0 ext4" >/dev/null; then
        mv ${WORKDIR}/system.img ${WORKDIR}/system.img.sparse
        simg2img ${WORKDIR}/system.img.sparse ${WORKDIR}/system.img
        resize2fs -M ${WORKDIR}/system.img
        rm ${WORKDIR}/system.img.sparse
    fi

    install -m 0644 ${WORKDIR}/system.img ${D}${localstatedir}/lib/lxc/android/system.img

    install -d ${D}${sysconfdir}
    ln -sf /system/etc/wifi ${D}${sysconfdir}/wifi
    ln -sf /system/etc/media_codecs.xml ${D}${sysconfdir}/media_codecs.xml
    ln -sf /system/etc/media_profiles.xml ${D}${sysconfdir}/media_profiles.xml
    # add a link for firmware, hardcoded in bluez5's hciattach for bcm43xx
    ln -sf /system/vendor/firmware ${D}${sysconfdir}/firmware

    for i in cache data factory firmware persist system; do
       ln -sf /android/$i ${D}/$i
    done
    ln -sf /system/vendor ${D}/vendor
}

FILES_${PN} = "${localstatedir}/lib/lxc/android/system.img ${sysconfdir} /android /cache /data /factory /firmware /persist /system /vendor /userdata"
