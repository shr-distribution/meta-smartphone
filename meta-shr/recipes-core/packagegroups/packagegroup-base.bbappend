RDEPENDS_packagegroup-base-phone = ""
# we don't want
#    gsmd \
#    libgsmd-tools"
PRINC := "${@int(PRINC) + 4}"
