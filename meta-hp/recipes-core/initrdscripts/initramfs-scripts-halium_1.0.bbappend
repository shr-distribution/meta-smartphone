FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI_append_tenderloin = "\
                             file://Fix-userdata-mount-options.patch;patchdir=${WORKDIR}/git \
                             file://S01-mount-boot.sh\
                             file://K99-move-boot.sh\
                             "

COMPATIBLE_MACHINE_tenderloin = "tenderloin"

do_install_append_tenderloin() {
    install -d ${D}/scripts/local-premount
    install -d ${D}/scripts/local-bottom
    install -m 0755 ${WORKDIR}/S01-mount-boot.sh ${D}/scripts/local-premount/S01-mount-boot.sh
    install -m 0755 ${WORKDIR}/K99-move-boot.sh ${D}/scripts/local-bottom/K99-move-boot.sh
    echo ". /scripts/local-premount/S01-mount-boot.sh" >> ${D}/scripts/local-premount/ORDER
    echo ". /scripts/local-bottom/K99-move-boot.sh" >> ${D}/scripts/local-bottom/ORDER
}

FILES_${PN} += "/scripts/local-premount  /scripts/local-bottom "
