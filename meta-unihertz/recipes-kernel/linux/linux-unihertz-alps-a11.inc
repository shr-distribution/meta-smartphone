# The Android-11 MT6771 ALPS drop: Titan Pocket and Titan Slim.
#
# One tree, one branch, one revision and one dtb serve both devices - the two
# only diverge in the defconfig. Everything in this file is therefore shared,
# and the per-device recipes are three lines each.
#
# See the layer README, "Can one image serve the Pocket and the Slim?", for how
# close the two defconfigs are and what would have to be settled on hardware to
# collapse them into a single boot image.

require recipes-kernel/linux/linux-unihertz-mt6771.inc

# github.com/shr-distribution/linux, branch unihertz-mt6771/4.14/halium-11.0:
# TheKit's halium-11.0 branch of github.com/ubuntu-touch-unihertz-titan/
# kernel-alps-mt6771 at ba3ad72c6c2d ("aw9523: rework the keyboard driver based
# on the orig kernel binary", 10 Sep 2025, that branch's head) with the LuneOS
# commits on top. That branch carries the drivers TheKit recovered for both
# devices: GT1151QM and CHSC5432 touchscreens, the R63308_CASCADEMDZ40 and
# R63308_ZMK42 panels, the is31fl backlight and the AW9523 keyboard matrix.
#
# The LuneOS commits on the branch:
#  - build the tree with OE's GCC instead of the clang r383902 it was written
#    against: the benign newer-GCC warning classes demoted back to warnings
#    (build plumbing), then what GCC found - bogus inline on two exported
#    block functions, an extraneous set_selection definition, a missing
#    lcm_init prototype, pointers logged through the wrong integer type, K&R
#    definitions without a (void) prototype, and a void * function returning
#    -ENOMEM;
#  - the backlight level inverted for the Titan Pocket's CP2166;
#  - the Sunwave TEE fingerprint driver (v2.2.38, from Teracube);
#  - the ov16880 (from TheKit's Titan branch) and ov8865 (from Teracube)
#    camera sensors, the ov16880 EEPROM entry and the Pocket's rear-camera
#    supply routing;
#  - the Titan Pocket's own aw9523 key matrix, and its Fn key as a Ctrl key.
#
# NOTE: unlike the Titan's halium-10.0 branch, nothing here has been shown to
# boot - the Ubuntu Touch port for these two devices still packs the *stock*
# prebuilt kernel and builds only loadable modules out of this tree. We are
# ahead of upstream in asking it to be a whole kernel.
SRCREV = "8d83bc9a15759440ccdef8b6c47961d38a1fff31"

SRC_URI = "git://github.com/shr-distribution/linux.git;branch=unihertz-mt6771/4.14/halium-11.0;protocol=https \
           file://luneos.cfg \
           file://TitanPocket-mt6771.dtb \
"

LINUX_VERSION = "4.14"
KV = "4.14.186"

# The dtb section of a v2 header must be an AOSP dt_table blob (magic
# 0xd7b7ab1e), not a bare FDT: MediaTek lk only parses the table format.
#
# This is the stock blob out of the UBports port repo, and the Pocket and the
# Slim ship the BYTE-IDENTICAL file - md5 1c0ec5e5c084e9bbb559ea99651a2e56,
# 114,158 bytes, one entry of 114,094 bytes at offset 64, page_size 2048,
# platform id 0. Hence one copy here rather than two, and hence the dtb is not
# a reason the two devices need separate images.
ANDROID_BOOTIMG_DTB = "${UNPACKDIR}/TitanPocket-mt6771.dtb"

# 11.0.0, security patch 2022-03, packed as (os_version << 11) | patch_level
# the way the v3/v4-style header field wants it. Matches the stock boot.img
# these replace, so MediaTek lk's rollback check stays happy. Never lower it
# below what is already flashed.
ANDROID_BOOTIMG_OS_VERSION = "0x16000163"
