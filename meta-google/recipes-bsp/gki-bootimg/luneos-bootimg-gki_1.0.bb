SUMMARY = "LuneOS boot images for a Tensor GKI device"
DESCRIPTION = "Packs the prebuilt Android Common Kernel and this machine's \
Halium initramfs into the boot images the device's bootloader wants. The \
kernel is not built here - see gki_bootimg.bbclass for why - so this recipe \
only assembles and deploys."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

COMPATIBLE_MACHINE = "^(bluejay|panther)$"

# nopackages deletes do_package and friends. PACKAGES = "" alone is not
# enough: do_package still runs, finds no packages-split directory, and
# buildhistory_list_pkg_files fails the task on the missing path.
inherit gki_bootimg nopackages

# Nothing is compiled, nothing is packaged: the output is images in
# DEPLOY_DIR_IMAGE, exactly like the kernel recipes' .fastboot images.
INHIBIT_DEFAULT_DEPS = "1"

# The images differ per device (different init_boot layout, different kernel),
# so this recipe cannot be shared between machines through sstate.
PACKAGE_ARCH = "${MACHINE_ARCH}"

# No sources: UNPACKDIR is the empty directory bitbake makes for them.
S = "${UNPACKDIR}"
B = "${WORKDIR}/build"

do_configure[noexec] = "1"
do_compile[noexec] = "1"
do_install[noexec] = "1"
