PV = "invalid, please override"

# use fso-autorev to set PREFERRED_VERSIONS for cornucopia components
DEFAULT_PREFERENCE = "-1"

FSO_CORNUCOPIA_SRCREV ?= "9c695dd15e712aa8a176bf491a0383a6c8c9221a"
FSO_CORNUCOPIA_BRANCH ?= "master"
SRC_URI = "${FREESMARTPHONE_GIT}/cornucopia;protocol=git;branch=${FSO_CORNUCOPIA_BRANCH}"
S = "${WORKDIR}/git/${PN}"
