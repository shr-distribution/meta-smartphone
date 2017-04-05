#! /bin/sh
#
# Copyright Matthias Hentges <devel@hentges.net> (c) 2008
# License: MIT (see http://www.opensource.org/licenses/mit-license.php 
#               for a copy of the license)
#
# Filename: openmoko-gps_svn.bb
# Date: 20080101 (YMD)

DESCRIPTION = "A tiny GPS output parser for the Openmoko platform."
HOMEPAGE = "http://forge.bearstech.com/trac/wiki/OpenmokoGPS"
SECTION = "base"
LICENSE = "GPL-2.0+"
LIC_FILES_CHKSUM = "file://gps_reader.py;endline=7;md5=ecdafa461a157727dbc18b5cd12adbdd"

RDEPENDS_${PN} = "python-pygtk"

SRCREV = "12"
PV = "0.0.1+svnr${SRCPV}"
PR = "r2" 

######################################################################################

SRC_URI = "svn://forge.bearstech.com/forge/openmoko;module=gps"

PNBLACKLIST[openmoko-gps] ?= "RDEPENDS on python-pygtk which was removed from oe-core - the recipe will be removed on 2017-09-01 unless the issue is fixed"

S = "${WORKDIR}/gps"

do_compile() {
    # fix hardcoded path
    sed -i "s#/usr/bin/python#env python#g" gps_reader.py
    # fix QA issues
    sed -i "/^Encoding/d; /^SingleInstance/d; s/Categories=GTK;Application;Utilities;/Categories=Utility;/g" openmoko-gps.desktop
}

do_install() {
    install -d ${D}${bindir}/
    install -d ${D}${datadir}/applications/
    install -d ${D}${datadir}/pixmaps/
    
    install -m 0755 ${S}/gps_reader.py ${D}${bindir}/
    install -m 0644 ${S}/openmoko-gps.desktop ${D}${datadir}/applications/
    install -m 0644 ${S}/*.png ${D}${datadir}/pixmaps/
}
