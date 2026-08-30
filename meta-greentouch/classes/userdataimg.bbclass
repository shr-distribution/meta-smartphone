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

    rootfs_bytes=`stat -c%s $staging/rootfs.img`
    rootfs_mb=`expr $rootfs_bytes / 1024 / 1024`
    size_mb=`expr $rootfs_mb + ${USERDATA_IMAGE_SLACK}`

    rm -f $target
    mke2fs -q -t ext4 -L userdata -b 4096 -O ^has_journal \
        -d $staging $target "$size_mb"M

    # mke2fs -d keeps the build user's ownership; root is the only user the
    # initramfs runs as.
    debugfs -w -R "sif /rootfs.img uid 0" $target > /dev/null 2>&1
    debugfs -w -R "sif /rootfs.img gid 0" $target > /dev/null 2>&1

    rm -rf $staging
}
