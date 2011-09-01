require linux-kexecboot.inc

PR = "${INC_PR}.4"

S = "${WORKDIR}/linux-${PV}"

# Mark archs/machines that this kernel supports
DEFAULT_PREFERENCE = "-1"

DEFAULT_PREFERENCE_akita = "1"
DEFAULT_PREFERENCE_c7x0 = "1"
DEFAULT_PREFERENCE_collie = "1"
DEFAULT_PREFERENCE_poodle = "1"
DEFAULT_PREFERENCE_spitz = "1"
DEFAULT_PREFERENCE_tosa = "1"

SRC_URI += "${KERNELORG_MIRROR}/linux/kernel/v2.6/linux-${PV}.tar.bz2;name=kernel \
            ${KERNELORG_MIRROR}/linux/kernel/v2.6/patch-${PV}.4.bz2;apply=yes;name=stablepatch \
            file://0001-pcmcia-pxa2xx_sharpsl-retain-ops-structure-on-collie.patch \
            file://locomo_kbd_tweak-r2.patch \
            file://defconfig \
            "

SRC_URI[kernel.md5sum] = "1aab7a741abe08d42e8eccf20de61e05"
SRC_URI[kernel.sha256sum] = "584d17f2a3ee18a9501d7ff36907639e538cfdba4529978b8550c461d45c61f6"
SRC_URI[stablepatch.md5sum] = "ff5eb7323c054a128d2922bde3297ed5"
SRC_URI[stablepatch.sha256sum] = "132ba590e92d5c9a1bb0dc0885795a095d48e7db2552f1c9e3c26ce946a70a40"
