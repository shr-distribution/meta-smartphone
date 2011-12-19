require shr-image.inc

DEPENDS += "task-shr "
RDEPENDS_${PN} += "\
    task-shr-apps \
    task-shr-games \
    task-shr-gtk \
    task-shr-cli \
"

IMAGE_INSTALL += "\
  task-shr-apps \
  task-shr-games \
  task-shr-gtk \
  task-shr-cli \
"
