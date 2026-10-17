DESCRIPTION = "Tiny Android system to leverage android hardware drivers through libhybris"
LICENSE = "Apache-2.0 & Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

PACKAGE_ARCH = "${MACHINE_ARCH}"

DEPENDS = "android-tools-native e2fsprogs-native"

INC_PR = "r8"

ANDROID_SYSTEM_IMAGE_DESTNAME ?= "system.img"

# The machine recipes fetch bare .img files (or archives that unpack straight
# into ${UNPACKDIR}), which is also where do_install looks for them, so the
# default S = "${UNPACKDIR}/${BP}" never comes into existence.
S = "${UNPACKDIR}"

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

    # The /system, /vendor, ... compatibility symlinks used to be created here.
    # They moved to android-system, because they are a property of the container
    # layout rather than of any particular system image: a machine that installs
    # no android-system-image - the GSI arriving as a separate artifact - still
    # needs them, and without /system the GSI's own absolute
    # /android/system_ext -> /system/system_ext dangles, so mount-apexes.py finds
    # no VNDK APEX and every vendor library fails to load.
}

FILES:${PN} = "${localstatedir}/lib/lxc/android \
               ${sysconfdir} \
               /android /userdata \
"

# vndservicemanager comes out of the Android build already stripped, and it is
# a bionic binary that never runs against anything this recipe's toolchain
# produced, so the usual ELF checks have nothing useful to say about it.
INSANE_SKIP:${PN} += "already-stripped ldflags"

# Write a host file into an ext image with debugfs, then prove it landed.
#
# Plain `debugfs -w -R "write ..."` returns 0 even when it could not allocate an
# inode (or blocks), so a file silently vanishes and the build still succeeds.
# This bit tissot: an AOSP system.img is sized with almost no spare inodes, the
# per-device GSI patching adds a dozen /system/etc/camera files, the last writes
# hit "Could not allocate inode", e2fsck still passed, and the image shipped
# without the front-camera chromatix XML - the camera then enumerated zero
# devices. The headroom itself is fixed in the image build (build_image.py keeps
# >=512 spare inodes), but every recipe that injects with debugfs must also
# verify the write, so a future tight image fails the build loudly rather than
# shipping broken. This rm's any existing file first (overwrites stay
# idempotent), writes, and aborts unless the on-image size matches the source.
android_image_write() {
    _aiw_src="$1"
    _aiw_img="$2"
    _aiw_dst="$3"
    if [ ! -f "${_aiw_src}" ]; then
        bbfatal "android_image_write: source ${_aiw_src} does not exist"
    fi
    debugfs -w -R "rm ${_aiw_dst}" "${_aiw_img}" >/dev/null 2>&1 || true
    debugfs -w -R "write ${_aiw_src} ${_aiw_dst}" "${_aiw_img}"
    _aiw_want=`stat -c %s "${_aiw_src}"`
    _aiw_got=`debugfs -R "stat ${_aiw_dst}" "${_aiw_img}" 2>/dev/null | grep -oE 'Size: [0-9]+' | head -1 | awk '{print $2}'`
    if [ "${_aiw_got}" != "${_aiw_want}" ]; then
        bbfatal "android_image_write: ${_aiw_dst} did not land in `basename ${_aiw_img}` (on-image size '${_aiw_got}', expected ${_aiw_want} bytes) - the image is out of inodes or space"
    fi
}
