# The A3 2015's BOOT partition is 13 MiB (from the device's own PIT), and
# upstream lk2nd takes the first 512 KiB of it, leaving 12.50 MiB for the whole
# Android boot image. The kernel alone is 9.96 MiB compressed, so the ramdisk
# has to fit in what is left.
#
# As built for every other machine this initramfs is 6.31 MiB compressed, and
# 5.51 MiB of the uncompressed tree is libcrypto.so.3 - pulled in by
# android-tools, whose only contribution here is adbd. Nothing in init.sh uses
# adb: the initramfs already brings up CDC ECM networking on 172.16.42.2 and can
# start telnetd, so adbd is a second, larger way to do what is already there.
#
# bash goes for the same reason. init.sh, init_functions.sh and the mdev helpers
# are all #!/bin/sh, and busybox ash runs them - better, in fact: the
# mdev-partlabel.sh comment records that bash 5.3 cannot source files in sysfs
# while busybox ash can.
#
# Scoped to this machine. Every other machine that uses this image keeps adbd
# and bash, because no other one is squeezed like this.
IMAGE_INSTALL:remove:a3-2015 = "android-tools bash"

# Compress the ramdisk with xz rather than gzip. Trimming android-tools and bash
# above got the boot image from 16.28 MiB to 12.79 MiB, still 292 KiB over the
# 12.50 MiB the BOOT partition leaves once lk2nd has its 512 KiB. On this exact
# tree the compressors measure:
#
#     gzip -9   2936251      xz    2246800      zstd -19  2295341      lz4  3392560
#
# so xz is worth ~690 KiB against gzip - twice what is needed - and the kernel
# already has CONFIG_RD_XZ=y (it has every RD_* decompressor, from pmos.config).
# The ramdisk is decompressed by the kernel, not by lk2nd, so nothing in the
# bootloader path cares which of these it is.
#
# The base recipe pins this with :forcevariable, which outranks a plain machine
# override, so the assignment has to be made at the same strength and gated on
# the machine by hand. linux-samsung-a3-2015_git.bb sets the matching
# INITRAMFS_NAME; the two must agree or do_deploy fails looking for a file that
# was never produced.
IMAGE_FSTYPES:forcevariable = "${@'cpio.xz' if d.getVar('MACHINE') == 'a3-2015' else 'cpio.gz'}"
