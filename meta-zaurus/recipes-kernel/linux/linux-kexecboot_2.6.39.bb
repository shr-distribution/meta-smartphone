require linux-kexecboot.inc

PR = "${INC_PR}.2"

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
            ${KERNELORG_MIRROR}/linux/kernel/v2.6/patch-${PV}.3.bz2;apply=yes;name=stablepatch \
            file://defconfig \
            "

SRC_URI[kernel.md5sum] = "1aab7a741abe08d42e8eccf20de61e05"
SRC_URI[kernel.sha256sum] = "584d17f2a3ee18a9501d7ff36907639e538cfdba4529978b8550c461d45c61f6"
SRC_URI[stablepatch.md5sum] = "06b858e8f81600038129afe7bcd4e162"
SRC_URI[stablepatch.sha256sum] = "244d18d076c06e3cd627128000fd18ebdfb3a8dae907e653fb6a6648c7dcbf5e"
