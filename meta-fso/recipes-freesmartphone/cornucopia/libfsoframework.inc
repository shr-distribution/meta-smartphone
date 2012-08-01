require cornucopia-base.inc
DESCRIPTION = "freesmartphone.org support library"

DEPENDS += "alsa-lib libgsm0710 libnl"

PACKAGES =. " \
  libfsobasics libfsobasics-dev libfsobasics-dbg \
  libfsoresource libfsoresource-dev libfsoresource-dbg \
  libfsosystem libfsosystem-dev libfsosystem-dbg \
  libfsotransport libfsotransport-dev libfsotransport-dbg \
  libfsotest libfsotest-dev libfsotest-dbg \
  libgsm0710mux libgsm0710mux-dev libgsm0710mux-dbg \
"

FILES_libfsobasics += "${libdir}/libfsobasics*${SOLIBS}"
FILES_libfsobasics-dev += "${libdir}/libfsobasics*${SOLIBSDEV} \
  ${libdir}/libfsobasics*la \
  ${datadir}/vala/vapi/fsobasics* \
  ${libdir}/pkgconfig/fsobasics* \
"
FILES_libfsobasics-dbg += "${libdir}/.debug/libfsobasics*"

FILES_libfsoresource += "${libdir}/libfsoresource*${SOLIBS}"
FILES_libfsoresource-dev += "${libdir}/libfsoresource*${SOLIBSDEV} \
  ${libdir}/libfsoresource*la \
  ${datadir}/vala/vapi/fsoresource* \
  ${libdir}/pkgconfig/fsoresource* \
"
FILES_libfsoresource-dbg += "${libdir}/.debug/libfsoresource*"

FILES_libfsosystem += "${libdir}/libfsosystem*${SOLIBS}"
FILES_libfsosystem-dev += "${libdir}/libfsosystem*${SOLIBSDEV} \
  ${libdir}/libfsosystem*la \
  ${datadir}/vala/vapi/fsosystem* \
  ${libdir}/pkgconfig/fsosystem* \
"
FILES_libfsosystem-dbg += "${libdir}/.debug/libfsosystem*"

FILES_libfsotransport += "${libdir}/libfsotransport*${SOLIBS}"
FILES_libfsotransport-dev += "${libdir}/libfsotransport*${SOLIBSDEV} \
  ${libdir}/libfsotransport*la \
  ${datadir}/vala/vapi/fsotransport* \
  ${libdir}/pkgconfig/fsotransport* \
"
FILES_libfsotransport-dbg += "${libdir}/.debug/libfsotransport*"

FILES_libfsotest += "${libdir}/libfsotest*${SOLIBS} \
  ${libdir}/cornucopia/modules/tests/plugin* \
"
FILES_libfsotest-dev += "${libdir}/libfsotest*${SOLIBSDEV} \
  ${libdir}/libfsotest*la \
  ${datadir}/vala/vapi/fsotest* \
  ${libdir}/pkgconfig/fsotest* \
"
FILES_libfsotest-dbg += "${libdir}/.debug/libfsotest* \
  ${libdir}/cornucopia/modules/tests/.debug/* \
"

FILES_libgsm0710mux += "${libdir}/libgsm0710mux*${SOLIBS}"
FILES_libgsm0710mux-dev += "${libdir}/libgsm0710mux*${SOLIBSDEV} \
  ${libdir}/libgsm0710mux*la \
  ${datadir}/vala/vapi/gsm0710mux* \
  ${libdir}/pkgconfig/gsm0710mux* \
"
FILES_libgsm0710mux-dbg += "${libdir}/.debug/libgsm0710mux*"
