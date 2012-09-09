require shr-image.inc

DEPENDS += "packagegroup-shr "
RDEPENDS_${PN} += "\
    packagegroup-shr-apps \
    packagegroup-shr-games \
    packagegroup-shr-gtk \
    packagegroup-shr-cli \
"

IMAGE_INSTALL += "\
  packagegroup-shr-apps \
  packagegroup-shr-games \
  packagegroup-shr-gtk \
  packagegroup-shr-cli \
"
