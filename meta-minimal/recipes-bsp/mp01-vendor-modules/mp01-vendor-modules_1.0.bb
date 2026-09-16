SUMMARY = "MP01 stock vendor kernel modules, for the initramfs"
DESCRIPTION = "\
The 180 kernel modules out of the MP01's stock vendor_boot ramdisk, installed \
into the LuneOS initramfs at /lib/modules alongside the vendor's own \
modules.load. \
\
Why this exists, since on paper it should not have to: on a GKI device the \
bootloader concatenates vendor_boot's ramdisk with the one in boot.img, so \
those modules ought to already be at /lib/modules by the time our init runs, \
and halium's load_vendor_modules() would find them there. That is what AOSP \
specifies and what stock Android on this phone demonstrably relies on. \
\
It is also unverified on this device, and unverifiable without a working \
console - which is precisely what we do not have, because the USB controller \
is one of the modules in question. Shipping our own copy removes the \
assumption entirely: /lib/modules is populated whatever the bootloader does \
with the vendor ramdisk. If the concatenation does happen, these are the same \
files landing in the same place and nothing changes. \
\
They cost 5.5 MB compressed in a 64 MiB boot partition (kernel 16 MB + \
initramfs 7.6 MB + these), so the budget is not close. \
\
Not redistributable, and not checked into the layer: extracted from Minimal's \
own factory image by /home/herrie/webos/LuneOS/MP01/extract-vendor-modules.sh, \
which is where the tarball below comes from. Re-run it after a firmware \
update - these have to stay in step with the vendor_dlkm and vendor partitions \
the phone keeps. \
"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

# Absolute path out of the port directory rather than a file in the layer, the
# same arrangement GKI_CLANG_DIR uses for the toolchain: this is a 25 MB set of
# vendor binaries pulled out of a factory image, and it has no business in git.
MP01_VENDOR_MODULES_TARBALL ?= "/home/herrie/webos/LuneOS/MP01/firmware/mp01-vendor-modules.tar.gz"
SRC_URI = "file://${MP01_VENDOR_MODULES_TARBALL}"

COMPATIBLE_MACHINE = "^(mp01)$"
PACKAGE_ARCH = "${MACHINE_ARCH}"

# Prebuilt aarch64 .ko from a vendor kernel: nothing here is ours to strip,
# split, or check for the usual QA properties.
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_SYSROOT_STRIP = "1"
EXCLUDE_FROM_SHLIBS = "1"
INSANE_SKIP:${PN} += "already-stripped arch ldflags staticdev"

S = "${UNPACKDIR}"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    install -d ${D}${nonarch_base_libdir}/modules
    # NOT cp -a: that preserves the uid of whoever extracted the factory image
    # (1000 here), and do_package then dies with
    #   KeyError: 'getpwuid(): uid not found: 1000'
    # because that uid does not exist in the target's passwd. install(1) writes
    # them as root, which is what a module in /lib/modules should be anyway.
    for f in ${S}/modules/*; do
        install -m 0644 "$f" ${D}${nonarch_base_libdir}/modules/
    done
}

FILES:${PN} = "${nonarch_base_libdir}/modules"
