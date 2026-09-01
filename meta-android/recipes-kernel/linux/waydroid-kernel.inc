# Kernel options Waydroid needs, for device kernels that predate them.
#
#     require recipes-kernel/linux/waydroid-kernel.inc
#
# Waydroid itself has no kernel version floor - it needs binder plus either
# ashmem or memfd, and a 4.9 Android kernel has both. What it does need is a
# set of options an Android defconfig has no reason to carry, because Android
# is the thing running in the container rather than the thing hosting it.
#
# Measured against sargo, which runs Waydroid on a 4.9 kernel with no changes:
# every option below is already correct there and wrong on tissot. This file
# exists to close that gap for the device trees that were not so lucky.
#
# Options are forced rather than appended. Every one of them is already set the
# other way in at least one Android defconfig we ship, and a plain append loses
# to the existing line.
#
# Hooked in through postfuncs rather than do_configure:append:halium, because
# halium-kernel.inc already defines a function under that name: a second
# definition would replace it rather than add to it, and silently drop its
# cgroup-v1 command line work. postfuncs compose, so a recipe may require both.

waydroid_kernel_conf() {
    # Drop any existing setting first - see above.
    sed -i "/CONFIG_$1[ =]/d" ${B}/.config
    kernel_conf_variable $1 $2
}

waydroid_kernel_config() {
    # Waydroid's own binder context managers.
    #
    # On a Halium host, Waydroid refuses to share the host HAL's /dev/binder:
    # tools/helpers/drivers.py searches BINDER_DRIVERS[:-1], which is
    # anbox-binder, puddlejumper and bonder but deliberately not binder, and
    # raises OSError when none of them exist. It does not probe or modprobe on
    # this path, and it does not use binderfs there either, so the nodes have
    # to exist before it runs. Without them "waydroid init" fails with
    #
    #   ERROR: Binder node "binder" for waydroid not found
    #
    # before it reads an image or touches the network.
    #
    # Written out by hand rather than through waydroid_kernel_conf because the
    # value is a quoted string, the same reason halium_kernel_add_cmdline is.
    # The stock three stay first: the host's Android HAL is already bound to
    # them and the order is what binder registers, not a preference.
    sed -i '/CONFIG_ANDROID_BINDER_DEVICES[ =]/d' ${B}/.config
    echo 'CONFIG_ANDROID_BINDER_DEVICES="binder,hwbinder,vndbinder,anbox-binder,anbox-hwbinder,anbox-vndbinder"' >> ${B}/.config

    # The container's network device. data/configs/config_3 sets
    #   lxc.net.0.type = veth
    # against the waydroid0 bridge, with no fallback path, so this is not
    # optional. Android defconfigs leave it off - a phone has no reason to make
    # ethernet pairs - and on tissot even loading it by hand fails:
    # "RTNETLINK answers: Operation not supported".
    waydroid_kernel_conf VETH y

    # Overlays for /system and /vendor. Waydroid catches the mount failure and
    # disables the feature, so this degrades rather than breaks, but without it
    # nothing the user changes in the Android system image survives.
    # halium-kernel.inc already forces this for its own reasons; repeated here
    # so this file stands alone.
    waydroid_kernel_conf OVERLAY_FS y

    # sw_sync, bind-mounted into the container for the hwcomposer path.
    waydroid_kernel_conf SW_SYNC y

    # uhid, for Android's input stack.
    waydroid_kernel_conf UHID y

    # Real-time group scheduling has to be off, or LXC cannot set up the
    # container's cgroup - waydroid#346. Android defconfigs turn it on;
    # upstream's default is n, and sargo, which works, has it off.
    waydroid_kernel_conf RT_GROUP_SCHED n

    # Pressure stall information. Upstream blames a missing PSI for
    # "WARNING: Service manager /dev/binder has died".
    #
    # The symbol arrived in 4.20, so on the 4.9 and 4.14 trees here these two
    # lines are dropped by olddefconfig and the warning above stays possible.
    # Kept anyway: this file is shared, and a GKI-era device that requires it
    # should get PSI without having to remember to ask. Same reasoning as
    # halium-kernel.inc applies to CONFIG_ANDROID_BINDERFS, in reverse.
    waydroid_kernel_conf PSI y
    waydroid_kernel_conf PSI_DEFAULT_DISABLED n

    # Resolve the new symbols. A device recipe may run its own oldnoconfig in
    # its own do_configure:append and the resulting order is not worth
    # depending on; running it twice is harmless, running it never is not.
    # olddefconfig replaced oldnoconfig in 4.19, so accept either.
    #
    # -C ${S} O=${B} spelled out, as linux.inc does. A postfunc does not
    # inherit do_configure's working directory, so a bare oe_runmake here finds
    # whatever Makefile happens to be under cwd and fails with
    # "No rule to make target 'olddefconfig'".
    oe_runmake -C ${S} O=${B} olddefconfig || oe_runmake -C ${S} O=${B} oldnoconfig
}

do_configure[postfuncs] += "waydroid_kernel_config"
