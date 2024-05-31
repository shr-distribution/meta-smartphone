inherit linux-mainline-8953

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "rosy"

# parameters for the fastboot image
inherit kernel_android
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x80008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x81000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x80f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x80000100"

do_deploy[depends] += " lk2nd-msm8953:do_deploy"

SRC_URI += " \
    file://defconfig \
    file://0001-rosy-add-i2c-sensors.patch \
"

# This patchset is a set of hacks dedicated to listing iio sensors
# present on the device. It needs the 'sns-reg' recipe to be run
# in userland to list the sensors in dmesg.
SRC_URI_disabled += "\
    file://iio-sensors-details-hack/0001-DEBUG-QMI-TX-RX-monitor.patch \
    file://iio-sensors-details-hack/0002-net-qrtr-Turn-QRTR-into-a-bus.patch \
    file://iio-sensors-details-hack/0003-net-qrtr-Define-macro-to-convert-QMI-version-and-ins.patch \
    file://iio-sensors-details-hack/0004-WIP-iio-Add-Qualcomm-Sensor-Manager-driver.patch \
    file://iio-sensors-details-hack/0005-WIP-iio-accel-Add-driver-for-Qualcomm-Sensor-Manager.patch \
    file://iio-sensors-details-hack/0006-fixup-WIP-iio-Add-Qualcomm-Sensor-Manager-driver.patch \
    file://iio-sensors-details-hack/0007-fixup-net-qrtr-Turn-QRTR-into-a-bus.patch \
"
