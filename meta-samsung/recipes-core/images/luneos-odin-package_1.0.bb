SUMMARY = "Odin-flashable LuneOS package for the Samsung Galaxy Tab A7 Lite Wi-Fi (SM-T220) and Tab Pro 10.1 Wi-Fi (SM-T520)"
DESCRIPTION = "An AP-style .tar.md5 holding the boot image, a vbmeta with verification \
disabled and a userdata image carrying the rootfs, in the format Samsung's own \
firmware uses and this device's lk Download mode accepts."
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

COMPATIBLE_MACHINE = "^(sm-t220|sm-t520)$"

INHIBIT_DEFAULT_DEPS = "1"
DEPENDS = "android-tools-native lz4-native coreutils-native"
PACKAGE_ARCH = "${MACHINE_ARCH}"

IMAGE_BASENAME = "luneos"
IMAGE_NAME = "${IMAGE_BASENAME}-image"

PKG_BASENAME = "${IMAGE_BASENAME}-odin-package-${MACHINE}"
PKG_NAME = "${PKG_BASENAME}${IMAGE_VERSION_SUFFIX}"

inherit kernel-artifact-names deploy nopackages

# Why Odin and not "fastboot flash", as the other MediaTek machines here do.
# Strings in the stock lk (BL_T220XXSAEYE4, lk-verified.img) show the
# bootloader's fastboot handler has getvar, boot, oem getsecure_status and
# reboot-fastboot only, answers "not allowed in locked state", and has no
# flash/erase/download commands; flashing goes through Samsung's own
# odin_handler in Download mode. That is a reading of a binary, not a test on
# the tablet.
#
# The package copies the format of Samsung's own AP file, read from
# T220XXSAEYE4:
#   - each image is an lz4 frame, 4 MB blocks, content size recorded
#     (boot.img.lz4 starts 04 22 4d 18 6c 60 ... and lists as "LZ4Frame B6I");
#   - boot is a raw image, userdata an Android sparse image (3a ff 26 ed);
#   - the tar is followed by "<md5>  <name>.tar\n", which is what .tar.md5 means.
#
# The userdata payload is an ext4 holding the rootfs as rootfs.img, which is
# where the halium initramfs looks (see classes/userdataimg.bbclass).

do_deploy() {
    USERDATA="${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}-${MACHINE}${IMAGE_NAME_SUFFIX}.userdataimg"
    BOOTIMG="${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGETYPE}.fastboot"

    if [ ! -e $USERDATA ] ; then
        bbfatal "No userdata image at $USERDATA. The machine has to put userdataimg in IMAGE_FSTYPES."
    fi
    if [ ! -e $BOOTIMG ] ; then
        bbfatal "Required boot image $BOOTIMG doesn't exist."
    fi

    rm -rf ${WORKDIR}/build
    mkdir -p ${WORKDIR}/build
    cd ${WORKDIR}/build

    cp $BOOTIMG boot.img

    if [ "${MACHINE}" = "sm-t520" ] ; then
        # The 2014 tablet is not the T220's Android 12 generation: its AP file
        # (T520XXUAOI2, read from the factory zip) is a plain ustar of raw
        # images - boot.img, recovery.img, system.img, sboot.bin - with no lz4
        # and no vbmeta (there is no AVB). Samsung's own boot.img ends in the
        # 16 bytes "SEANDROIDENFORCE", which LineageOS's mkbootimg.mk
        # (hardware/samsung/mkbootimg.mk) also appends; sboot checks for it on
        # a Knox device, so it is added here and not by the kernel recipe.
        #
        # The boot partition is 8 MiB (BOARD_BOOTIMAGE_PARTITION_SIZE).
        printf 'SEANDROIDENFORCE' >> boot.img
        if [ `stat -c%s boot.img` -gt 8388608 ] ; then
            bbfatal "boot.img is `stat -c%s boot.img` bytes, over the 8388608 byte BOOT partition. Shrink the initramfs (see sm-t520-STATUS.md)."
        fi

        img2simg $USERDATA userdata.img 4096

        # TODO(verify): that the PIT names USERDATA's file userdata.img; a
        # mismatch makes Odin refuse the file instead of flashing the wrong
        # partition.
        TAR=${PKG_NAME}.tar
        tar -H ustar --owner=0 --group=0 --numeric-owner -cf $TAR boot.img userdata.img
    else

    # A vbmeta that tells lk not to verify. This is what
    # "avbtool make_vbmeta_image --flags 2" writes: an AvbVBMetaImageHeader
    # (256 bytes, big-endian) with no key, no signature and no descriptors, and
    # AVB_VBMETA_IMAGE_FLAGS_VERIFICATION_DISABLED (2) at offset 120, padded to
    # 4096. The field layout was checked by parsing Samsung's own vbmeta.img
    # with it (flags at 120, release string at 128, 256 bytes in all).
    #
    # Only meaningful with the bootloader unlocked; a locked one rejects it.
    python3 - <<'PYEOF'
import struct
fmt = '>4sIIQQI10QQII48s80x'
hdr = struct.pack(fmt, b'AVB0', 1, 0, 0, 0, 0,
                  0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                  0, 2, 0, b'avbtool 1.2.0')
assert len(hdr) == 256
open('vbmeta.img', 'wb').write(hdr.ljust(4096, b'\0'))
PYEOF

    # Samsung's userdata is sparse; a raw one this size would be slow to send.
    img2simg $USERDATA userdata.img 4096

    for f in boot.img vbmeta.img userdata.img ; do
        lz4 -B6 --content-size -f $f $f.lz4
        rm -f $f
    done

    TAR=${PKG_NAME}.tar
    tar -H ustar --owner=0 --group=0 --numeric-owner -cf $TAR \
        boot.img.lz4 vbmeta.img.lz4 userdata.img.lz4

    # ustar cannot hold a member over 8 GiB; Odin would not take it either.
    for f in boot.img.lz4 vbmeta.img.lz4 userdata.img.lz4 ; do
        if [ `stat -c%s $f` -ge 8589934592 ] ; then
            bbfatal "$f is 8 GiB or more, which the tar format cannot hold."
        fi
    done
    fi

    # The md5 trailer: "<md5>  <name>.tar\n" appended to the tar, which is what
    # makes it a .tar.md5. The hash covers every byte before that line.
    #
    # Samsung's own files put the line straight after the tar's NUL padding. That
    # is what their Odin reads, but the Linux odin4 used here (7.3.0-a46321b)
    # finds the line by searching backwards for the previous newline instead. On
    # such a file it hashes up to whatever stray 0x0a byte sits in the binary
    # data and reports "MD5 verification failed" (measured: it hashed 1556979129
    # bytes of a 1556981760-byte tar and expected the value computed over all of
    # them).
    #
    # A newline after the padding and before the line satisfies both readings:
    # the line is then the last line, and "everything before it" is the same
    # range whichever way a tool finds it. The newline is hashed too. Tar readers
    # stop at the end-of-archive zero blocks, so it is ignored there.
    printf '\n' >> $TAR
    echo "`md5sum $TAR | cut -d' ' -f1`  $TAR" >> $TAR
    mv $TAR ${DEPLOYDIR}/$TAR.md5
}

do_deploy[sstate-outputdirs] = "${DEPLOY_DIR_IMAGE}"
do_deploy[depends] += "${IMAGE_NAME}:do_image_complete virtual/kernel:do_deploy"
addtask deploy after do_install before do_package
