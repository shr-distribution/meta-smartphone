require recipes-kernel/linux/linux-unihertz-alps-a11.inc

DESCRIPTION = "Linux kernel for the Unihertz Titan Slim (MT6771), from TheKit's \
reverse-engineered MediaTek ALPS 4.14 tree - Unihertz publish no source"

COMPATIBLE_MACHINE = "^titanslim$"

# Adds, over the shared Android-11 config: the R63308_ZMK42 panel at 768x1280,
# the CHSC5432 touchscreen and TRUSTKERNEL_TEE_FP_SUPPORT.
#
# Two gaps in this defconfig, both upstream's and both worth checking before
# blaming anything here:
#
#   - CONFIG_MTK_AW9523 is NOT set, although the Slim plainly has a physical
#     QWERTY. titanpocket_defconfig does set it. The keyboard driver was last
#     reworked in Sep 2025 and the Slim config may simply not have been
#     regenerated since; expect to add it.
#   - CONFIG_CUSTOM_KERNEL_IMGSENSOR and CONFIG_CUSTOM_KERNEL_CAM_CAL_DRV are
#     absent, so no camera sensor is compiled in at all. The Pocket names two.
UNIHERTZ_DEFCONFIG = "titanslim_defconfig"
