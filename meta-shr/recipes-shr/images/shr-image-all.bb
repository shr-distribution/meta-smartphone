require shr-image.bb

# usefull only for devs and automated builds to force do_rootfs for all packages we have in feed

DEPENDS += "packagegroup-shr-feed"

RDEPENDS_${PN} += "\
    packagegroup-shr-feed \
"

IMAGE_INSTALL += "\
  packagegroup-shr-feed \
"
