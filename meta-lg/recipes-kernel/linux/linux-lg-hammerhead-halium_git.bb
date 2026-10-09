require recipes-kernel/linux/linux.inc
require recipes-kernel/linux/halium-kernel.inc
require recipes-kernel/linux/halium-kernel-3.4.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "hammerhead-halium"

DESCRIPTION = "Linux kernel for the LG Hammerhead (Nexus 5) device based on the offical \
source from Google/LG"

# The two systemd flags have to be here and not only in CONFIG_CMDLINE (which
# halium-kernel.inc also sets): this kernel boots from a device tree, and in
# 3.4 the DT path uses CONFIG_CMDLINE only when the bootloader passes no command
# line at all - CMDLINE_EXTEND is honoured on the ATAGS path alone. Checked on a
# booted device: /proc/config.gz had the flags, /proc/cmdline did not.
ANDROID_BOOTIMG_CMDLINE = "androidboot.hardware=hammerhead user_debug=31 maxcpus=2 msm_watchdog_v2.enable=1 systemd.unified_cgroup_hierarchy=0 SYSTEMD_CGROUP_ENABLE_LEGACY_FORCE=1"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x00008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x02900000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x02700000"

inherit kernel_android

# kernel.bbclass sets S = "${STAGING_KERNEL_DIR}", and do_symlink_kernsrc only
# moves the unpacked tree there when the recipe points S somewhere else.
S = "${UNPACKDIR}/${BP}"

# The topic branch is hammerhead/3.4/halium-9.0 plus what running the 16.0 GSI needs (see
# android-system-image-legacy-gsi.inc), carried as commits instead of recipe patches:
#
#  - the five loop driver fixes that mako/3.4/halium-9.0 carries and this tree lacked
#    (cherry-picked from there). Android 16 attaches about ten APEX images with mount -o loop
#    at boot, and on this kernel that hung in lo_ioctl and lo_open with no task visibly
#    holding the loop mutex. Whether they cure the hang is not proven; until then a udev rule
#    that stops udev opening loop devices avoids it.
#  - msm_cpp: do not panic when its firmware is missing.
#  - ASoC DPCM: clear a dangling FE runtime pointer in the open path, the best-fitting
#    explanation for the snd_pcm_drop panic (not yet shown on hardware to cure it).
#  - ASoC DPCM: do not connect a BE that has no stream in the requested direction. Recording
#    video opened a capture PCM whose path reached such a BE, and soc_pcm_open() dereferenced its
#    NULL private_data (panic in qml-runner, last_kmsg saved). Not yet run on the device.
#  - ARM: renameat2 for the flags == 0 case, which Android 16's bionic needs (rename() has no
#    ENOSYS fallback). halium-kernel-3.4.inc adds it as a patch for the other 3.4 kernels, so
#    say that this one has it.
KERNEL_HAS_RENAMEAT2 = "1"
# The 64-bit time clock calls (see halium-kernel-3.4.inc).
KERNEL_ADD_CLOCK_TIME64 = "1"
SRC_URI = "git://github.com/shr-distribution/linux.git;branch=herrie/hammerhead-3.4-gsi;protocol=https"

do_configure:prepend() {
    cp -v -f ${S}/arch/arm/configs/lineageos_hammerhead_defconfig ${WORKDIR}/defconfig
}

SRCREV = "863b3b0fbdc5ac4ea8aff932bd22aa7121f7fb36"

LINUX_VERSION = "3.4.0"
PV = "${LINUX_VERSION}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

do_install:append() {
    # make headers_install leaves kbuild's ..install.cmd bookkeeping behind, and
    # linux.inc ships everything under ${exec_prefix}/src/linux* as kernel-headers.
    # Those files record absolute command lines, which wrynose rejects as
    # "contains reference to TMPDIR [buildpaths]".
    find ${D}${exec_prefix}/src -name '..install.cmd' -delete 2>/dev/null || true
}
