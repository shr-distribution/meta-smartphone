require ${BPN}.inc

PR = "r2"

SRC_URI[md5sum] = "4aa35c7ec47014bdb001823eb4e083b9"
SRC_URI[sha256sum] = "786b7a30eeaa87062ce51d3dd49d7d12f36842f3f30471cc4c8f09de40e244bb"

PNBLACKLIST[libfso-glib] ?= "Depends on blacklisted vala-dbus-binding-tool"
