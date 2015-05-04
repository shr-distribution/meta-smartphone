PV = "invalid, please override"

# use fso-autorev to set PREFERRED_VERSIONS for cornucopia components
# use git recipes by default now, because they are compatible with libgee-0.8
# released tarballs are compatible only with older libgee-1.0
DEFAULT_PREFERENCE = "-1"

FSO_CORNUCOPIA_SRCREV ?= "07c6fdede0870edc37a8d51d033b6e7e29aa7c91"
FSO_CORNUCOPIA_BRANCH ?= "master"
SRC_URI = "git://github.com/freesmartphone/cornucopia.git;protocol=https;branch=${FSO_CORNUCOPIA_BRANCH}"
S = "${WORKDIR}/git/${PN}"
