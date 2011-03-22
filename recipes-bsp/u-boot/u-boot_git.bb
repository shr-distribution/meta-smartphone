require u-boot.inc
PR = "r73"

FILESPATHPKG =. "u-boot-git:"

SRCREV_nokia900 = "bd2313078114c4b44c4a5ce149af43bcb7fc8854"

S = "${WORKDIR}/git"

PV_nokia900 = "2010.06+gitr${SRCPV}"
SRC_URI_nokia900 = "git://www.denx.de/git/u-boot.git;protocol=git \
                    file://0001-ARM-Avoid-compiler-optimization-for-usages-of-readb-.patch \
                    file://0001-Reduce-delays-in-omap-i2c-driver.patch \
                    file://0002-Make-bootm-optionally-use-pre-existing-atags-for-Lin.patch \
                    file://0003-Store-existing-atags-at-startup-if-chainloading.patch \
                    file://0004-Nokia-RX-51-aka-N900-support.patch \
                    file://0001-nokia-rx51-fix-declaration-fails-when-building-with-.patch \
                    file://0005-fix-loading-file-from-ext2-partition-on-OMAP3-evm.patch \
                    file://0006-omap3_mmc.c-fix-formating.patch \
                    file://0007-Only-delay-boot-if-keyboard-open.patch \
		    file://gcc-4.5.O0.patch \
"
SRC_URI_nokia900_append_shr = " \
                    file://0001-configs-nokia_rx51.h-start-shr-as-default-and-change.patch \
"

UBOOT_MACHINE_nokia900 = "nokia_rx51_config"
