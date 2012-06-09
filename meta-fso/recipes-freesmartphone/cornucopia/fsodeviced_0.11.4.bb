require ${BPN}.inc

PR = "${INC_PR}.2"

SRC_URI[md5sum] = "df1a16d79f860297c766be16dff3c6a0"
SRC_URI[sha256sum] = "6ac0e761160e1b0f04d479828cb59733ec15c032a89cdbde645aea9c0ccb7ecc"

# The following adds support for the Palm Pre device which is not available in development
# anymore, so we need to specify it's configuration here. This can be removed with the
# next release 0.12 of fsogsmd.

CONFFILES_${PN}-config += " ${sysconfdir}/freesmartphone/conf/palm_pre/${PN}.conf"

PACKAGES =+ "${PN}-module-palmpre-quirks ${PN}-module-palmpre-quirks-dev ${PN}-module-palmpre-quirks-dbg"
FILES_${PN}-module-palmpre-quirks = "${CORNUCOPIA_MODULE_DIR}/palmpre_quirks.so"
FILES_${PN}-module-palmpre-quirks-dev = "${CORNUCOPIA_MODULE_DIR}/palmpre_quirks.la"
FILES_${PN}-module-palmpre-quirks-dbg = "${CORNUCOPIA_MODULE_DIR}/.debug/palmpre_quirks*"
