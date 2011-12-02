EXTRA_OECONF_append_shr = "\
  --enable-fso \
"

PRINC := "${@int(PRINC) + 1}"
