# fix libhybris revision to an older version, as there is a serious memory leak
# in more recent ones, which still needs to be analyzed
SRCREV_tenderloin = "3beca8ad9246e3fce3c47ef978c3b07f6c04284a"

FILESEXTRAPATHS_prepend := "${THISDIR}/${BPN}:"

SRC_URI_append_tenderloin = " \
    file://0001-Force-distro-wayland-instead-of-built-in.patch;patchdir=.. \
    file://0002-include-gles3-headers.patch;patchdir=.. \
    file://0003-add-glesv3-headers.patch;patchdir=.. \
"
