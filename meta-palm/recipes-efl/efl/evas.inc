# Neon support for evas is broken on the palmpre and as long as it is not fixed it's
# disabled.
EVAS_CPU_TWEAKS_palmpre = "--disable-cpu-neon"

PRINC := "${@int(PRINC) + 1}"
