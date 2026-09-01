require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tissot-halium"

PV = "20260901-1"

# webOS-ports/halium-images has two release naming schemes. The old one tagged
# each release with the asset's own filename, so the tag and the file were the
# same string and the URL just repeated it. Newer releases are tagged by date
# and carry several artefacts at once, so the two have to be named separately.
TISSOT_RELEASE ?= "halium-luneos-20260901"

SRC_URI = "https://github.com/webOS-ports/halium-images/releases/download/${TISSOT_RELEASE}/halium-luneos-9.0-${PV}-tissot.tar.bz2"
SRC_URI[sha256sum] = "53ff8c6ed8fc46f50be001f3eefb20990b4e1004a8d5d95ac14001724f686026"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
