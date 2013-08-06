FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

PRINC := "${@int(PRINC) + 2}"

RDEPENDS_${PN}_append_shr = " fsoraw"
#Second launcher for shr
SRC_URI_append_shr = "file://navitD.png \
                      file://navit.launcher.sh \
"
do_install_append_shr() {
    # Install second launcher for shr
    install -m 0644 ${S}/navit/xpm/desktop_icons/navit.desktop ${D}${datadir}/applications/navitD.desktop
    install -d ${D}${datadir}/icons/hicolor/86x86/apps
    install -m 0644 ${WORKDIR}/navitD.png ${D}${datadir}/icons/hicolor/86x86/apps/navitD.png
    install -m 0755 ${WORKDIR}/navit.launcher.sh ${D}${bindir}/
    # Change .desktop files a bit
    sed -i 's#Exec=navit#Exec=fsoraw -fr CPU,GPS -- navit.launcher.sh#g' ${D}${datadir}/applications/navit.desktop
    sed -i 's#Exec=navit#Exec=fsoraw -fr CPU,GPS,Display -- navit.launcher.sh#g' ${D}${datadir}/applications/navitD.desktop
    sed -i 's#Categories=GTK;Utility;Geography;#Categories=Graphics;#g' ${D}${datadir}/applications/navitD.desktop
    sed -i 's#Comment=GPS Navigation#Comment=GPS Navigation Display blanking disabled#g' ${D}${datadir}/applications/navitD.desktop
    sed -i 's#Icon=navit#Icon=navitD#g' ${D}${datadir}/applications/navitD.desktop
    sed -i 's#Navit#NavitD#g' ${D}${datadir}/applications/navitD.desktop
}
