SUMMARY = "Minimal Phone MP01 kernel - MediaTek mgk android12-5.10 + the LuneOS fragment"
DESCRIPTION = "\
The MP01's kernel, built the same way the Q25's is - MediaTek's mgk flavour of \
android12-5.10, not the Android Common Kernel - with one difference that has to \
be stated plainly because it is the recipe's single biggest assumption. \
\
The Minimal Company has published no GPL kernel source for this device. What we \
have instead is the *stock kernel config*, which their factory image ships loose \
as merged/.config, and the observation that the MP01 and the Zinwa Q25 are the \
same SoC (MT6789) running the same MediaTek mgk 5.10 kernel: the two stock \
configs differ by 85 option names out of 2,665, and every one of those 85 is a \
board-level driver (panel, keyboard, touch, charger, fingerprint) rather than \
anything structural. So this recipe builds the *xelex* MT6789 tree - the Q25's \
source, which is published - against the *MP01's* stock config. \
\
That substitution is legitimate only if the exported-symbol CRCs come out \
matching, and that is a measurement, not an argument. The gate is \
/home/herrie/webos/LuneOS/MP01/check-kmi.sh against the MP01's own stock module \
set. Run it with LUNEOS_KERNEL_FRAGMENT = \"0\" first: that separates \"the tree \
is wrong for this device\" from \"my delta broke it\". If the baseline does not \
come back clean, no fragment tuning will save it and the answer is to get the \
real source from Minimal. \
\
Why it matters more here than on the Q25: on this device the *display* is a \
stock vendor module (panel-z10-eink-i2c.ko, an I2C-attached Pango FPGA driving \
the E Ink panel) rather than anything built into vmlinux. A CRC regression is a \
phone that boots to a blank screen with no way to see why. \
\
Gate, every time, before flashing: \
  ./check-kmi.sh   (in /home/herrie/webos/LuneOS/MP01)  -> 0 would fail \
"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

# Same Clang the stock kernel was built with. The MP01's own stock config names
# it outright:
#   CONFIG_CC_VERSION_TEXT="Android (7284624, based on r416183b) clang version
#                           12.0.5 (...)"
# which is the same string the Q25's stock kernel carries, so the toolchain
# fetched by ../zinwa/fetch-gki-toolchain.sh serves both. Set GKI_CLANG_DIR:mp01
# in local.conf.
GKI_CLANG_VERSION ?= "r416183b"
GKI_CLANG_DIR ?= ""
GKI_CLANG_URI ?= ""

# The MP01's own tree, published by Minimal on 15 Sep 2026 (genuinely
# 5.10.233, so the vermagic match is real), carried on the shr kernel repo
# like every other LuneOS kernel: branch mp01/5.10.233/lune = Minimal's
# 6ca3cc4 "initial publish" plus LuneOS's commits on top (the GKI task_struct
# padding, the rt5133 error print and the mediatek DRM plane properties that
# used to be .patch files here). Fixes go as commits on that branch and a
# SRCREV bump, never as patches in this directory.
#
# It also carries what the Q25's xelex tree could not: the E Ink panel
# (panel-z10-eink-vdo, panel-z10-eink-i2c) and the MediaTek v2 display driver
# this device actually runs.
SRC_URI = "\
    git://github.com/shr-distribution/linux.git;protocol=https;branch=mp01/5.10.233/lune;name=kernel \
    ${@(d.getVar('GKI_CLANG_URI') + ';name=clang;subdir=clang') if d.getVar('GKI_CLANG_URI') else ''} \
    file://mp01-stock.config;subdir=frag \
    file://vermagic.cfg;subdir=frag \
    file://luneos.cfg;subdir=frag \
"
SRCREV_kernel = "7fb2662e0ded8c6c65ff110250f4cccb25d8620b"
SRCREV_FORMAT = "kernel"

PV = "5.10.233+git"
B = "${WORKDIR}/build"

DEPENDS = "elfutils-native openssl-native bc-native bison-native flex-native lz4-native python3-native"

inherit deploy

COMPATIBLE_MACHINE = "^(mp01)$"
PACKAGE_ARCH = "${MACHINE_ARCH}"
INHIBIT_DEFAULT_DEPS = "1"
EXCLUDE_FROM_WORLD = "1"

# Unlike the Q25, there is no config *chain* to name here. The Q25's
# BoardConfig.mk gives TARGET_KERNEL_CONFIG as four fragments that all live in
# its own tree; the MP01's board fragment (a z10_*.config) is not published and
# is not in the xelex tree.
#
# What we do have is better for the purpose anyway: the device's fully resolved
# stock .config, shipped loose in the factory image. Using it verbatim as the
# base is a more faithful reproduction of the CRC baseline than reconstructing a
# chain would be - it is literally the config the stock modules were built
# against.
#
# olddefconfig then resolves the mismatch between the two trees: symbols the
# MP01 has and xelex does not (DRM_PANEL_Z10_EINK_VDO, Z10_EINK_IIC_INTF,
# KEYBOARD_AW9523B, TOUCHSCREEN_FTS, FINGERPRINT_FOCALTECH, NXP_NFC_I2C,
# PWM_WARMLIGHT_SUPPORT, CPS_WLS_CHARGER, CHARGER_ETA6947, ...) simply drop out.
# That is harmless and in fact required: every one of them is =m in stock, so we
# neither can nor need to build them - the device keeps the vendor's own .ko.
LUNEOS_KERNEL_FRAGMENT ?= "1"
KERNEL_LTO_THIN ?= "0"

python do_check_toolchain() {
    clang_dir = d.getVar("GKI_CLANG_DIR")
    if not clang_dir and not d.getVar("GKI_CLANG_URI"):
        bb.fatal("Neither GKI_CLANG_DIR nor GKI_CLANG_URI is set. This kernel "
                 "must be built with Google's Clang %s - see the comment in %s."
                 % (d.getVar("GKI_CLANG_VERSION"), d.getVar("FILE")))
    if clang_dir and not os.path.exists(os.path.join(clang_dir, "bin", "clang")):
        bb.fatal("GKI_CLANG_DIR %s has no bin/clang" % clang_dir)
}
addtask check_toolchain before do_configure

def gki_clang_bin(d):
    p = d.getVar("GKI_CLANG_DIR")
    if p:
        bins = [os.path.join(p, "bin")]
    else:
        bins = [os.path.join(d.getVar("UNPACKDIR"), "clang",
                             "clang-" + d.getVar("GKI_CLANG_VERSION"), "bin")]
    tools = d.getVar("GKI_BUILD_TOOLS_DIR")
    if tools:
        bins.append(os.path.join(tools, "bin"))
    return ":".join(bins)

# OE exports its own cross toolchain and flags into every task; none of it
# applies here and some of it is fatal. kernel.bbclass unsets the same set.
gki_clear_oe_env() {
    unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS
    unset CC CXX CPP LD AR NM STRIP OBJCOPY OBJDUMP RANLIB READELF
    unset HOSTCC HOSTCXX BUILD_CC BUILD_CXX MACHINE
}

# The four 5.10 kbuild properties, all of them documented at length in
# meta-zinwa's linux-zinwa-q25_git.bb. They are properties of 5.10's kbuild
# rather than of either device, so they recur on any android12-5.10 port:
# CROSS_COMPILE (5.10 derives clang's --target from it and LLVM=1 alone does
# not), LLVM_IAS=1 (5.10 defaults to -fno-integrated-as), CROSS_COMPILE_COMPAT
# (CONFIG_COMPAT_VDSO=y in the MP01's stock config too), and PYTHON=python3
# (link-vmlinux.sh calls ${PYTHON} scripts/jobserver-exec, and 5.10 still
# spells PYTHON as "python").
KERNEL_MAKE = "make -C ${S} O=${B} LLVM=1 LLVM_IAS=1 ARCH=arm64 \
    CROSS_COMPILE=aarch64-linux-gnu- CROSS_COMPILE_COMPAT=arm-linux-gnueabi- \
    PYTHON=python3"

# The sublevel half of the vermagic match; the rest is in files/vermagic.cfg,
# which explains why this is needed at all. Idempotent, and applied before
# configure so include/config/kernel.release picks it up.
do_configure:prepend() {
    # No SUBLEVEL rewrite any more: this tree is genuinely 5.10.233, so the
    # sublevel half of the vermagic match comes for free. What is still needed is
    # to suppress the "+" kbuild would otherwise append.
    #
    # scripts/setlocalversion ends with
    #
    #     if test "${LOCALVERSION+set}" != "set"; then
    #             scm=$(scm_version --short)
    #             res="$res${scm:++}"
    #     fi
    #
    # i.e. a plus sign whenever the tree is not sitting on a clean annotated
    # tag - which the sed above guarantees, since it dirties the checkout. That
    # single character is enough to miss the vermagic match
    # ("...gdeb4d30d3489+" != "...gdeb4d30d3489") and none of the stock modules
    # would load. An empty .scmversion short-circuits scm_version() to the empty
    # string, so ${scm:++} expands to nothing.
    : > ${S}/.scmversion
}

do_configure() {
    gki_clear_oe_env
    export PATH="${@gki_clang_bin(d)}:$PATH"
    mkdir -p ${B}
    # The device's own stock config, verbatim, as the baseline.
    install -m 0644 ${UNPACKDIR}/frag/mp01-stock.config ${B}/.config
    ${KERNEL_MAKE} olddefconfig
    # tree-fixup.cfg is gone with the xelex tree it existed for: it turned off
    # the symbols whose source directories that tree did not have. This one is
    # the device's own, so the stock config and the tree agree and only the
    # vermagic fragment is left. Verified by building this tree with exactly
    # mp01-stock.config + vermagic.cfg and loading the result on the device.
    ${S}/scripts/kconfig/merge_config.sh -m -O ${B} \
        ${B}/.config ${UNPACKDIR}/frag/vermagic.cfg
    ${KERNEL_MAKE} olddefconfig
    # Optional: trade the stock full-LTO for ThinLTO. See conf/mp01-thinlto.conf
    # for the measurement that motivates it and why it should be KMI-neutral.
    if [ "${KERNEL_LTO_THIN}" = "1" ]; then
        printf '%s\n' '# CONFIG_LTO_CLANG_FULL is not set' 'CONFIG_LTO_CLANG_THIN=y' \
            > ${B}/thinlto.cfg
        ${S}/scripts/kconfig/merge_config.sh -m -O ${B} ${B}/.config ${B}/thinlto.cfg
        ${KERNEL_MAKE} olddefconfig
    fi
    if [ "${LUNEOS_KERNEL_FRAGMENT}" = "1" ]; then
        ${S}/scripts/kconfig/merge_config.sh -m -O ${B} \
            ${B}/.config ${UNPACKDIR}/frag/luneos.cfg
        ${KERNEL_MAKE} olddefconfig
    fi
}

do_compile() {
    gki_clear_oe_env
    export PATH="${@gki_clang_bin(d)}:$PATH"
    ${KERNEL_MAKE} -j${@oe.utils.cpu_count()} \
        HOSTCFLAGS="-I${STAGING_INCDIR_NATIVE} -Wno-incompatible-pointer-types-discards-qualifiers" \
        HOSTLDFLAGS="-L${STAGING_LIBDIR_NATIVE} -Wl,-rpath,${STAGING_LIBDIR_NATIVE}" \
        Image.gz modules
}

# Ship the patched display driver in the initramfs, where it REPLACES the
# vendor's copy.
#
# The bootloader concatenates the vendor_boot ramdisk and ours and later archives
# win, so a file at lib/modules/mediatek-drm.ko in this image substitutes for the
# vendor's, and init.sh's load_kernel_modules() then inserts ours in the correct
# dependency order. /override/modules cannot be used for this: it runs *before*
# the vendor set, and mediatek-drm imports 487 symbols from vendor modules that
# do not exist yet, so insmod fails and the vendor copy loads regardless.
#
# mp01.conf adds this package to ANDROID_EXTRA_INITRAMFS_IMAGE_INSTALL.
do_install() {
    install -d ${D}${nonarch_base_libdir}/modules
    install -m 0644 ${B}/drivers/gpu/drm/mediatek/mediatek_v2/mediatek-drm.ko \
        ${D}${nonarch_base_libdir}/modules/mediatek-drm.ko
    # 37MB unstripped against the vendor's 5.8MB, and an unstripped .ko also
    # trips the debug-files QA check.
    ${@gki_clang_bin(d).split(':')[0]}/llvm-strip --strip-debug \
        ${D}${nonarch_base_libdir}/modules/mediatek-drm.ko
}

FILES:${PN} = "${nonarch_base_libdir}/modules/mediatek-drm.ko"

# We strip it ourselves with the toolchain that built it; OE's own strip and
# debug-split do not understand a module built outside its cross environment.
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
# A kernel module legitimately records the build path it was compiled in.
INSANE_SKIP:${PN} += "buildpaths"

do_deploy() {
    install -d ${DEPLOYDIR}
    install -m 0644 ${B}/arch/arm64/boot/Image.gz ${DEPLOYDIR}/Image.gz
    # The input to check-kmi.sh, and the only way to tell without a device
    # whether a config change just stopped the vendor's modules from loading.
    install -m 0644 ${B}/Module.symvers ${DEPLOYDIR}/Module.symvers
    install -m 0644 ${B}/.config ${DEPLOYDIR}/kernel-config
    # What the modules will be checked against. If this does not read
    # 5.10.233-android12-9-gdeb4d30d3489 the stock modules will not insmod.
    install -m 0644 ${B}/include/config/kernel.release ${DEPLOYDIR}/kernel.release

    # The display driver, patched by 0004 to create the standard "alpha" and
    # "pixel blend mode" plane properties. Without those the vendor hwcomposer
    # abandons DRM entirely ("failed to initialize drm resource") and the
    # compositor never starts - see mp01-notes.md.
    #
    # This has to REPLACE the vendor's copy rather than be loaded alongside it.
    # /override/modules cannot do that: those are inserted before the vendor set,
    # and mediatek-drm imports 487 symbols from vendor modules that do not exist
    # yet, so insmod fails and the vendor copy loads anyway. What works is
    # shipping it at lib/modules/mediatek-drm.ko in OUR boot ramdisk: the
    # bootloader concatenates the vendor_boot ramdisk and ours, later archives
    # win, and load_kernel_modules() then loads ours in the right dependency
    # order.
    #
    # Strip it: the unstripped module is 37MB against the vendor's 5.8MB, and an
    # unstripped .ko also trips the buildpaths and debug-files QA checks.
    #
    # Also deployed loose, for check-kmi.sh and for hand-repacking a ramdisk when
    # bisecting. The copy that actually reaches the device comes from do_install
    # via the initramfs - see above.
    install -d ${DEPLOYDIR}/modules
    install -m 0644 ${B}/drivers/gpu/drm/mediatek/mediatek_v2/mediatek-drm.ko \
        ${DEPLOYDIR}/modules/mediatek-drm.ko
    ${@gki_clang_bin(d).split(':')[0]}/llvm-strip --strip-debug \
        ${DEPLOYDIR}/modules/mediatek-drm.ko
}
addtask deploy after do_compile before do_build
