PV = "invalid, please override"

FSO_CORNUCOPIA_SRCREV ?= "1319aa34f58388a1d9e66b13141776191f465d3a"
FSO_CORNUCOPIA_BRANCH ?= "master"
SRC_URI = "${FREESMARTPHONE_GIT}/cornucopia;protocol=git;branch=${FSO_CORNUCOPIA_BRANCH}"
S = "${WORKDIR}/git/${PN}"
