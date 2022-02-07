PV = "invalid, please override"

# use fso-autorev to set PREFERRED_VERSIONS for cornucopia components
DEFAULT_PREFERENCE = "-1"

MSMCOMM_SRCREV ?= "5a0fe06c6c7ab435c06c2bd44f5d8cbb97f1bdc7"
MSMCOMM_BRANCH ?= "master"
SRC_URI = "git://github.com/freesmartphone/msmcomm;protocol=https;branch=${MSMCOMM_BRANCH}"
S = "${WORKDIR}/git/${PN}"
