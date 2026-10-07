# Samsung Galaxy A3 (2015) — LuneOS mainline port status

Machine `a3-2015`, adaptation `a3u-eur`, kernel `linux-samsung-a3-2015`
(msm8916-mainline 7.3-rc2). Written at the end of the session of 2026-09-30.

---

## Flash this

```
tmp/deploy/images/a3-2015/lk2nd-boot-a3-2015-NEW.img
```

lk2nd truncated to 512K with the boot image appended, same construction as the
image flashed previously. Odin or heimdall, as before.

**This flash is required**, not optional. Three of this session's features are
device tree nodes — the flash LED, the light sensor and the rear camera — and a
DTB lives inside the boot image. Dropping modules onto the rootfs cannot add DT
nodes, which is exactly why the torch stayed missing earlier: the module was in
place and the kernel was still printing
`rt5033-led: Failed to locate of_node [id: -1]`.

### First three checks after flashing

```sh
ls /sys/class/leds                      # expect an rt5033 flash entry
ls /sys/bus/iio/devices                 # expect the tmd3782
dmesg | grep -iE "camss|imx219|s5k5e3"  # camera probe
```

The light sensor check is genuinely informative either way. The TMD3782 was
identified from the **A5** vendor DT and I never confirmed the A3 populates it.
The driver reads `CHIPID` and refuses to probe unless it reads `0x69`, so a
clean rejection in dmesg means the part is absent, not that the driver is
broken.

---

## Patches (all in `SRC_URI`, all building clean)

| # | What |
|---|------|
| 0001 | `drm/panel s6e88a0-ams452ef01` — backlight with AOR+ELVSS brightness |
| 0002 | `rt5033_charger` — host-port input current cap |
| 0003 | `leds/flash` — new Richtek RT5033 flash LED driver |
| 0004 | dts a2015 — flash LED node |
| 0005 | `iio/light` — new AMS/TAOS TMD3782 driver |
| 0006 | dts a2015 — light sensor node |
| 0007 | dts a3u-eur — rear camera (IMX219 + CAMSS + CCI) |
| 0008 | dts a2015 — board thermistor VADC channel |
| 0009 | `media/i2c` — new Samsung S5K5E3YX sensor driver |

Config added: `LEDS_RT5033`, `TMD3782`, `VIDEO_IMX219`, `VIDEO_S5K5E3`,
`V4L2_CCI_I2C`, `SENSORS_IIO_HWMON` (all `=m`).

Userspace: `luneos-device-config` `a3u-eur/deviceinfo` gained the hall sensor.

---

## Display brightness — done, with a known limit

The panel had no backlight device at all, so the screen turned off on idle and
never came back. It now registers one and dims by walking the vendor's 62-level
brightness ladder (5 → 360 cd/m²), writing a vendor AOR value (`0xb2`) and a
vendor ELVSS value (`0xb6`) per step, taken from
`samsung,aid_map_table_revA` and `samsung,smart_acl_elvss_map_table_revA`.

Three things learned the hard way here, all recorded in the patch so they are
not re-learned:

- **Gamma must be retransmitted with AOR.** The panel will not latch a new AOR
  unless gamma is rewritten in the same transaction. `0xb2` plus `0xf7` alone is
  accepted without error and silently does nothing until the next enable —
  which presents as brightness that only changes after the screen has been off.
- **ELVSS is not optional.** Between roughly 64 and 162 cd/m² the vendor holds
  AOR fixed at index 33 and varies only ELVSS. An AOR-only implementation is
  therefore flat across the middle of the slider by construction.
- **Writes are coalesced.** `update_status` records the wanted step and rearms
  an 80 ms delayed work, so a slider drag produces one panel write instead of
  dozens. This removed roughly 80% of the flicker.

Remaining: some flicker persists when adjusting. The ~45-byte gamma+AOR
transaction inherently lands mid-frame on a burst-mode video panel, it cannot be
shortened (see above), it is already in low-power mode, and closing the rest
would need vblank synchronisation — which no panel driver in the tree does and
msm's DSI host offers no hook for. Left as-is by agreement.

---

## USB charging — unresolved, and it is not the driver

**Status: charges from a wall charger, does not charge from a PC port.**
Measured: 2h22m continuously on a host port went 39% → 0%, with `online=1`,
`status=Discharging`, `charge_type=N/A` throughout.

Every register the driver programs is byte-identical between the wall-charger
case that works and the host-port case that does not:

| | AICR (0x01) | MIVR (0x05) | 0x02/03/04 | result |
|---|---|---|---|---|
| wall charger | `5c` = 500 mA | `09` = off | `70 86 78` | Charging/Fast |
| PC port | `5c` = 500 mA | `09` = off | `70 86 78` | Discharging |

Excluded, each by direct measurement rather than reasoning:

- **MIVR** — disabled in both cases. 4600 → 4400 → disabled all made no
  difference to the host-port case.
- **AICR** — identical in both. 2000 mA is genuinely wrong for a host port and
  is what tripped the hub (`usb usb3-port4: disabled by hub (EMI?)`), so patch
  0002 caps it, but capping it does not make charging work.
- **Charger vs boost mode** — `0x01` bit 0 is 0, i.e. `RT5033_CHARGER_MODE`.
  Correct.
- **HZ** (input suspend) clear, **TE** enabled.
- **sm5502 MUIC VBUS path** — tested live over i2c. `MANUAL_SW1` `0x27` → `0x25`
  put VBUSIN on the same path the DCP case uses; charging did not start. The
  `CONTROL` `MANUAL_SW` bit will not set (reads back unchanged), so that half is
  untested rather than excluded.
- **Vendor driver fixups** — the vendor has two writes mainline lacks, both
  commented as bug workarounds, and **both are already satisfied** on our
  hardware: `UUG` (0x19) needs `(v & ~0x02) == 0x45` and reads `0x47`;
  `CHG_STAT_CTRL` (0x00) needs `(v & 0x82) == 0x02` and reads `0x46`/`0x66`.
  Also noted: mainline treats `0x00` as read-only status while the vendor also
  writes `CHGENB` there — but that bit is clear (enabled) in both our cases.

I was wrong about this four times across the session (MIVR, then AICR, then a
system-load budget, then the MUIC), each time corrected by measurement. I am
not offering a fifth theory.

**The one variable never changed is the physical supply.** The port in use was
over-currented and disabled by the hub earlier in the same session, which makes
it the worst possible control. **Next step: a different PC port, or a different
cable, before any more code.**

---

## Cameras

**Rear (Sony IMX219) — wired, untested.** Mainline already has `imx219.c`; this
is the Raspberry Pi Camera v2 sensor. The DT wiring came from a reference
implementation for this exact board
(`ghosthgytop/msm8916-mainline_linux`, `msm8916-samsung-a3u-eur.dts`), adapted
to current mainline naming. That reference supplied several values inference
would have got wrong — i2c `0x1a` not `0x10`, `clock-frequency` 23880000 not
24000000, and the CSIPHY lane mapping and 456 MHz link frequency.

It also resolved something that was sitting in our own tree unread: the empty
`LDO {}` and `BUCK {}` nodes in `msm8916-samsung-a2015-common.dtsi`, commented
*"Needed for camera, but not used yet"*, **are** the sensor's 2.8 V analogue and
1.2 V core rails. They now have voltages. `pm8916_l16` had to be declared too —
each board declares the pm8916 rails it uses, and a2015 only had `l17`.

**Front (Samsung S5K5E3YX) — driver written, no DT, untested.** No mainline
driver existed; patch 0009 adds one, 903 lines, modelled on `imx219.c` because
the sensor uses the standard SMIA/MIPI register layout. All device-specific data
is from the vendor driver (`b52_camera/s5k5e3.[ch]`): the 143-register init
sequence, chip ID `0x5E30`, and two modes (2576×1932, 1280×960). Clocking was
derived and cross-checks against the vendor's stated ~179 MHz pixel clock:
26 MHz EXTCLK, pre-PLL ÷6, ×204 → 884 Mbps/lane, 2 lanes at 10 bpp →
176.8 Mpx/s behind a 442 MHz link.

Trap recorded in the commit message: the vendor's separate 5 MP table is
**entirely `#if 0`'d out**. The global init table *is* the full-resolution
configuration, which is why the full mode carries no registers of its own.

### Front camera: CSID1 could never power up (upstream bug, patch 0010)

`STREAMON` on the front pipeline failed with only
`qcom-camss 1b0ac00.camss: Failed to power up pipeline: -22`. Return probes
(kretprobes via tracefs) on the power path showed `csiphy_set_power()` returning
0 and then `csid_set_power()` returning -22 before touching any clock.

Cause: `csid_set_power()` first powers "its" VFE, the one with the CSID's index -
an SDM845-era ordering requirement applied to every SoC - and
`vfe_parent_dev_ops_get()` returns -EINVAL for an index with no VFE. msm8916 has
two CSIDs and one VFE, so **CSID1 can never power up on mainline msm8916**, and
it fails silently. Patch 0010 returns 0 for those indices, matching
`vfe_parent_dev_ops_put()`. Other SoCs with more CSIDs than VFEs are likely
affected the same way.

Proof that it is only CSID1: the same front sensor streams six clean frames
through `csiphy1 -> csid0 -> ispif0 -> vfe0_rdi0`, and the developed frame is a
real optical image (dim, peak 133/1023, since there is no auto-exposure in a raw
capture). The s5k5e3 driver, written blind from vendor registers, works.

Side notes from the same session:

- `csiphy0 -> csid1` returning `EBUSY` is CAMSS refusing a second link from a
  source pad that already has one - here the rear branch was enabled. Not a fault.
- The format is `SGRBG10_1X10/2576x1932`; the matching video-node format is
  `pgAA`, and CAMSS pads the stride to 3224 bytes (2576 px x 10 bit = 3220).

### Sequencing advice

Prove the **rear** camera first. It has a mainline driver and a reference DT.
No msm8916 board upstream has a camera sensor wired at all, so CAMSS on this
SoC is itself unproven — chasing the front camera first means debugging two
unknowns simultaneously. The front camera additionally needs `cci_i2c1` and
CSIPHY1 wiring for which **no reference exists**, and rail/GPIO inference in
that exact area is what went wrong before the reference corrected it.

---

## Sensors and misc

- **TMD3782 light + proximity** — new driver, patch 0005. Register layout is
  TCS3472 plus proximity (`PRX_TIME`, proximity thresholds, `PRX_CFG`,
  `PRX_COUNT`, proximity data/offset), so mainline's `tcs3472` cannot drive it
  and `tsl2772` covers the tmd2xxx parts, not this one. Exposes four raw
  intensity channels and proximity. **Deliberately no illuminance channel**:
  lux needs per-board coefficients the vendor keeps in DT, and only the A5's
  were available — reporting lux computed from another board's coefficients
  would be worse than reporting none.
- **Flash LED** — new driver, patch 0003. The MFD has always registered an
  `rt5033-led` cell but no driver ever existed for it. Register semantics from
  the vendor driver, cross-checked against this device's own reset values
  (`0x0f` → 544 ms strobe timeout and `0x12` → 500 mA both match the vendor
  platform defaults). Two non-obvious behaviours are encoded: mode selection
  only latches on an off→on transition of `FUNCTION2`'s enable bit, and turning
  off requires dropping strobe before enable with a settle between or the output
  stays latched on.
- **Board thermistor** — patch 0008 adds the `P_MUX4_1_1` VADC channel as
  `ap_therm`, following the identical declaration in `pm8950.dtsi`.
  **No thermal zone**, on purpose: the vendor's lookup table is in raw
  qpnp-vadc codes and mainline reports processed microvolts, so any table
  written now would be invented. With `SENSORS_IIO_HWMON` the raw channel is
  readable — sample it at known temperatures and build a real table, then
  `generic-adc-thermal` (precedent: `sm7225-fairphone-fp4.dts`).
- **Battery temperature is still unavailable.** `rt5033_battery` exposes only
  `STATUS`, `VOLTAGE_NOW/AVG/OCV`, `PRESENT`, `CAPACITY` — no `TEMP` — and the
  thermistor above is a board sensor, not the pack NTC.
- **Hall sensor** — needed no kernel work. It was already in the DT as a second
  `gpio-keys` node (`tlmm 52`, `EV_SW`/`SW_LID`). It was missing from
  `deviceinfo_key_devices_by_name`, so nyx never opened it; now added. It needs
  naming separately because it is its own node, not part of `"GPIO Buttons"`.

### Audit correction

My earlier "what else is missing" list was mostly wrong and is superseded by
this. I diffed vendor `compatible` strings against mainline ones while filtering
out `gpio-keys` as generic, which hid devices that *are* supported, then guessed
at the remaining vendor names instead of checking. Of four claimed gaps, three
were already implemented:

- hall sensor — already present via `gpio-keys`
- headset jack (`sec_jack`) — already present, `pm8916_codec` has
  `jack-gpios = <&tlmm 110>` plus MBHC thresholds
- `abov,mc96ft16xx` — **not** an IR blaster; it is the touchkey, and our DT says
  so: *"Actually an ABOV MCU that implements same interface"*, driven by
  `coreriver,tc360-touchkey`
- thermistor — genuinely missing, now partly addressed

---

## Telephony — the modem is fine, ofono cannot reach it

The modem boots and its **entire telephony stack is alive**. It is simply not on
the transport ofono looks at.

msm8916 splits QMI across two transports:

| transport | what it carries |
|---|---|
| **QRTR** (`qrtr-lookup`) | platform services only — thermal, time, PDC, RF RPE, SAR, coex, PDS/location, DSD, DFS, subsystem control, test |
| **`/dev/wwan0qmi0`** (rpmsg/SMD ctrl port) | the whole telephony stack |

So `qrtr-lookup` shows a healthy modem at node 0 with **no DMS/WDS/NAS/UIM/VOICE/WMS**
and that looks like a dead modem. It is not. Asking the port directly:

```
# qmicli -d /dev/wwan0qmi0 --get-service-version-info
wds (1.36)  dms (1.14)  nas (1.25)  wms (1.10)  voice (2.1)  uim (1.36)  cat2 (2.24)
# qmicli -d /dev/wwan0qmi0 --dms-get-ids
IMEI: 356713073856542
```

A `ctl` service in that list proves the port is QMUX-framed. Node 1 in
`qrtr-lookup` is the AP (rmtfs + DHMS), node 7 is WCNSS.

### Why ofono still finds nothing

ofono 2.19 has two QMI paths and neither fits:

- **`qrtrqmi` / `qrtrsoc`** scan QRTR for DMS/NAS, which genuinely are not there.
  `udevng.c:check_net_device()` only builds a `qrtrsoc` modem for netdevs whose
  name starts with `rmnet_`; bam-dmux gives us `wwan0`..`wwan7`.
- **`gobi`** speaks exactly the right protocol — `gobi_enable()` just needs
  `Device` and calls `qmi_qmux_device_new()` — but `gobi_probe()` rejects us:

  ```c
  if (!L_IN_STRSET(if_driver, "qmi_wwan"))  return -ENOTSUP;
  if (!L_IN_STRSET(bus, "usb"))             return -ENOTSUP;
  ```

  plus a mandatory `InterfaceNumber` and a qmi_wwan-only `pass_through` sysfs node.

And a `ENV{OFONO_DRIVER}="gobi"` udev rule on the wwan port is never consulted at
all, because `udevng.c:check_wwan_device()` bails before reading any properties
unless the port has an **MHI** parent (PCIe modems); ours is rpmsg-parented:

```c
parent = udev_device_get_parent_with_subsystem_devtype(device, "mhi", NULL);
if (!parent)
        return;
```

**Conclusion:** this is an ofono porting job, not a config job — it needs an
embedded/QMUX bus type in `gobi` (skipping QMAP/pass-through, since bam-dmux
already gives one netdev per channel) and a `check_wwan_device()` branch for
rpmsg-parented ports. ModemManager would handle this port as-is; ofono will not.

### Where the dead /ril_0 comes from

oFono reported exactly one modem, `/ril_0`, permanently `Powered=false`
`Online=false`. That is not a failed detection of the real modem - it is a
phantom, and it has nothing to do with QMI.

`OFONO_RIL_DEVICE` is read by oFono itself in `plugins/rildev.c`:

```c
ril_type = getenv("OFONO_RIL_DEVICE");
if (ril_type == NULL)
        return 0;              /* nothing is created */
...
create_rilmodem(ril_type, i);  /* else creates and registers ril_<slot> */
```

The shared `ofono-conf/environment.conf` sets it to `ril`, and the oFono bbappend
only passes `--disable-rilmodem` for `:halium`, so on a mainline build the `ril`
driver genuinely exists and the modem registers. Hence a dead `/ril_0` and nothing
else - which reads like "oFono cannot see the modem" for the wrong reason.

Fixed with a machine override, `ofono-conf/a3-2015/environment.conf`, that simply
omits `OFONO_RIL_DEVICE`; `rildev` then creates nothing. (pinephone, pinephonepro
and pinetab2 set it to `qmimodem` instead, which suppresses the modem only as a
side effect: that is a driver-directory name, not a modem plugin name, so
registration fails.)

Note `OFONO_RIL_DEVICE` is set in four of our `environment.conf` files and read
nowhere in meta-webos-ports or meta-smartphone - the consumer is oFono itself, so
grepping only our layers makes it look vestigial when it is not.

### SIM and radio — working

The earlier `no-atr-received` was an empty slot. With a Vodafone NL SIM in:

- **Hot-insert is not detected**; the modem only probes the card when it boots.
  Restart the modem (`remoteproc0/state` stop/start) or reboot.
- After a modem restart DMS can sit in operating mode `shutting-down`;
  `qmicli --dms-set-operating-mode=online` brings the radio up.
- PIN verified over QMI, USIM application `ready`, IMSI 204-04 read back.
- The radio **receives**: it camped on a real UMTS cell (204/16, WCDMA-900,
  channel 3087) and was `registration-denied` - correct, wrong operator for a
  Vodafone SIM. Vodafone coverage at the test location is poor, so not
  registering there is coverage, not the port. Retest somewhere with signal.

### oFono support — patch 0009

`0009-gobi-support-embedded-QMUX-modems-on-BAM-DMUX-SoCs.patch` (meta-luneos
ofono bbappend) is the porting job described above: udevng groups the QMI port
with BAM-DMUX channel 0 (`dev_port` 0) under the remoteproc platform device as an
embedded `gobi` modem, keyed on bam-dmux so qrtrsoc platforms are untouched; gobi
accepts `Bus=embedded` with `bam-dmux` and keeps qmi_wwan's sysfs knobs and the
WDA negotiation to qmi_wwan. Data uses gobi's existing single-context path bound
to `wwan0`. Built but **not yet tested on the device**.

### Dead ends — already excluded, do not redo

- **qrtr-ns start order** — enabled and started at boot, before the modem is up.
- **rmtfs / EFS** — registers service 14 on node 1; all of `efs`, `fsc`, `fsg`,
  `modemst1`, `modemst2` present. Running `rmtfs -v` showed only
  `sendto(): Connection reset by peer`, and that was the modem being torn down by
  hand. QRTR has exactly one `ECONNRESET` site (`net/qrtr/af_qrtr.c`) meaning
  "destination node gone"; the follow-on `Operation not permitted` is a stale
  errno, not a real fault.
- **Firmware** — mba/mpss authenticate and boot; `modem_pr` resolves and holds
  `mcfg`. The `ln` error in the log is a harmless retry on a read-only mount.
- **PD mapper** — the absent service 47/64 is correct: `qcom_pd_mapper.c` lists
  `qcom,msm8916` with `.data = NULL`, so upstream says this SoC needs no domains.
- **Modem crashes** — none; clean boot, `wwan0qmi0 attached`.

## Working before this session

Display/DRM/Adreno, touch, all physical keys, WiFi, modem, audio (UCM + speaker
sink), accelerometer and magnetometer with correct mount matrix, vibrator, GPU,
video decode, SD, USB gadget, NFC, screen blanking.

## Open

1. **Telephony** — modem, SIM and radio proven over QMI. oFono patch 0009 and
   the `/ril_0` fix need deploying and testing; registration needs a spot with
   Vodafone coverage. Data (gprs context on `wwan0`) is the least certain part.
2. **USB charging** — needs a different port or cable, not code.
3. **Front camera in the app** — sensor, driver and PHY are proven working:
   streamed and developed a real frame through `csiphy1 -> csid0`. Routing it
   through `csid1` failed with a silent `Failed to power up pipeline: -22`,
   which was an upstream CAMSS bug, fixed by patch 0010 (see the cameras
   section). Needs the 0010 kernel flashed, then a retest in the app.
4. **Proximity** — `PS_DATA` reads constant. Emitter polarity has been reverted
   to `GPIO_ACTIVE_HIGH` (needs a rebuild + flash to test). Second known issue:
   the vendor driver takes near/far from the **IRQ pin** against `PS_THD`
   thresholds rather than polling `PS_DATA` as our driver does.
5. **Battery temperature** — no source at all.
6. **Thermistor calibration** — raw channel available, table needs measuring.

---

## Process notes for next time

- **`git diff` is cumulative.** Generating two patches that touch one file from
  the same dirty tree produced overlapping patches that conflicted on apply.
  Fixed by committing each change and using `git format-patch`. Do that from the
  start when several patches touch one file.
- **Check the task count, not the exit code.** Three separate builds this
  session reported `exit=0` while doing nothing, because a `SRC_URI` edit had
  failed and bitbake happily rebuilt an unchanged recipe. The tell is
  `Tasks Summary: ... N of N didn't need to be rerun`. Verify by grepping the
  built artifact, e.g. `strings <dtb> | grep -c <compatible>`.
- **Editing the kernel clone is not enough.** The recipe builds from the patch
  files. Regenerate the patch after every source edit — checking `srcversion`
  on the built `.ko` catches a stale build immediately.
- **Don't guess `format-patch` filenames.** It truncates subjects; capture
  whatever it produced with `ls` instead.
- **BusyBox on the device.** No `head -c`, no `head -N`, no `ls --time-style`.
  Use `sed -n '1,Np'`. An early register dump looked empty purely because of
  `head -c`, which delayed the charging investigation.
- **Identify the device, never assume.** There are at least three LuneOS
  devices on this host (A3, PineTab2, halium-arm64) and the A3's USB gadget MAC
  is randomised every boot, so its link-local address changes. Probe for the
  model string; do not trust an interface name.
