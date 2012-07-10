RDEPENDS_task-base-phone = ""
# we don't want
#    gsmd \
#    libgsmd-tools"
PRINC := "${@int(PRINC) + 3}"
