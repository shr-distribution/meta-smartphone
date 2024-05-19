inherit linux-mainline-8953

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "rosy"

# parameters for the fastboot image
inherit kernel_android
ANDROID_BOOTIMG_CMDLINE = "androidboot.hardware=qcom msm_rtb.filter=0x237 ehci-hcd.park=3 lpm_levels.sleep_disabled=1 androidboot.bootdevice=7824900.sdhci "
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x80008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x81000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x80f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x80000100"

do_deploy[depends] += " lk2nd-msm8953:do_deploy"

SRC_URI += " \
    file://defconfig \
    file://0001-rosy-add-i2c-sensors.patch \
"
