require shr-image.inc

IMAGE_BASENAME = "full"

DEPENDS += "task-shr task-jama-shr "
RDEPENDS_${PN} += "\
    task-shr-apps \
    task-shr-games \
    task-shr-gtk \
    task-shr-cli \
    task-jama-shr \
"

IMAGE_INSTALL += "\
  task-shr-apps \
  task-shr-games \
  task-shr-gtk \
  task-shr-cli \
  task-jama-shr \
"
