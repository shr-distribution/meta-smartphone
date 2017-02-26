SECTION = "kernel"
DESCRIPTION = "Prebuilt Linux kernel for the HP touchpad"
LICENSE = "GPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0;md5=801f80980d171dd6425610833a22dbe6"

PROVIDES += "virtual/kernel"

DEPENDS += "u-boot-mkimage-native"

COMPATIBLE_MACHINE = "tenderloin"

PACKAGE_ARCH = "${MACHINE_ARCH}"

PACKAGES_DYNAMIC += "^kernel-module-.*"
PACKAGES_DYNAMIC += "^kernel-image-.*"

# Version coming from our jenkens when building the kernel within the android build
BUILD_VERSION = "20161117-94"

# Python3 doesn't like regexp searching in binary files
# and it cannot open binary modules and decode them as utf-8
# strings, so either this needs to be re-written to search
# in binary or we can use hardcode the version bellow :)
def get_kernelversion_modules(p):
    import re
    if not os.path.isdir(p):
        return None

    for fn in os.listdir(p):
        if os.path.isfile(p+fn):
            try:
                f = open(p+fn, 'r')
            except IOError:
                return None

            l = f.readlines()
            f.close()

            r = re.compile("vermagic=(\S*)")
            for s in l:
                m = r.search(s)
                if m:
                    return m.group(1)
    return None

# This is the version we get from the kernel tree: <kernel version>-<commits since last
# tag>-g<short ref of last commit>. You can get it easily with running `git describe`
# within the repo. In this case the last tag was v3.0.65 but we're already at 3.0.101
# without having all the tags merged.
KERNEL_VERSION = "3.0.101-gc31ea94"
# KERNEL_VERSION = "${@get_kernelversion_modules('${S}/modules/')}"

PV = "3.0.101-${BUILD_VERSION}"

SRC_URI = " \
    http://build.webos-ports.org/cm-wop-11.0/cm-wop-11.0-${BUILD_VERSION}-kernel-parts-tenderloin.tar.bz2 \
"
S = "${WORKDIR}/kernel-parts-${BUILD_VERSION}"
B = "${WORKDIR}/build"

SRC_URI[md5sum] = "67d8dbc04a2412a75af6e941676815dc"
SRC_URI[sha256sum] = "2e973c0710b47634300f7bdb4d03be8cd1b14a951475912ed59a23568518bff2"

INITRAMFS_IMAGE ?= "initramfs-android-image"
do_compile[depends] += "${INITRAMFS_IMAGE}:do_image_complete"

KERNEL_IMAGEDEST = "/boot"
KERNEL_MODULEDEST = "/lib/modules"
KERNEL_IMAGETYPE = "uImage"
KERNEL_SRC_PATH = "/usr/src"
KERNEL_OUTPUT = "uImage"
KERNEL_OUTPUT_DIR = "${B}"

do_configure() {
    :
}

do_compile () {
    if [ -e ${DEPLOY_DIR_IMAGE}/${INITRAMFS_IMAGE}-${MACHINE}.cpio.gz ] ; then
        cp ${DEPLOY_DIR_IMAGE}/${INITRAMFS_IMAGE}-${MACHINE}.cpio.gz ${B}

        # pack initramfs as uboot image
        uboot-mkimage -A arm -O Linux -T ramdisk -n 'HP Touchpad boot initrd' -C none \
            -e 0 -a 0 -d ${B}/${INITRAMFS_IMAGE}-${MACHINE}.cpio.gz ${B}/${INITRAMFS_IMAGE}-${MACHINE}.uImage

        # now pack kernel and initramfs together
        uboot-mkimage  -A arm -O Linux -T multi -n 'HP Touchpad boot' -C none \
            -e 0 -a 0 -d ${S}/uImage:${B}/${INITRAMFS_IMAGE}-${MACHINE}.uImage \
            ${B}/${KERNEL_OUTPUT}
    else
        bberror "Could not find ${INITRAMFS_IMAGE}-${MACHINE}.cpio.gz in ${DEPLOY_DIR_IMAGE}"
    fi
}

do_install() {
    install -d ${D}${KERNEL_MODULEDEST}/${KERNEL_VERSION}
    install -d ${D}${KERNEL_IMAGEDEST}
    
    install -m 0644 ${B}/${KERNEL_OUTPUT}  ${D}${KERNEL_IMAGEDEST}/${KERNEL_OUTPUT}

    for mod in `find ${S}/modules -iname *.ko -type f -exec basename {} \;` ; do
        install -m 0644 ${S}/modules/$mod ${D}${KERNEL_MODULEDEST}/${KERNEL_VERSION}/
    done
}

do_bundle_initramfs() {
    :
}
do_bundle_initramfs[nostamp] = "1"
addtask bundle_initramfs after do_compile

inherit deploy

KERNEL_IMAGE_BASE_NAME ?= "${PKGE}-${PKGV}-${PKGR}-${MACHINE}-${DATETIME}"
# Don't include the DATETIME variable in the sstate package signatures
KERNEL_IMAGE_BASE_NAME[vardepsexclude] = "DATETIME"
KERNEL_IMAGE_SYMLINK_NAME ?= "${MACHINE}"
MODULE_IMAGE_BASE_NAME ?= "modules-${PKGE}-${PKGV}-${PKGR}-${MACHINE}-${DATETIME}"
MODULE_IMAGE_BASE_NAME[vardepsexclude] = "DATETIME"
MODULE_TARBALL_BASE_NAME ?= "${MODULE_IMAGE_BASE_NAME}.tgz"
# Don't include the DATETIME variable in the sstate package signatures
MODULE_TARBALL_SYMLINK_NAME ?= "modules-${MACHINE}.tgz"
MODULE_TARBALL_DEPLOY ?= "1"
KERNEL_IMAGETYPES ?= "uImage"

do_deploy() {
    for type in ${KERNEL_IMAGETYPES} ; do
        base_name=${type}-${KERNEL_IMAGE_BASE_NAME}
        install -m 0644 ${KERNEL_OUTPUT_DIR}/${type} ${DEPLOYDIR}/${base_name}.bin
    done
    if [ ${MODULE_TARBALL_DEPLOY} = "1" ] && (grep -q -i -e '^CONFIG_MODULES=y$' .config); then
        mkdir -p ${D}/lib
        tar -cvzf ${DEPLOYDIR}/${MODULE_TARBALL_BASE_NAME} -C ${D} lib
        ln -sf ${MODULE_TARBALL_BASE_NAME} ${DEPLOYDIR}/${MODULE_TARBALL_SYMLINK_NAME}
    fi

    for type in ${KERNEL_IMAGETYPES} ; do
        base_name=${type}-${KERNEL_IMAGE_BASE_NAME}
        symlink_name=${type}-${KERNEL_IMAGE_SYMLINK_NAME}
        ln -sf ${base_name}.bin ${DEPLOYDIR}/${symlink_name}.bin
        ln -sf ${base_name}.bin ${DEPLOYDIR}/${type}
    done

    cd ${B}
    # Update deploy directory
    for type in ${KERNEL_IMAGETYPES} ; do
        if [ -e "${KERNEL_OUTPUT_DIR}/${type}.initramfs" ]; then
            echo "Copying deploy ${type} kernel-initramfs image and setting up links..."
            initramfs_base_name=${type}-${INITRAMFS_BASE_NAME}
            initramfs_symlink_name=${type}-initramfs-${MACHINE}
            install -m 0644 ${KERNEL_OUTPUT_DIR}/${type}.initramfs ${DEPLOYDIR}/${initramfs_base_name}.bin
            ln -sf ${initramfs_base_name}.bin ${DEPLOYDIR}/${initramfs_symlink_name}.bin
        fi
    done
}

do_deploy[cleandirs] = "${DEPLOYDIR}"
do_deploy[dirs] = "${DEPLOYDIR} ${B}"
do_deploy[prefuncs] += "package_get_auto_pr"

addtask deploy after do_populate_sysroot do_packagedata

PACKAGES = "kernel kernel-base kernel-image kernel-modules"

FILES_kernel-base = "/lib/modules/${KERNEL_VERSION}/modules.order /lib/modules/${KERNEL_VERSION}/modules.builtin"
FILES_kernel-image = "/boot/${KERNEL_IMAGETYPE}*"
FILES_kernel-modules = "/lib/modules/${KERNEL_VERSION} ${sysconfdir}/modules-load.d/*"

ALLOW_EMPTY_kernel = "1"

emit_depmod_pkgdata() {
    # Stash data for depmod
    install -d ${PKGDESTWORK}/kernel-depmod/
    echo "${KERNEL_VERSION}" > ${PKGDESTWORK}/kernel-depmod/kernel-abiversion
    # cp System.map ${PKGDESTWORK}/kernel-depmod/System.map-${KERNEL_VERSION}
}

PACKAGEFUNCS += "emit_depmod_pkgdata"

RDEPENDS_kernel = "kernel-base"
# Allow machines to override this dependency if kernel image files are
# not wanted in images as standard
RDEPENDS_kernel-base ?= "kernel-image"
PKG_kernel-image = "kernel-image-${@legitimize_package_name('${KERNEL_VERSION}')}"
PKG_kernel-base = "kernel-${@legitimize_package_name('${KERNEL_VERSION}')}"
RPROVIDES_kernel-base += "kernel-${KERNEL_VERSION}"

pkg_postinst_kernel-modules_append() {
    if [ ! -e "$D/lib/modules/${KERNEL_VERSION}" ]; then
        mkdir -p $D/lib/modules/${KERNEL_VERSION}
    fi
    if [ -n "$D" ]; then
        depmodwrapper -a -b $D ${KERNEL_VERSION}
    else
        depmod -a ${KERNEL_VERSION}
    fi
}
