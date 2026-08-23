DESCRIPTION = "Tiny Android system to leverage android hardware drivers through libhybris"
LICENSE = "Apache-2.0 & Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

PACKAGE_ARCH = "${MACHINE_ARCH}"

DEPENDS = "android-tools-native e2fsprogs-native"

INC_PR = "r7"

ANDROID_SYSTEM_IMAGE_DESTNAME ?= "system.img"

do_install() {
    install -d ${D}/android
    install -d ${D}/userdata
    install -d ${D}${localstatedir}/lib/lxc/android

    # convert the sparse android image into a regular ext2/3/4 image
    if file ${UNPACKDIR}/system.img | grep -v ": Linux rev 1.0 ext" >/dev/null; then
        mv ${UNPACKDIR}/system.img ${UNPACKDIR}/system.img.sparse
        simg2img ${UNPACKDIR}/system.img.sparse ${UNPACKDIR}/system.img
        resize2fs -M ${UNPACKDIR}/system.img
        rm ${UNPACKDIR}/system.img.sparse
    fi

    install -m 0644 ${UNPACKDIR}/system.img ${D}${localstatedir}/lib/lxc/android/${ANDROID_SYSTEM_IMAGE_DESTNAME}

    if [ -e ${UNPACKDIR}/vendor.img ]; then
        # convert the sparse android image into a regular ext4 image
        if file ${UNPACKDIR}/vendor.img | grep -v ": Linux rev 1.0 ext" >/dev/null; then
            mv ${UNPACKDIR}/vendor.img ${UNPACKDIR}/vendor.img.sparse
            simg2img ${UNPACKDIR}/vendor.img.sparse ${UNPACKDIR}/vendor.img
            resize2fs -M ${UNPACKDIR}/vendor.img
            rm ${UNPACKDIR}/vendor.img.sparse
        fi

        install -m 0644 ${UNPACKDIR}/vendor.img ${D}${localstatedir}/lib/lxc/android/vendor.img
    fi

    # The vndbinder context manager, when the Android build shipped one.
    #
    # A stock Android 11 vendor's own /vendor/bin/vndservicemanager is AOSP's,
    # and its Access constructor does CHECK(selinux_status_open(true) >= 0),
    # which cannot succeed in a container that has no selinuxfs. It aborts,
    # init restarts it forever, and nothing becomes the context manager for
    # /dev/vndbinder - which strands every vendor HAL that opens it, the
    # display composer included. mount-android.sh binds this over the vendor
    # one; see the long comment there.
    #
    # It has to come from the same build as system.img: it links the VNDK
    # libbinder, whose Parcel wire format has to match the rest of the
    # userspace it talks to.
    if [ -e ${UNPACKDIR}/vndservicemanager ]; then
        install -m 0755 ${UNPACKDIR}/vndservicemanager ${D}${localstatedir}/lib/lxc/android/vndservicemanager
    fi

    # also install ramdisk image if any
    if [ -e ${UNPACKDIR}/ramdisk.img ]; then
        install -m 0644 ${UNPACKDIR}/ramdisk.img ${D}${localstatedir}/lib/lxc/android/android-ramdisk.img
    fi

    install -d ${D}${sysconfdir}
    ln -sf /system/etc/wifi ${D}${sysconfdir}/wifi
    ln -sf /system/etc/media_codecs.xml ${D}${sysconfdir}/media_codecs.xml
    ln -sf /system/etc/media_profiles.xml ${D}${sysconfdir}/media_profiles.xml
    # add a link for firmware, hardcoded in bluez5's hciattach for bcm43xx
    ln -sf /system/vendor/firmware ${D}${sysconfdir}/firmware

    for i in cache data factory firmware persist system; do
       ln -sf /android/$i ${D}/$i
    done

    if [ -e ${UNPACKDIR}/vendor.img ]; then
        ln -sf /android/vendor ${D}/vendor
    else
        ln -sf /system/vendor ${D}/vendor
    fi
}

FILES:${PN} = "${localstatedir}/lib/lxc/android \
               ${sysconfdir} \
               /android /cache /data /factory /firmware /persist /system /vendor /userdata \
"

# vndservicemanager comes out of the Android build already stripped, and it is
# a bionic binary that never runs against anything this recipe's toolchain
# produced, so the usual ELF checks have nothing useful to say about it.
INSANE_SKIP:${PN} += "already-stripped ldflags"
