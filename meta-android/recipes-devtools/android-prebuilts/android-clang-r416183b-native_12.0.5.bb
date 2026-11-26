# Android (7284624, based on r416183b) clang version 12.0.5: android12-5.10 (q25, mp01, bramble), sunfish, radon.
# The commit the common-android12-5.10 kernel manifest pins; 7154d3ed, which
# the other three use, no longer carries r416183b.
require android-clang.inc

ANDROID_CLANG_VERSION = "r416183b"
SRCREV = "6e3223f76384455acde43affde3df0ea9df66c0d"
