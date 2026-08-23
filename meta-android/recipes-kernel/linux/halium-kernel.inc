# Kernel options every Halium target needs, regardless of device.
#
# Android defconfigs are built for Android, so they omit things a general
# purpose Linux userspace assumes. Rather than repeat the same edits in each of
# the device kernel recipes, require this from them:
#
#     require recipes-kernel/linux/halium-kernel.inc
#
# The options are forced rather than merely appended: an Android defconfig may
# already set them the other way, and a plain append would lose to the existing
# line.

halium_kernel_conf() {
    # Drop any existing setting first - appending alone loses to whatever the
    # Android defconfig already said.
    sed -i "/CONFIG_$1[ =]/d" ${B}/.config
    kernel_conf_variable $1 $2 ${B}/.config
}

# Append to CONFIG_CMDLINE and switch the kernel to extending the bootloader's
# command line rather than replacing it or deferring entirely.
#
# Not named *_append: bitbake reads a name ending in _append, _prepend or
# _remove as the old override syntax and refuses to parse the recipe.
#
# Written out by hand rather than through halium_kernel_conf because the value
# is a quoted string that has to keep whatever the device already set - sargo
# ships " loglevel=3" - and because the three CMDLINE_* choices are mutually
# exclusive, so FROM_BOOTLOADER and FORCE have to go.
halium_kernel_add_cmdline() {
    _cur=$(sed -n 's/^CONFIG_CMDLINE="\(.*\)"$/\1/p' ${B}/.config)
    sed -i '/^CONFIG_CMDLINE=/d' ${B}/.config
    echo "CONFIG_CMDLINE=\"${_cur} $1\"" >> ${B}/.config
    halium_kernel_conf CMDLINE_FROM_BOOTLOADER n
    halium_kernel_conf CMDLINE_FORCE n
    halium_kernel_conf CMDLINE_EXTEND y
}

do_configure:append:halium() {
    # overlayfs. luneos-device-config applies device adaptations over the
    # rootfs, and prefers a single overlay mount to one bind mount per file.
    # The bind fallback works everywhere and is what runs without this, but it
    # can only *shadow* files that already exist - it cannot add a file the
    # generic image does not ship, and creating a placeholder to bind onto
    # would mean writing to the rootfs, which is exactly what the universal
    # image must not do. sargo's 4.9 kernel has no CONFIG_OVERLAY_FS at all:
    # "overlay" does not appear in /proc/filesystems.
    halium_kernel_conf OVERLAY_FS y


    # cgroup v1, for Android's libprocessgroup.
    #
    # Android 11 wants the v1 controllers at /dev/cpuctl, /dev/cpuset and
    # friends, as its cgroups.json says. On a v2-only host it cannot mount them
    # and every service it starts dies:
    #
    #   init: createProcessGroup(1000, 987) failed for service
    #         'vndservicemanager': Read-only file system
    #   init: Service 'vndservicemanager' (pid 987) received signal 6
    #
    # Halium's own libprocessgroup patch assumes a v1 host - it works around
    # "systemd with cgroup v1 mounts cpu and cpuacct controllers together".
    #
    # systemd 257 refuses v1 unless BOTH of these are on the kernel command
    # line; src/shared/cgroup-setup.c reads them with proc_cmdline_get_bool(),
    # so there is no environment variable or config file alternative.
    #
    # They cannot go in ANDROID_BOOTIMG_CMDLINE, which is where LuneOS
    # documents putting them: a Pixel bootloader takes only the arguments it
    # already knows from the boot image and silently drops the rest. Verified
    # on sargo - selinux=1, the two below and even a "zz.marker=1" canary never
    # reached /proc/cmdline, while the existing datapart= and lpm_levels.
    # arguments did, and moving them to the front of the string changed
    # nothing. CONFIG_CMDLINE_EXTEND appends the kernel's own string to
    # whatever the bootloader supplies, which the bootloader cannot filter.
    halium_kernel_add_cmdline "systemd.unified_cgroup_hierarchy=0 SYSTEMD_CGROUP_ENABLE_LEGACY_FORCE=1"

    # Not set here: CONFIG_ANDROID_BINDERFS. mount-android.sh mounts binderfs
    # when the kernel advertises it, but the symbol only exists from 5.0, so
    # forcing it on a 4.9 tree would just be dropped and read as though it were
    # supported. GKI-era kernels have it already.

    # Resolve the new symbols here rather than relying on a later task doing
    # it. A device recipe may run its own oldnoconfig in its own
    # do_configure:append, and the order the two appends end up in is not worth
    # depending on - running it twice is harmless, running it never is not.
    # olddefconfig replaced oldnoconfig in 4.19, so accept either.
    oe_runmake olddefconfig || oe_runmake oldnoconfig
}
