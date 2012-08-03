require shr-image.bb

# usefull only for devs and automated builds to force do_rootfs for all packages we have in feed

DEPENDS += "task-shr-feed"

RDEPENDS_${PN} += "\
    task-shr-feed \
"

IMAGE_INSTALL += "\
  task-shr-feed \
"
