# A userdata image with the rootfs inside it, for devices installed by fastboot.
#
# The ext4 an image recipe builds is the root filesystem itself. On a device
# with no custom recovery there is nothing to unpack it, so what gets flashed
# is a userdata filesystem holding that image as rootfs.img, which is where
# the halium initramfs looks for it.
#
# Wrapping it by hand is easy to get wrong and fails quietly: the initramfs
# carries on past a failed mount, /android is left empty, and the container
# dies with "mkdir: can't create directory '/android/vendor'" - a device that
# boots to nothing, with the reason four steps back.
#
# The filesystem is sized to the rootfs plus a little slack; it does not need
# to match the partition, because the initramfs grows it on first boot.

IMAGE_TYPES += "userdataimg"
IMAGE_TYPEDEP:userdataimg = "ext4"
do_image_userdataimg[depends] += "e2fsprogs-native:do_populate_sysroot"

USERDATA_IMAGE_SLACK ?= "128"

# The Android system image the initramfs mounts. A machine whose rootfs carries it
# (VIRTUAL-RUNTIME_android-system-image names a recipe) needs nothing here. A
# machine that sets that variable empty - the GSI is not in the rootfs, as on q25 -
# gets the pinned GSI next to rootfs.img as android-rootfs.img, the halium
# "file_layout" name halium-boot.sh looks for. Without it the container has no
# /android/system and the phone boots to nothing.
USERDATA_IMAGE_ANDROID_ROOTFS ?= "${@'halium-gsi-${MACHINE}.img' if d.getVar('VIRTUAL-RUNTIME_android-system-image') == '' else ''}"
do_image_userdataimg[depends] += "${@'halium-gsi-image:do_deploy' if d.getVar('USERDATA_IMAGE_ANDROID_ROOTFS') else ''}"

# Extra ext4 features for mke2fs to clear (or set), comma separated and merged into
# the one -O argument, e.g. "^64bit,^metadata_csum,^metadata_csum_seed,^orphan_file".
# The default mke2fs.conf of a current e2fsprogs turns those four on, and a kernel
# older than 3.18 (the SM-T520's 3.4) cannot mount them: the initramfs finds the
# partition and then fails to read it. Empty by default, so machines on newer
# kernels are unchanged.
USERDATA_IMAGE_FEATURES ?= ""

IMAGE_CMD:userdataimg () {
    staging="${WORKDIR}/userdata-staging"
    target="${IMGDEPLOYDIR}/${IMAGE_NAME}.userdataimg"

    rm -rf $staging
    mkdir -p $staging
    # Prefer the stable link; the versioned name carries the DATETIME of the
    # run that built the ext4, which is not this one when that task did not
    # have to re-run.
    rootfs_img="${IMGDEPLOYDIR}/${IMAGE_LINK_NAME}.ext4"
    if [ ! -e "$rootfs_img" ] ; then
        rootfs_img=`ls -1t ${IMGDEPLOYDIR}/*.ext4 2>/dev/null | head -n 1`
    fi
    if [ -z "$rootfs_img" ] ; then
        bbfatal "userdataimg: no ext4 rootfs in ${IMGDEPLOYDIR} to wrap"
    fi

    cp "$rootfs_img" $staging/rootfs.img

    if [ -n "${USERDATA_IMAGE_ANDROID_ROOTFS}" ] ; then
        android_img="${DEPLOY_DIR_IMAGE}/${USERDATA_IMAGE_ANDROID_ROOTFS}"
        if [ ! -e "$android_img" ] ; then
            bbfatal "userdataimg: no Android system image at $android_img"
        fi
        cp -L "$android_img" $staging/android-rootfs.img
    fi

    content_bytes=`du -sb --apparent-size $staging | cut -f1`
    content_mb=`expr $content_bytes / 1024 / 1024`
    size_mb=`expr $content_mb + ${USERDATA_IMAGE_SLACK}`

    rm -f $target
    features="^has_journal"
    if [ -n "${USERDATA_IMAGE_FEATURES}" ] ; then
        features="$features,${USERDATA_IMAGE_FEATURES}"
    fi
    mke2fs -q -t ext4 -L userdata -b 4096 -O "$features" \
        -d $staging $target "$size_mb"M

    # mke2fs -d keeps the build user's ownership; root is the only user the
    # initramfs runs as.
    debugfs -w -R "sif /rootfs.img uid 0" $target > /dev/null 2>&1
    debugfs -w -R "sif /rootfs.img gid 0" $target > /dev/null 2>&1
    if [ -e $staging/android-rootfs.img ] ; then
        debugfs -w -R "sif /android-rootfs.img uid 0" $target > /dev/null 2>&1
        debugfs -w -R "sif /android-rootfs.img gid 0" $target > /dev/null 2>&1
    fi

    rm -rf $staging
}
