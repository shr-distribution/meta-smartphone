require ${BPN}.inc

PR = "${INC_PR}.1"

SRC_URI[md5sum] = "ca69c62ee24e669457974023c73e6e55"
SRC_URI[sha256sum] = "a6b437be387f2576eca8aef1dbace68c818cc9d6e5cc59a07d981d395006cddb"

FILES_${PN}-config += " \
  ${sysconfdir}/freesmartphone/conf/palm_pre/${PN}.conf \
"
CONFFILES_${PN}-config += " \
  ${sysconfdir}/freesmartphone/conf/palm_pre/${PN}.conf \
"

