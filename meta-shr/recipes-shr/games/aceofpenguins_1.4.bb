DESCRIPTION = "The Ace of Penguins is a set of Unix/X solitaire games based on the ones available for Windows(tm) but with a number of enhancements that my wife says make my versions better :-) \
The latest version includes clones of freecell, golf, mastermind, merlin, minesweeper, pegged, solitaire, taipei (with editor!), and thornq (by Martin Thornquist)."
AUTHOR = "dj@delorie.com"
HOMEPAGE = "http://www.delorie.com/store/ace/"
SECTION = "games"
DEPENDS = "libpng-native libpng zlib-native libxpm"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=9836c6a6315b77f2a3c270058e6d96a9"

SRC_URI = "\
  http://www.delorie.com/store/ace/ace-${PV}.tar.gz\
  file://fix-crosscompile.patch \
  file://libpng15.patch \
"
SRC_URI[md5sum] = "b80169fa59d69758bb9686f31a84ad2b"
SRC_URI[sha256sum] = "1fee3b0533921a42873c0623f33d873a067b1eec58f2c70c01449146e79a4cce"

S = "${WORKDIR}/ace-${PV}"

inherit autotools

# http://errors.yoctoproject.org/Errors/Details/81006/
PNBLACKLIST[aceofpenguins] ?= "BROKEN: fails to build with new binutils-2.27 - the recipe will be removed on 2017-09-01 unless the issue is fixed"
