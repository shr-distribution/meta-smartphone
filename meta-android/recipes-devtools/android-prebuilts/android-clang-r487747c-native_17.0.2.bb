# Android (10087095, +pgo, +bolt, +lto, -mlgo, based on r487747c) clang version 17.0.2:
# android14-6.1 (bluejay, panther, tangorpro), and pioneer. Same commit as
# r450784e and r510928, so one download serves all three; its clang-r487747c is
# the same git tree (8eb8e3af) as at 7775eb11, which the
# android-gs-bluejay-6.1-android16 manifest pins.
require android-clang.inc

ANDROID_CLANG_VERSION = "r487747c"
SRCREV = "7154d3eddcb62ff7909a6ae2bf0c03e445e65bc4"
