DESCRIPTION = "Encrypted shellscript for the Zaurus ROM update"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://updater.sh;endline=59;md5=667f442c1e555f21adb58957306612cc"
DEPENDS = "encdec-updater-native"
PR = "r26"

SRC_URI = "file://updater.sh"

S = "${WORKDIR}"

do_configure() {
        sed -i "s/ZAURUS_UPDATER_VERSION/${PR}/" "${S}/updater.sh"
}
do_compile() {
        encdec-updater -e updater.sh
}

# even though the package is not machine-specific
# we have to force it there to allow multimachine builds
PACKAGE_ARCH = "${MACHINE_ARCH}"
PACKAGES = ""


COMPATIBLE_MACHINE = "(poodle|c7x0|spitz|akita|tosa)"

inherit deploy

addtask deploy before do_populate_sysroot after do_compile

do_deploy() {
        install -d ${DEPLOY_DIR_IMAGE}
        install -m 0755 updater.sh ${DEPLOY_DIR_IMAGE}/updater.sh
}
