require ${BPN}.inc

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

SRC_URI += " \
           file://0001-vapi-fix-IPC_SEC_SIM_STATUS_READY-removed-INITIALIZI.patch \
           "

SRC_URI[md5sum] = "6e7fc37de47fbb90d9bca8cfde62a897"
SRC_URI[sha256sum] = "fe9adb389343c060a56fe44454cc789fa7b6a48f7f3fa2cac84aa88c5e87331e"

PR = "r1"
