PV = "invalid, please override"

# use fso-autorev to set PREFERRED_VERSIONS for cornucopia components
DEFAULT_PREFERENCE = "-1"

FSO_CORNUCOPIA_SRCREV ?= "f2ebc7a1a504fb694b0cd0bfa627ab835196b505"
FSO_CORNUCOPIA_BRANCH ?= "master"
SRC_URI = "${FREESMARTPHONE_GIT}/cornucopia;protocol=git;branch=${FSO_CORNUCOPIA_BRANCH}"
S = "${WORKDIR}/git/${PN}"
