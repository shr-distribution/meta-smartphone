#@TYPE: Machine
#@NAME: Palm Pre Smartphone
#@DESCRIPTION: Machine configuration for the Palm Pre

TARGET_ARCH = "arm"
SOC_FAMILY = "omap3"

require conf/machine/include/tune-cortexa8.inc

# use this for overrides
MACHINE_CLASS = "palmpre"

PREFERRED_PROVIDER_virtual/xserver = "xserver-xorg"
XSERVER = "xserver-xorg \
           xf86-input-evdev \
           xf86-input-mouse \
           xf86-video-omapfb \
           xf86-input-keyboard"

MACHINE_DISPLAY_WIDTH_PIXELS = "320"
MACHINE_DISPLAY_HEIGHT_PIXELS = "480"
MACHINE_DISPLAY_ORIENTATION = "0"
MACHINE_DISPLAY_PPI = "185"

PREFERRED_PROVIDER_virtual/kernel = "linux-palmpre"

IMAGE_FSTYPES += "tar.gz"

MACHINE_FEATURES = "kernel26 screen apm usbgadget usbhost vfat alsa phone bluetooth touchscreen keyboard wifi"
MACHINE_EXTRA_RDEPENDS = " \
  tsmd \
  read-tokens \
  lvm2 \
  kernel-module-sirloin-wifi \
"
# marvell-license \
# marvell-sdio-fw \
# palmpre-audio-scripts \
# compat-wireless-2.6-old \

# Increase this everytime you change something in the kernel
MACHINE_KERNEL_PR = "r3"
KERNEL_IMAGETYPE = "uImage"
UBOOT_ENTRYPOINT = "80008000"
UBOOT_LOADADDRESS = "80008000"
