require linux.inc

PV = "2.6.32+${PR}+gitr${SRCREV}"
PR = "r15"

COMPATIBLE_MACHINE = "htcnexus|htcleo"

SRCREV = "1da53cb5741477b03021991843636097a18ba874"

SRC_BRANCH = "htc-msm-2.6.32"

SRC_URI = "\
  git://git.linuxtogo.org/home/groups/mobile-linux/kernel.git;branch=${SRC_BRANCH};protocol=git \
  file://defconfig \
"

S = "${WORKDIR}/git"

LIC_FILES_CHKSUM = "file://COPYING;md5=d7810fab7487fb0aad327b76f1be7cd7"

