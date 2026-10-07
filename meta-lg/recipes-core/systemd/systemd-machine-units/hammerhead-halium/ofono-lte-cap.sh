#!/bin/sh
# Cap oFono's radio technology preference at LTE.
#
# ofono-binder-plugin starts every modem with TechnologyPreference "nr" and sends the matching
# radio preference (26, NR/LTE/TDSCDMA/GSM/WCDMA) to the RIL. The Android 9 qcril of this device
# knows nothing of 26: it keeps the old value, the plugin reads that back, sees it is not what it
# asked for and asks again, for as long as the modem is up (hundreds of setPreferredNetworkType /
# getPreferredNetworkType pairs a second, each of which can restart the network search). The
# modem then sits at "searching" and the RadioSettings interface is slow to appear.
#
# "lte" (9, LTE/GSM/WCDMA) is accepted and read back as set, which ends the loop.
#
# The preference is stored per SIM, so this has to run whenever the modem shows up with "nr".

ofono_test=/usr/lib/ofono/test
modem=/ril_0

i=0
while [ "$i" -lt 120 ]; do
    pref=$("${ofono_test}/get-tech-preference" "${modem}" 2>/dev/null | sed 's/^.*: //')
    case "${pref}" in
        nr)
            echo "ofono-lte-cap: ${modem} prefers nr, setting lte"
            "${ofono_test}/set-tech-preference" "${modem}" lte
            exit $?
            ;;
        "")
            ;;
        *)
            echo "ofono-lte-cap: ${modem} prefers ${pref}, nothing to do"
            exit 0
            ;;
    esac
    sleep 2
    i=$((i + 2))
done

echo "ofono-lte-cap: ${modem} did not offer RadioSettings in time"
exit 0
