SUMMARY = "Google Pixel 4a 5G (bramble) kernel - LineageOS redbull msm-4.19, pinned to the stock revision"
DESCRIPTION = "\
bramble and redfin (Pixel 5) share one device tree and one kernel, which \
LineageOS calls redbull; device/google/bramble is a thin shim over it. \
\
Built with Google's Clang, not with OE's GCC, and that is forced rather than \
chosen. redbull_defconfig sets CONFIG_LTO_CLANG, CONFIG_CFI_CLANG and \
CONFIG_SHADOW_CALL_STACK - all three are clang-only - and the ~329 stock \
vendor modules this port keeps (see the note at the end of this file) were \
compiled with them. A GCC build cannot produce them at all: it stops at \
\
    cc1: error: '-fsanitize=shadow-call-stack' requires '-ffixed-x18' \
\
and turning the three off instead would produce a kernel that the stock CFI \
and SCS modules cannot safely be loaded into - x18 is the shadow stack \
pointer their code assumes the kernel maintains, and their cross-module calls \
go through CFI checks the kernel would no longer implement. vermagic does not \
catch that (MODULE_ARCH_VERMAGIC here is just 'aarch64'), so the failure mode \
is not a refused insmod but a kernel that corrupts itself later. \
\
So this recipe follows meta-zinwa's linux-zinwa-q25 rather than sargo's: its \
own configure and compile with the prebuilt Clang, no kernel.bbclass. The \
boot image is assembled by luneos-bootimg-gki, the way bluejay and panther do \
it - bramble has the same header v3 / vendor_boot layout. \
"

LICENSE = "GPL-2.0-only"
# 4.19 carries the SPDX-style COPYING (GPL-2.0 WITH Linux-syscall-note), whose
# md5 differs from the classic GPLv2 text sargo's and sunfish's older trees
# ship - so state it rather than have do_populate_lic fail on a license that
# has not actually changed.
LIC_FILES_CHKSUM = "file://COPYING;md5=bbea815ee2795b2f4230826c0c6b8814"

# The Clang the stock kernel was built with: build.config.common in this very
# tree says
#   CLANG_PREBUILT_BIN=prebuilts-master/clang/host/linux-x86/clang-r416183b/bin
# which is the same release q25 pins, so fetch-gki-toolchain.sh already knows
# how to produce it. Set GKI_CLANG_DIR:bramble in local.conf.
GKI_CLANG_VERSION ?= "r416183b"
GKI_CLANG_DIR ?= ""
GKI_CLANG_URI ?= ""

SRC_URI = "\
    git://github.com/LineageOS/android_kernel_google_redbull.git;protocol=https;branch=lineage-23.2;name=kernel \
    ${@(d.getVar('GKI_CLANG_URI') + ';name=clang;subdir=clang') if d.getVar('GKI_CLANG_URI') else ''} \
    file://luneos.cfg;subdir=frag \
    file://vermagic.cfg;subdir=frag \
    file://buildfix.cfg;subdir=frag \
    file://lineage-config;subdir=frag \
"
# Pinned to the revision the LineageOS 23.2 nightly for bramble was built from,
# taken out of that build's own build-manifest.xml:
#
#   LineageOS/android_kernel_google_redbull  a0096400123a165d701f23c7745d8d8e721d049d
#
# which is the head of lineage-23.2, so the branch above resolves it.
#
# WHY THIS REVISION AND NOT THE STOCK ONE
#   An earlier version of this recipe pinned 7b0944645172 (4.19.278), the commit
#   Google's own shipped vermagic names, and measured 0 of 218 *stock* vendor
#   modules failing. Right answer, wrong question: the phone does not run
#   Google's vendor_boot. The 221 modules actually on the device are LineageOS's
#   and want
#
#     vermagic=4.19.325-cip135-st19-ga0096400123a SMP preempt mod_unload modversions aarch64
#
#   On this device that is not a missing-feature problem. bramble keeps its
#   storage drivers in the vendor_boot ramdisk - ufs_qcom.ko, ufshcd-core.ko,
#   ufshcd-pltfrm.ko - which the initramfs must modprobe before it can find
#   userdata. A mismatch here is not "no wifi", it is no rootfs and no boot at
#   all, which is exactly what the 4.19.278 build did on hardware.
#
#   This revision also has techpack/{audio,camera,dataipa,display,video}
#   populated, where the stock one had only Kbuild and stub/ - so msm_drm.ko
#   (display), msm-vidc.ko and the fts touch driver can actually be built here.
SRCREV_kernel = "a0096400123a165d701f23c7745d8d8e721d049d"
SRCREV_FORMAT = "kernel"

# No S assignment: oe-core sets S = "${UNPACKDIR}/${BP}" and the git fetcher
# unpacks to exactly that.
PV = "4.19.325+git"
B = "${WORKDIR}/build"

# Host tools only. Nothing here compiles anything that runs on the target: the
# kernel proper is built by the pinned Clang, not by OE's cross toolchain.
DEPENDS = "elfutils-native openssl-native bc-native bison-native flex-native lz4-native python3-native"

inherit deploy

COMPATIBLE_MACHINE = "^bramble$"
PACKAGE_ARCH = "${MACHINE_ARCH}"
INHIBIT_DEFAULT_DEPS = "1"
EXCLUDE_FROM_WORLD = "1"

# Nothing is packaged from this recipe: the kernel goes to DEPLOYDIR as
# Image.lz4 and the modules as modules-${MACHINE}.tgz, both consumed by other
# recipes, and nothing ever installs a linux-google-bramble package.
#
# Saying so is required now that do_install actually populates ${D}. OE's
# do_package would otherwise try to split debug info out of the 237 .ko files
# with ${HOST_PREFIX}objcopy - and INHIBIT_DEFAULT_DEPS means there is no
# aarch64-webos-linux-objcopy in this recipe's sysroot:
#
#   FileNotFoundError: [Errno 2] No such file or directory: 'aarch64-webos-linux-objcopy'
#
# The modules are already stripped anyway - modules_install runs llvm-strip via
# INSTALL_MOD_STRIP=1 - so there is nothing to split even in principle.
# ... but the packaging tasks must still RUN. They cannot be marked noexec:
# oe.package_manager walks do_rootfs's dependency graph for do_package_write_ipk
# edges and insists on an sstate manifest for each one, so as soon as the
# initramfs image depends on this recipe, switching them off gives
#
#   ERROR: The sstate manifest for task 'linux-google-bramble:package_write_ipk'
#          could not be found.
#
# Before do_install existed this recipe already produced an (empty) package and
# its manifest, which is why nothing complained. So let packaging run as it did,
# and change only the two things that the newly-populated ${D} breaks:
#
#   - no debug splitting or stripping, which is what called
#     ${HOST_PREFIX}objcopy - absent here, because INHIBIT_DEFAULT_DEPS means
#     this recipe has no OE cross binutils. The modules are already stripped by
#     modules_install via INSTALL_MOD_STRIP=1.
#   - ship /lib/modules in the main package, so the 329 .ko files are not
#     "installed but not shipped".
#
# The package itself is never installed anywhere - the modules reach the device
# through the deploy tarball and the initramfs - it exists only to keep the
# packaging machinery coherent.
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
FILES:${PN} += "${nonarch_base_libdir}/modules"


# LUNEOS_KERNEL_FRAGMENT = "0" builds the pure stock baseline, which is how you
# establish that tree and toolchain reproduce the stock CRCs before blaming the
# LuneOS delta for a KMI failure. Baseline first, then the delta - the habit
# q25's recipe spells out.
LUNEOS_KERNEL_FRAGMENT ?= "1"

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

# 4.19 predates LLVM=1 - that shorthand arrived in 5.7, and this Makefile has no
# "ifneq ($(LLVM),)" block at all, so the LLVM=1 in the tree's own
# build.config.common is read by Google's build.sh and never by kbuild. Spell
# out what that shorthand would have set instead:
#
#   CC / LD               build.config.redbull.common.clang says exactly this
#                         pair: CC=clang, LD=ld.lld.
#   AR ... READELF        without LLVM=1, kbuild would reach for
#                         $(CROSS_COMPILE)ar and friends - GNU cross-binutils
#                         that this build does not have. The llvm-* equivalents
#                         ship in the same Clang prebuilt.
#   LLVM_IAS=1            build.config.aarch64 sets it; the integrated
#                         assembler is what removes the last need for GNU as.
#   CLANG_TRIPLE          4.19 does not derive clang's --target from
#                         CROSS_COMPILE the way 6.1 does.
#   CROSS_COMPILE         still consulted by a few cc-option probes; harmless
#                         to name even though no binary of that prefix is used.
#
# Not set: CROSS_COMPILE_ARM32/COMPAT. build.config.common names them, but
# redbull_defconfig has no CONFIG_COMPAT_VDSO, so nothing asks for a 32-bit
# toolchain here - unlike sunfish, whose fragment has to turn that option off.
KERNEL_MAKE = "make -C ${S} O=${B} ARCH=arm64 \
    CC=clang LD=ld.lld AR=llvm-ar NM=llvm-nm OBJCOPY=llvm-objcopy \
    STRIP=llvm-strip OBJDUMP=llvm-objdump READELF=llvm-readelf \
    HOSTCC=clang HOSTCXX=clang++ HOSTAR=llvm-ar HOSTLD=ld.lld \
    LLVM_IAS=1 CLANG_TRIPLE=aarch64-linux-gnu- CROSS_COMPILE=aarch64-linux-gnu-"

do_configure() {
    gki_clear_oe_env
    export PATH="${@gki_clang_bin(d)}:$PATH"
    mkdir -p ${B}
    # scripts/setlocalversion appends a "+" - and "-dirty" once do_patch has
    # touched the tree - to anything it derives itself, and either is enough to
    # miss the vermagic match by one character. An empty .scmversion
    # short-circuits scm_version() to the empty string. Same treatment as MP01.
    : > ${S}/.scmversion

    # Base config = what the shipped LineageOS 23.2 kernel actually carries,
    # extracted from the IKCFG_ST blob in that build's boot.img, NOT
    # arch/arm64/configs/redbull_defconfig. The defconfig is what LineageOS
    # feeds kbuild; this is what came out, with every dependency resolved.
    # Guessing from a defconfig instead cost sunfish several builds chasing
    # DEBUG_FS and CFI through options the vendor had already settled.
    #
    # -cip135 and -st19 need no help: they are localversion-cip and
    # localversion-st in the tree and setlocalversion collects them. Only the
    # -g<sha> half is stated, in vermagic.cfg.
    install -m 0644 ${UNPACKDIR}/frag/lineage-config ${B}/.config
    ${KERNEL_MAKE} olddefconfig

    # buildfix.cfg and vermagic.cfg are not part of the LuneOS delta: the first
    # is what makes this tree compile here, the second is what makes the stock
    # modules loadable at all. Both apply even to the baseline build, or
    # LUNEOS_KERNEL_FRAGMENT = "0" would not produce something to compare
    # against.
    ${S}/scripts/kconfig/merge_config.sh -m -O ${B} \
        ${B}/.config ${UNPACKDIR}/frag/buildfix.cfg ${UNPACKDIR}/frag/vermagic.cfg \
        ${UNPACKDIR}/frag/buildfix.cfg
    ${KERNEL_MAKE} olddefconfig

    if [ "${LUNEOS_KERNEL_FRAGMENT}" = "1" ]; then
        # merge_config.sh warns about anything the fragment asked for that did
        # not survive, which is how a typo is caught here rather than on the
        # device.
        ${S}/scripts/kconfig/merge_config.sh -m -O ${B} \
            ${B}/.config ${UNPACKDIR}/frag/luneos.cfg
        ${KERNEL_MAKE} olddefconfig
    fi
}

do_compile() {
    gki_clear_oe_env
    export PATH="${@gki_clang_bin(d)}:$PATH"
    ${KERNEL_MAKE} -j${@oe.utils.cpu_count()} \
        HOSTCFLAGS="-I${STAGING_INCDIR_NATIVE}" \
        HOSTLDFLAGS="-L${STAGING_LIBDIR_NATIVE} -Wl,-rpath,${STAGING_LIBDIR_NATIVE}" \
        Image.lz4 modules
}

# Unlike sunfish this recipe does not inherit kernel.bbclass, so nothing
# packages the modules for us: install them here and deploy a tarball that the
# initramfs image unpacks (ANDROID_INITRAMFS_KERNEL_MODULES in bramble.conf).
#
# Why we ship our own rather than load LineageOS's - see "THE module question"
# below. In short: our config delta moves exported-symbol CRCs, while modules
# built from the same tree and config match by construction. The merged
# initramfs performs the substitution for free: the bootloader lays vendor_boot's
# ramdisk down first and our boot ramdisk on top, so our /lib/modules/*.ko
# override theirs, while their modules.load, modules.dep and modules.softdep stay
# in place and go on deciding what loads and in what order.
# Both tasks run under pseudo, so the modules end up owned by root rather than
# by whoever ran bitbake. Without this, do_package refuses them -
#
#   KeyError: 'getpwuid(): uid not found: 1000'
#   Path .../usr/lib/modules/adc_tm.ko is owned by uid 1000, gid 1000, which
#   doesn't match any user/group on target.
#
# - and, worse, the tarball do_deploy writes would carry uid 1000 into the
# initramfs, where every file is expected to be root's. kernel.bbclass arranges
# this for ordinary kernel recipes; this one is standalone and has to say it.
# do_install runs under pseudo so the chown at the end of it is intercepted and
# the modules are recorded as root's - do_package refuses them otherwise
# ("getpwuid(): uid not found: 1000").
#
# do_deploy deliberately does NOT: bitbake creates sstate-build-deploy itself,
# outside pseudo, so running the task under pseudo makes the sstate hashing trip
# over that directory's ownership instead. The tarball gets correct ownership
# from tar's own flags instead of from pseudo.
do_install[fakeroot] = "1"

do_install() {
    # Same two lines every task that runs the kernel's make needs: OE's cross
    # variables out of the way, and Google's Clang on PATH. modules_install is
    # not just a copy - it runs depmod, and with INSTALL_MOD_STRIP it runs
    # ${STRIP}, which KERNEL_MAKE sets to llvm-strip. Without the PATH it fails
    # with "llvm-strip: not found" after having installed part of the tree.
    gki_clear_oe_env
    export PATH="${@gki_clang_bin(d)}:$PATH"

    # ${nonarch_base_libdir} is /usr/lib on a usrmerge distro, which this is -
    # packaging fails otherwise ("package is not obeying usrmerge distro
    # feature"). modules_install writes INSTALL_MOD_PATH/lib/modules, so stage it
    # and move, rather than fighting kbuild.
    install -d ${D}${nonarch_base_libdir}
    ${KERNEL_MAKE} INSTALL_MOD_PATH=${WORKDIR}/modinst INSTALL_MOD_STRIP=1 modules_install
    mv ${WORKDIR}/modinst/lib/modules ${D}${nonarch_base_libdir}/modules

    # FLATTEN into /lib/modules, and ship nothing but the .ko files.
    #
    # This is not tidying, it is the whole mechanism. The bootloader lays
    # vendor_boot's ramdisk down first and our boot ramdisk on top, and a later
    # cpio entry only replaces an earlier one at the SAME path. LineageOS's
    # vendor_boot keeps its 221 modules flat:
    #
    #   /lib/modules/ufs_qcom.ko
    #
    # while modules_install writes the usual
    #
    #   /lib/modules/4.19.325-cip135-st19-ga0096400123a/kernel/drivers/.../ufs_qcom.ko
    #
    # Leave it like that and ours override nothing: init.sh reads the vendor's
    # /lib/modules/modules.load and modprobes out of /lib/modules, so it would
    # load THEIR binaries against OUR kernel - the exact CRC failure this whole
    # arrangement exists to avoid, and an unbootable phone, because the UFS
    # drivers are in that set.
    #
    # The index files are deliberately NOT shipped either: modules.load,
    # modules.dep and modules.softdep stay the vendor's, which is what decides
    # what loads and in what order. They reference modules by name, so they
    # remain correct for our binaries.
    kver=$(cat ${B}/include/config/kernel.release)
    find ${D}${nonarch_base_libdir}/modules -name '*.ko' -exec mv -f -t ${D}${nonarch_base_libdir}/modules/ {} +
    rm -rf ${D}${nonarch_base_libdir}/modules/$kver
    # Tell pseudo these are root's. do_install runs under pseudo (see the
    # fakeroot flags above) but pseudo only records what it is asked to record -
    # files kbuild copies in keep the builder's uid in pseudo's database, and
    # do_package then refuses them:
    #
    #   KeyError: 'getpwuid(): uid not found: 1000'
    #
    # An explicit chown IS intercepted, which both satisfies packaging and puts
    # root ownership in the tarball that ends up in the initramfs.
    chown -R root:root ${D}${nonarch_base_libdir}/modules

    echo "installed $(ls ${D}${nonarch_base_libdir}/modules/*.ko | wc -l) modules, flat, to override vendor_boot's"
}

do_deploy() {
    install -d ${DEPLOYDIR}
    # luneos-bootimg-gki picks this up via GKI_KERNEL_PROVIDER in bramble.conf.
    install -m 0644 ${B}/arch/arm64/boot/Image.lz4 ${DEPLOYDIR}/Image.lz4

    # The modules as one tarball. A tarball rather than packages because this
    # recipe has no kernel.bbclass packaging - and it is safe here, unlike on
    # sunfish, because bramble's boot image is assembled by a separate recipe
    # (luneos-bootimg-gki), so the initramfs can depend on this task without the
    # dependency loop that route creates when the kernel's own do_deploy builds
    # the boot image.
    # --owner/--group because this task is not under pseudo: without them the
    # archive records whoever ran bitbake, and those uids would be carried into
    # the initramfs when it is unpacked there - every file in an initramfs is
    # expected to be root's.
    tar --owner=0 --group=0 --numeric-owner \
        -czf ${DEPLOYDIR}/modules-${MACHINE}.tgz -C ${D} usr/lib/modules
    # The input to kmi-crc-check.py, and the only way to tell without a device
    # whether a config change just stopped the stock vendor modules loading.
    install -m 0644 ${B}/Module.symvers ${DEPLOYDIR}/Module.symvers
    install -m 0644 ${B}/.config ${DEPLOYDIR}/kernel-config
}
addtask deploy after do_compile after do_install before do_build

#-----------------------------------------------------------------------------
# THE module question, which on this device is the whole port.
#
# device/google/redbull's modules.load has **329 entries**, and they are not
# peripherals: ufs_qcom.ko, ufshcd-core.ko, ufshcd-pltfrm.ko, phy-qcom-ufs*.ko,
# pinctrl-lito.ko, clk-qcom.ko, gcc-lito.ko, msm_drm.ko, msm_adreno.ko,
# arm-smmu.ko, ipa3.ko, diagchar.ko, the whole techpack camera and audio
# stacks, and extra/wlan.ko. Storage, clocks, pinctrl and display are modules.
# They ship in the stock vendor_boot's 30 MB vendor ramdisk and in vendor_dlkm,
# and this recipe leaves both alone.
#
# redbull_defconfig sets CONFIG_MODVERSIONS=y. So the rule that governs
# bluejay - the KMI discipline in kernel-porting.md - governs this device too,
# even though it is not GKI: any config option we add that changes the CRC of
# an exported symbol makes the stock modules refuse to load, and on bramble
# that means no storage, not a missing feature.
#
# Hence luneos.cfg here is deliberately SMALLER than sunfish's: it omits
# IPC_NS, FANOTIFY and NET_L3_MASTER_DEV, which are the empirically-established
# poison on bluejay's KMI. That list is a property of a specific kernel tree,
# not a universal. Measured here on 22 Sep 2026: the full delta scores 0 of 218
# modules failing, identical to the stock-config baseline, so nothing in
# luneos.cfg poisons this KMI.
#
# CONFIG_SYSVIPC used to be the exception - libPmLogLib called shmget()/
# shmat(), so without it PmLogCtl blocked forever and the compositor never
# started - and it is also the most KMI-hostile option there is. PmLog moved to
# POSIX shared memory on 15 Sep 2026, so the option is simply off now and the
# genksyms patch that carried it has been dropped. Nothing in luneos.cfg
# touches task_struct any more.
#
# The gate, runnable host-side with no device, before anything is flashed:
#
#   1. extract the stock modules once:
#        cd ~/webos/LuneOS/bramble/stock
#        python3 -c 'import sys;d=open("vendor_boot.img","rb").read();...'   # or
#        unpack_bootimg --boot_img vendor_boot.img --out vb && \
#          lz4 -d vb/vendor_ramdisk | cpio -idmv -D vb-ramdisk
#   2. after a kernel build - do_deploy puts Module.symvers next to the image:
#        python3 ~/webos/gsigki/bluejay/kmi-crc-check.py \
#            tmp/deploy/images/bramble/Module.symvers \
#            vb-ramdisk/lib/modules
#   3. target: 0 modules would fail to load. Anything else, bisect the
#      fragment with symvers-drift.py before flashing.
#
# CONFIG_MODULE_FORCE_LOAD is in luneos.cfg as an escape hatch for debugging a
# single driver, NOT as the plan: force-loading a module whose struct layouts
# genuinely moved is how you get silent memory corruption, and these modules
# are the storage stack. mindphone force-loaded wifi/BT/GPS safely; that is a
# different risk class from ufshcd-core.
#-----------------------------------------------------------------------------
