require ${BPN}.inc

# On the palmpre we need the msmcomm daemon to talk to the modem
RDEPENDS_${PN}_palmpre += "msmcommd"

## palmpre
#  modem_qualcomm_pre.so
#  lowlevel_palmpre.so
RDEPENDS_${PN}_palmpre += "fsogsmd-module-lowlevel-palmpre"
RDEPENDS_${PN}_palmpre += "fsogsmd-module-modem-qualcomm-palm"


