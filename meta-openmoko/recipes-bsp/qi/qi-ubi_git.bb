require qi.inc

do_configure_prepend() {
  sed -i 's#\(IMAGE = .(IMAGE_DIR)/\)qi-\(.(CPU)-.(BUILD_VERSION)\)#\1qi-ubi-\2#g' ${S}/Makefile
  sed -i 's#\(UDFU_IMAGE = .(IMAGE_DIR)/\)qi-\(.(CPU)-.(BUILD_VERSION).udfu\)#\1qi-ubi-\2#g' ${S}/Makefile
}
