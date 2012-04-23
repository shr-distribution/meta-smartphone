PV = "invalid, please override"

# use fso-autorev to set PREFERRED_VERSIONS for cornucopia components
DEFAULT_PREFERENCE = "-1"

MSMCOMM_SRCREV ?= "ec3ba8e5fdca377a914412dee2327fb281343e43"
MSMCOMM_BRANCH ?= "master"
SRC_URI = "${FREESMARTPHONE_GIT}/msmcomm;protocol=git;branch=${MSMCOMM_BRANCH}"
S = "${WORKDIR}/git/${PN}"
