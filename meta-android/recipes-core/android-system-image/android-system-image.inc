DESCRIPTION = "Tiny Android system to leverage android hardware drivers through libhybris"
LICENSE = "Apache-2.0 & Properietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

# don't run package_do_shlibs on our packages as this will add a RDEPENDS from ${PN} to
# ${PN}-dbg
EXCLUDE_FROM_SHLIBS = "1"

PACKAGE_ARCH = "${MACHINE_ARCH}"

RPROVIDES_${PN} += "virtual/android-system-image"

INC_PR = "r4"

do_install() {
    cp -rpv ${WORKDIR}/system ${D}
    ln -sf /system/vendor ${D}/vendor

    install -d ${D}/system/symbols
    cp -rv ${WORKDIR}/symbols ${D}/system

    # Copy android ramdisk so we can use it
    install -d ${D}/boot
    # Support packages with both ramdisk names
    if [ -e ${WORKDIR}/android-ramdisk.img ] ; then
        install -m 0644 ${WORKDIR}/android-ramdisk.img ${D}/boot/android-ramdisk.img
    else
        install -m 0644 ${WORKDIR}/ramdisk.img ${D}/boot/android-ramdisk.img
    fi

    if [ -e ${WORKDIR}/filesystem_config.txt ] ; then
        # Fixup permissions of the android binaries in /system
        while read line
        do
            file=`echo $line | cut -d' ' -f 1`
            user=`echo $line | cut -d' ' -f 2`
            group=`echo $line | cut -d' ' -f 3`
            mode=`echo $line | cut -d' ' -f 4`
            chown -h $user:$group ${D}/$file
            # Avoid changing symlinks
            if [ ! -h ${D}/$file ] ; then
                chmod $mode ${D}/$file
            fi
        done < "${WORKDIR}/filesystem_config.txt"
    else
        bbwarn "image doesn't contain a filesystem_config.txt; may cause runtime errors with android system"
    fi
}

# 400+ elf binaries without GNU_HASH and we cannot fix it
INSANE_SKIP_${PN} = "ldflags"
INSANE_SKIP_${PN}-dbg = "ldflags"
# Already stripped and we cannot fix it, this will hide a lot of warnings about files already stripped
INHIBIT_PACKAGE_STRIP = "1"
PACKAGES = "${PN}-dbg ${PN}"
FILES_${PN}-dbg = "/system/symbols"
FILES_${PN} = "/system /vendor /android /default.prop /boot/android-ramdisk.img ${sysconfdir}"
