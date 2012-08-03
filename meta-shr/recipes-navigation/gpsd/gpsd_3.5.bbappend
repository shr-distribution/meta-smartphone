INITSCRIPT_NAME = "gpsd-sysv"
do_install_append() {
    mv ${D}${sysconfdir}/init.d/gpsd ${D}${sysconfdir}/init.d/gpsd-sysv
}

PRINC := "${@int(PRINC) + 1}"
