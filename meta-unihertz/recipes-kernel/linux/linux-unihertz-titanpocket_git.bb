require recipes-kernel/linux/linux-unihertz-alps-a11.inc

DESCRIPTION = "Linux kernel for the Unihertz Titan Pocket (MT6771), from TheKit's \
reverse-engineered MediaTek ALPS 4.14 tree - Unihertz publish no source"

COMPATIBLE_MACHINE = "^titanpocket$"

# Adds, over the shared Android-11 config: the R63308_CASCADEMDZ40 panel, the
# Goodix GT1151QM touchscreen, the ov16880 + ov8865 image sensors and
# CONFIG_MTK_AW9523 for the keyboard matrix.
UNIHERTZ_DEFCONFIG = "titanpocket_defconfig"

# No camera sensor driver for this device exists in the tree; see the fragment.
SRC_URI += "file://titanpocket-imgsensor.cfg file://titanpocket-fingerprint.cfg"
UNIHERTZ_EXTRA_CFG = "titanpocket-imgsensor.cfg titanpocket-fingerprint.cfg"
