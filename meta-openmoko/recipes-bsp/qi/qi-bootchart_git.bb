require qi_git.bb

RDEPENDS_${PN} = "bootchart"

do_configure_prepend() {
  sed -i 's#\(IMAGE = .(IMAGE_DIR)/\)qi-\(.(CPU)-.(BUILD_VERSION)\)#\1qi-bootchart-\2#g' ${S}/Makefile
  sed -i 's#\(UDFU_IMAGE = .(IMAGE_DIR)/\)qi-\(.(CPU)-.(BUILD_VERSION).udfu\)#\1qi-bootchart-\2#g' ${S}/Makefile
}

SRC_URI_append = "\
  file://0007-use-bootchart-as-init.patch \
"
