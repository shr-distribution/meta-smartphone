FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:tenderloin = "\
    file://S01-mount-boot.sh \
    file://K99-move-boot.sh \
"

COMPATIBLE_MACHINE:tenderloin = "tenderloin"

do_install:append:tenderloin() {
    install -d ${D}/scripts/local-premount
    install -d ${D}/scripts/local-bottom
    install -m 0755 ${WORKDIR}/S01-mount-boot.sh ${D}/scripts/local-premount/S01-mount-boot.sh
    install -m 0755 ${WORKDIR}/K99-move-boot.sh ${D}/scripts/local-bottom/K99-move-boot.sh
    echo ". /scripts/local-premount/S01-mount-boot.sh" >> ${D}/scripts/local-premount/ORDER
    echo ". /scripts/local-bottom/K99-move-boot.sh" >> ${D}/scripts/local-bottom/ORDER
}

FILES:${PN} += "/scripts/local-premount /scripts/local-bottom"
