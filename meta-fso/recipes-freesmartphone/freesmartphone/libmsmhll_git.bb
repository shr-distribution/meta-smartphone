require msmcomm.inc

PR = "${INC_PR}.2"
PV = "0.10.0+gitr${SRCPV}"

S = "${WORKDIR}/git/libmsmhll"

DEPENDS = " \
  vala-native \
  glib-2.0 \
  libgee \
"

inherit autotools vala
