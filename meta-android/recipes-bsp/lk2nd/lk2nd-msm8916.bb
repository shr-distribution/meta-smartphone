SUMMARY = "lk2nd Android bootloader"
DESCRIPTION = "Android bootloader for Qualcomm MSM devices."
LICENSE = "GPL-2.0-only & MIT"
LIC_FILES_CHKSUM = 'file://LICENSE;md5=07a95ab00b41f0fd502338e16ecc2149'

inherit deploy

DEPENDS = "gcc-arm-none-eabi-native dtc-native python3-dtc-native"

PV = "23.1+git"

BRANCH = "main"
# NOT the 23.1 tag, deliberately. The published 23.1 binary and the 23.1 source
# tag do not agree: for msm8916 the tag lists a single appended DTB
#
#     ADTBS += $(LOCAL_DIR)/msm8939-qrd-skuk.dtb
#
# while the release image people actually download carries two, confirmed by
# walking back from the end of each payload - ours [1408], theirs [2688, 1408],
# the extra one being msm8916-qrd-9.dtb. Building the tag therefore produces an
# lk2nd whose only appended device tree is an *msm8939* QRD, and the appended DTB
# is what the stock Samsung aboot matches against on an msm8916. A build from the
# tag resets 2-3 s after handover; the released binary with the same kernel
# behind it boots. main carries both entries.
SRCREV ?= "8b46487c4c76776c4f2f61468d44a74c69e6b9ea"
SRC_URI = "git://github.com/msm8916-mainline/lk2nd.git;protocol=https;branch=${BRANCH} \
"


PACKAGE_ARCH = "${MACHINE_ARCH}"

COMPATIBLE_MACHINE = "(a3-2015)"

# Let the Makefile handle setting up the CFLAGS and LDFLAGS as it is a standalone application
CFLAGS[unexport] = "1"
LDFLAGS[unexport] = "1"
AS[unexport] = "1"
LD[unexport] = "1"

do_configure() {
	make spotless
}

# NOTE: upstream lk2nd names its targets lk2nd-<soc>, unlike the
# msm8953-mainline fork (msm8953-secondary) that lk2nd-msm8953.bb builds. The
# build directory follows the target name, and so does the 512 KiB offset at
# which this lk2nd expects the real boot image - see LK2ND_BOOT_IMAGE_OFFSET in
# conf/machine/a3-2015.conf.
do_compile() {
	make LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:${STAGING_LIBDIR_NATIVE}" TOOLCHAIN_PREFIX=arm-none-eabi- lk2nd-msm8916
}

do_install() {
	:
}

do_deploy() {
	install -m 0644 ${S}/build-lk2nd-msm8916/lk2nd.img ${DEPLOYDIR}/lk2nd-${MACHINE}.img
}

addtask deploy before do_build after do_compile
