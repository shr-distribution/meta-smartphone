require ${BPN}.inc

PR = "${INC_PR}.0"

SRC_URI[md5sum] = "c1065ef070e3b1fa28dbf71d95dc1f98"
SRC_URI[sha256sum] = "f72637985dfd26c4a571d2e6759cefe3857c8fcdddd90b5d8ffba5a78852fa38"

EXTRA_OECONF += "--enable-vala"
do_configure_prepend() {
    # force re-creation with new gee
    rm -f ${S}/src/lib/lowlevel.c ${S}/src/lib/fsousage.h
}

#   5065  rm ../fsousaged-0.12.0/src/plugins/dbus_service/commands.c 
#    5066  rm ../fsousaged-0.12.0/src/plugins/dbus_service/resource.c
#     5067  rm ../fsousaged-0.12.0/src/plugins/dbus_service/plugin.c

SRC_URI += "file://0001-fsousaged-upgrade-to-gee-0.8.patch"

PNBLACKLIST[fsousaged] ?= "Depends on blacklisted libfso-glib"
