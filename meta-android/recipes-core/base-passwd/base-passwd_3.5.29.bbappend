FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# Need to change the IDs of input and audio group to match those from
# the android system
SRC_URI:append = " file://change-android-group-ids.patch;striplevel=0"
