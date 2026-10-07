FILESEXTRAPATHS:prepend := "${THISDIR}/org.webosports.app.camera:"

# The SM-T520's cameras are upright in landscape: see the patch.
SRC_URI:append:sm-t520 = " file://0001-PreferencesModel-the-SM-T520-sensors-are-mounted-upright.patch"
