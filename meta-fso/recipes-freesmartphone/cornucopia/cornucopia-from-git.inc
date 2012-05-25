PV = "invalid, please override"

# use fso-autorev to set PREFERRED_VERSIONS for cornucopia components
DEFAULT_PREFERENCE = "-1"

FSO_CORNUCOPIA_SRCREV ?= "22a6e018a2d6fba7db483a0ef2843bf201d22e08"
FSO_CORNUCOPIA_BRANCH ?= "master"
SRC_URI = "${FREESMARTPHONE_GIT}/cornucopia;protocol=git;branch=${FSO_CORNUCOPIA_BRANCH}"
S = "${WORKDIR}/git/${PN}"
