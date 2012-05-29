PV = "invalid, please override"

# use fso-autorev to set PREFERRED_VERSIONS for cornucopia components
DEFAULT_PREFERENCE = "-1"

FSO_CORNUCOPIA_SRCREV ?= "32a89a97cdbd86300ed70e23f7f94b9b71d36d45"
FSO_CORNUCOPIA_BRANCH ?= "master"
SRC_URI = "${FREESMARTPHONE_GIT}/cornucopia;protocol=git;branch=${FSO_CORNUCOPIA_BRANCH}"
S = "${WORKDIR}/git/${PN}"
