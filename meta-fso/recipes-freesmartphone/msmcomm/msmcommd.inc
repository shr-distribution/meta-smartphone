require msmcomm-base.inc

DEPENDS = " \
 vala-native \
 glib-2.0 \
 libfso-glib \
 libfsoframework \
 libgee \
 libmsmcomm \
 libmsmhll \
 msmcommd-specs \
"

SRC_URI += "file://msmcommd"

inherit autotools vala update-rc.d

INITSCRIPT_NAME = "msmcommd"
INITSCRIPT_PARAMS = "defaults 28"

do_install_append() {
    install -d ${D}${sysconfdir}/init.d/
    install -m 0755 ${WORKDIR}/msmcommd ${D}${sysconfdir}/init.d/
}

FILES_${PN} += "\
  ${sysconfdir} \
  ${datadir}/dbus-1 \
"
