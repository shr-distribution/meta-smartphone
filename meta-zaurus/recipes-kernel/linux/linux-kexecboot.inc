# We set INC_PR here, since a change in the kexecboot recipe will need to get picked up by *all* the kernels:
INC_PR = "r43"

inherit kernel siteinfo

LICENSE = "GPLv2"

LIC_FILES_CHKSUM = "file://COPYING;md5=d7810fab7487fb0aad327b76f1be7cd7"

DEPENDS += "xz-native"

# Kernel bootlogo is distro-specific (default is OE logo).
# Logo resolution (qvga, vga, ...) is machine-specific.
LOGO_SIZE ?= "."
SRC_URI = "file://${LOGO_SIZE}/logo_linux_clut224.ppm.bz2"

# Set the verbosity of kernel messages during runtime
CMDLINE_DEBUG ?= '${@base_contains("IMAGE_FEATURES", "debug-tweaks", "debug", "loglevel=3", d)}'

# Note how we set master console on serial to protect bootlogo.
CMDLINE_c7x0 = "console=tty1 console=ttyS0,115200n8 ${CMDLINE_DEBUG}"
CMDLINE_tosa = "console=tty1 console=ttyS0,115200n8 ${CMDLINE_DEBUG}"
CMDLINE_poodle = "console=tty1 console=ttyS0,115200n8 fbcon=rotate:1 ${CMDLINE_DEBUG}"
CMDLINE_akita = "console=tty1 console=ttyS0,115200n8 fbcon=rotate:1 ${CMDLINE_DEBUG}"
CMDLINE_collie = "console=tty1 console=ttySA0,115200n8 fbcon=rotate:1 mem=64M ${CMDLINE_DEBUG}"
CMDLINE_spitz = "console=tty1 console=ttyS0,115200n8 fbcon=rotate:1 ${CMDLINE_DEBUG}"

PACKAGES = ""
PROVIDES = ""

# Zaurus machines need kernel size-check
KERNEL_IMAGE_MAXSIZE_akita = "1294336"
KERNEL_IMAGE_MAXSIZE_c7x0 = "1294336"
KERNEL_IMAGE_MAXSIZE_collie = "1048576"
KERNEL_IMAGE_MAXSIZE_poodle = "1294336"
KERNEL_IMAGE_MAXSIZE_tosa = "1294336"
KERNEL_IMAGE_MAXSIZE_spitz = "1294336"

KERNEL_IMAGE_BASE_NAME = "${KERNEL_IMAGETYPE}-kexecboot-${PV}-${PR}-${MACHINE}"
KERNEL_IMAGE_SYMLINK_NAME = "${KERNEL_IMAGETYPE}-kexecboot-${MACHINE}"

LOCALVERSION ?= ""

# we want the smallest size
INITRAMFS_IMAGE = "initramfs-kexecboot-klibc-image"

do_configure_prepend() {

        echo "" > ${S}/.config

        #
        # CONFIG_CMDLINE mangling
        #
        echo "CONFIG_CMDLINE_BOOL=y" >> ${S}/.config
        echo "CONFIG_CMDLINE=\"${CMDLINE}\"" >> ${S}/.config

        # mips and x86
        echo "CONFIG_CMDLINE_OVERRIDE=y" >> ${S}/.config

        # sh only
        echo "CONFIG_CMDLINE_OVERWRITE=y" >> ${S}/.config
        echo "# CONFIG_CMDLINE_EXTEND is not set" >> ${S}/.config

        #
        # endian support
        #
        if [ "${SITEINFO_ENDIANNESS}" = "be" ]; then
                echo "CONFIG_CPU_BIG_ENDIAN=y"          >> ${S}/.config
        fi

        #
        # logo support, if you supply logo_linux_clut224.ppm in SRC_URI, then it is going to be used
        #
        if [ -e ${WORKDIR}/logo_linux_clut224.ppm ]; then
                install -m 0644 ${WORKDIR}/logo_linux_clut224.ppm drivers/video/logo/logo_linux_clut224.ppm
                echo "CONFIG_LOGO=y"                    >> ${S}/.config
                echo "CONFIG_LOGO_LINUX_CLUT224=y"      >> ${S}/.config
        fi

        # When enabling thumb for userspace we also need thumb support in the kernel
        if [ "${ARM_INSTRUCTION_SET}" = "thumb" ] ; then
            sed -i -e /CONFIG_ARM_THUMB/d ${WORKDIR}/defconfig 
            echo "CONFIG_ARM_THUMB=y" >> ${S}/.config
        fi

        sed -e '/CONFIG_CMDLINE=/d' \
            -e '/CONFIG_CMDLINE_BOOL/d' \
            -e '/CONFIG_CMDLINE_OVERRIDE/d' \
            -e '/CONFIG_CMDLINE_OVERWRITE/d' \
            -e '/CONFIG_CMDLINE_EXTEND/d' \
            -e '/CONFIG_CPU_BIG_ENDIAN/d' \
            -e '/CONFIG_LOGO=/d' \
            -e '/CONFIG_LOGO_LINUX_CLUT224=/d' \
            -e '/CONFIG_LOCALVERSION/d' \
            -e '/CONFIG_LOCALVERSION_AUTO/d' \
            < '${WORKDIR}/defconfig' >>'${S}/.config'

        echo 'CONFIG_LOCALVERSION="${LOCALVERSION}"' >>${S}/.config
        echo '# CONFIG_LOCALVERSION_AUTO is not set' >>${S}/.config

        # Force sane defaults for kexec-enabled kernels and keep size small
        sed -i -e /CONFIG_BLK_DEV_INITRD/d \
               -e /CONFIG_KEXEC/d \
               -e /=m/d \
               -e /CONFIG_MODULES/d \
               -e /CONFIG_DEBUG_ERRORS/d \
               -e /CONFIG_DEBUG_BUGVERBOSE/d \
               -e /CONFIG_DEBUG_KERNEL/d \
               ${S}/.config

               echo 'CONFIG_BLK_DEV_INITRD=y' >>${S}/.config
               echo 'CONFIG_KEXEC=y' >>${S}/.config
               echo '# CONFIG_MODULES is not set' >> ${S}/.config

        # Force lzma for arm kernels only
        if [ "${ARCH}" = "arm" ] ; then

            sed -i -e /CONFIG_KERNEL/d \
               -e /CONFIG_INITRAMFS_COMPRESSION/d \
               -e /CONFIG_INITRAMFS_SOURCE/d \
               -e /CONFIG_RD/d \
               ${S}/.config

               echo 'CONFIG_KERNEL_LZMA=y' >>${S}/.config
               echo 'CONFIG_INITRAMFS_COMPRESSION_LZMA=y' >>${S}/.config
               echo 'CONFIG_INITRAMFS_SOURCE="initramfs.cpio.lzma"' >>${S}/.config
               echo 'CONFIG_RD_LZMA=y' >>${S}/.config
        fi

        yes '' | oe_runmake oldconfig
}

do_populate_sysroot() {
        :
}

do_install() {
        :
}

