DESCRIPTION = "Free GPS navigation for car and outdoor with OpenStreetMap maps"
HOMEPAGE = "http://www.gps-routes.info/index.php?name=Content&pa=showpage&pid=1"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=8f0e2cd40e05189ec81232da84bd6e1a"
DEPENDS = "ecore evas imlib2 gpsd edje-native mysql5"

SRC_URI = "http://www.gps-routes.info/debian/pool/main/m/mcnavi/mcnavi_${PV}.tar.gz"
SRC_URI[md5sum] = "9c2f89924b3cc665cc9e3d7e8b3d1d07"
SRC_URI[sha256sum] = "4e4fe21b6ec5d33aea226b402b594eeb293f2d0fdbd822a71c112ea3e23f4c12"

S = "${WORKDIR}/${PN}"

do_configure_prepend() {
  # for some reason our mysql_config returns just sysroot/include_dir while mysql.h is in include_dir/mysql
  sed -i 's#`mysql_config --include`#`mysql_config --include`/mysql#g' ${S}/configure.ac
}

inherit autotools
