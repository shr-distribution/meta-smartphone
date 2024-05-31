inherit linux-mainline-8953

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^tissot$"

# parameters for the fastboot image
inherit kernel_android
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x80008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x81000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x80f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x80000100"

do_deploy[depends] += " lk2nd-msm8953:do_deploy"

SRC_URI += " \
    file://defconfig \
"

