SUMMARY = "lk2nd Android bootloader"
DESCRIPTION = "Android bootloader for Qualcomm MSM devices."
LICENSE = "GPL-2.0-only & MIT"
LIC_FILES_CHKSUM = 'file://LICENSE;md5=2724240b5837035c1d1ea21590998de5'

inherit deploy

DEPENDS = "gcc-arm-none-eabi-native dtc-native python3-dtc-native"

PV = "0.3.8+git"

BRANCH = "main"
SRCREV ?= "9100613e03a4f609915e54ad4a957bed3555795a"
SRC_URI = "git://github.com/msm8953-mainline/lk2nd.git;protocol=https;branch=${BRANCH} \
"

S = "${WORKDIR}/git"

PACKAGE_ARCH = "${MACHINE_ARCH}"

COMPATIBLE_MACHINE = "(rosy|mido|tissot)"

# Let the Makefile handle setting up the CFLAGS and LDFLAGS as it is a standalone application
CFLAGS[unexport] = "1"
LDFLAGS[unexport] = "1"
AS[unexport] = "1"
LD[unexport] = "1"

do_configure() {
	make spotless
}

do_compile() {
	make LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:${STAGING_LIBDIR_NATIVE}" TOOLCHAIN_PREFIX=arm-none-eabi- msm8953-secondary
}

do_install() {
	:
}

do_deploy() {
	install -m 0644 ${S}/build-msm8953-secondary/lk2nd.img ${DEPLOYDIR}/lk2nd-${MACHINE}.img
}

addtask deploy before do_build after do_compile
