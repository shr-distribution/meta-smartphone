FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}"
PRINC = "4"

SRC_URI_append_palmpre = " \
    file://include-cy8mrln-header.patch \
"
EXTRA_OECONF_palmpre = " \
    --enable-shared \
    --enable-cy8mrln-palmpre \
    --enable-input \
    --disable-h3600 \
    --disable-corgi \
    --disable-collie \
    --disable-mk712 \
    --disable-artic2 \
    --disable-ucb1x00 \
    --disable-tatung \
"
