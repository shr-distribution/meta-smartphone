PRINC := "${@int(PRINC) + 1}"
PACKAGECONFIG ??= "${@base_contains('VIRTUAL-RUNTIME_init_manager', 'sysvinit', 'consolekit', 'systemd', d)}"
