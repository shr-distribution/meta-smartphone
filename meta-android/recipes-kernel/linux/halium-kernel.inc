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
