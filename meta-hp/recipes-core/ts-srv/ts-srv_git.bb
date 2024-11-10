DESCRIPTION = "Userspace touchscreen driver for the HP TouchPad"
LICENSE = "GPL-3.0-or-later"
LIC_FILES_CHKSUM = " file://${COMMON_LICENSE_DIR}/GPL-3.0-or-later;md5=1c76c4cc354acaac30ed4d5eefea7245"

PV = "0.1.0+git"
SRCREV = "af457915f04ec92cd1e29e1162082090890a0771"

COMPATIBLE_MACHINE = "tenderloin"
PACKAGE_ARCH:tenderloin = "${MACHINE_ARCH}"

SRC_URI = "git://github.com/Evervolv/android_device_hp_tenderloin-common.git;protocol=https;branch=p-9.0;subpath=hardware/touchscreen \
           file://0001-remove-android-dependencies-and-fix-warnings.patch;striplevel=3 \
"
S = "${WORKDIR}/touchscreen"

do_compile() {
	${CC} -Wall -lm -g ${LDFLAGS} -o ts_srv digitizer.c ts_srv.c
	${CC} -Wall -g ${LDFLAGS} -o ts_srv_set ts_srv_set.c
}

do_install() {
    install -d ${D}${sbindir}
	install -m 0755 ${S}/ts_srv ${D}${sbindir}/ts_srv
	install -m 0755 ${S}/ts_srv_set ${D}${sbindir}/ts_srv_set
}

FILES:${PN} = "${sbindir} ${confdir}"
