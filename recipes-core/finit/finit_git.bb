SUMMARY = "Fast init for Linux systems"
DESCRIPTION = "Fast init for Linux systems"
SECTION = "base"
HOMEPAGE = "https://github.com/troglobit/finit"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7f4881796913b7fa3c08182acd0b3987"

PACKAGE_ARCH = "${MACHINE_ARCH}"

def get_custom_rtc_restore_date(d):
    import datetime

    timestamp = d.getVar("REPRODUCIBLE_TIMESTAMP_ROOTFS")
    if timestamp:
        return datetime.datetime.fromtimestamp(int(timestamp), datetime.timezone.utc).strftime('%4Y-%2m-%2d %2H:%2M:%2S')
    else:
        return datetime.datetime.now().strftime('%4Y-%2m-%2d %2H:%2M:%2S')

# kevent or hotplug-plugin
UEVENT_MANAGER ?= "hotplug-plugin"
RTC_RESTORE_DATE ?= "${@get_custom_rtc_restore_date(d)}"
RANDOM_SEED_FILE ?= "/var/lib/misc/random-seed"
WATCHDOG_DEVICE ?= "/dev/watchdog"

PACKAGECONFIG_CONFARGS[vardepsexclude] = "RTC_RESTORE_DATE"
PACKAGECONFIG ??= "auto-reload \
                   dbus \
                   dbus-plugin \
                   fastboot \
                   random-seed \
                   hook-scripts-plugin \
                   kernel-cmdline \
                   libcap \
                   modules-load-plugin \
                   netlink-plugin \
                   rtc-plugin \
                   urandom-plugin \
                   redirect \
                   rescue \
                   tty-plugin \
                   ${UEVENT_MANAGER} \
                   ${@bb.utils.filter('DISTRO_FEATURES', 'pam', d)} \
                  "

PACKAGECONFIG[random-seed] = "--with-random-seed=${RANDOM_SEED_FILE},--without-random-seed"
PACKAGECONFIG[auto-reload] = "--enable-auto-reload,--disable-auto-reload"
PACKAGECONFIG[cgroup] = "--enable-cgroup,--disable-cgroup"
PACKAGECONFIG[contrib] = "--enable-contrib,--disable-contrib"
PACKAGECONFIG[doc] = "--enable-doc,--disable-doc"
PACKAGECONFIG[dbus] = "--enable-dbus,--disable-dbus"
PACKAGECONFIG[kernel-cmdline] = "--enable-kernel-cmdline,--disable-kernel-cmdline"
PACKAGECONFIG[kernel-logging] = "--enable-kernel-logging,--disable-kernel-logging"
PACKAGECONFIG[fastboot] = "--enable-fastboot,--disable-fastboot"
PACKAGECONFIG[fsckfix] = "--enable-fsckfix,--disable-fsckfix"
PACKAGECONFIG[pam] = "--enable-pam,--disable-pam,libpam"
PACKAGECONFIG[redirect] = "--enable-redirect,--disable-redirect"
PACKAGECONFIG[keventd] = "--with-keventd --with-udev-rules,--without-keventd --without-udev-rules,util-linux"
PACKAGECONFIG[watchdog] = "--with-watchdog=${WATCHDOG_DEVICE},--without-watchdog"
PACKAGECONFIG[reboot-watchdog] = ",,"
PACKAGECONFIG[rescue] = "--enable-rescue,--disable-rescue"
PACKAGECONFIG[libcap] = "--enable-libcap,--disable-libcap,libcap"
PACKAGECONFIG[libsystemd] = "--with-libsystemd,--without-libsystemd"
PACKAGECONFIG[sulogin] = "--with-sulogin,--without-sulogin,,util-linux-sulogin"
PACKAGECONFIG[modules-load-plugin] = "--enable-modules-load-plugin,--disable-modules-load-plugin"
PACKAGECONFIG[modprobe-plugin] = "--enable-modprobe-plugin,--disable-modprobe-plugin,,kmod"
PACKAGECONFIG[hotplug-plugin] = "--enable-hotplug-plugin,--disable-hotplug-plugin,udev,udev"
PACKAGECONFIG[hook-scripts-plugin] = "--enable-hook-scripts-plugin,--disable-hook-scripts-plugin"
PACKAGECONFIG[rc-local] = "--with-rc-local=${sysconfdir}/rc.loal,--without-rc-local"
PACKAGECONFIG[rtc-plugin] = '--enable-rtc-plugin --with-rtc-date="${RTC_RESTORE_DATE}",--disable-rtc-plugin'
PACKAGECONFIG[rtc-file] ="--with-rtc-file=yes,--without-rtc-file"
PACKAGECONFIG[urandom-plugin] = "--enable-urandom-plugin,--disable-urandom-plugin"
PACKAGECONFIG[tty-plugin] = "--enable-tty-plugin,--disable-tty-plugin"
PACKAGECONFIG[netlink-plugin] = "--enable-netlink-plugin,--disable-netlink-plugin"
PACKAGECONFIG[dbus-plugin] = "--enable-dbus-plugin,--disable-dbus-plugin,dbus,dbus"
PACKAGECONFIG[alsa-utils-plugin] = "--enable-alsa-utils-plugin,--disable-alsa-utils-plugin,alsa-utils,alsa-utils-alsactl"
PACKAGECONFIG[x11-common-plugin] = "--enable-x11-common-plugin,--disable-x11-common-plugin,virtual/libx11"
PACKAGECONFIG[resolvconf-plugin] = "--enable-resolvconf-plugin,--disable-resolvconf-plugin,resolvconf,resolvconf"
PACKAGECONFIG[plymouth-plugin] = "--enable-plymouth-plugin,--disable-plymouth-plugin,plymouth,plymouth"
PACKAGECONFIG[testserv-plugin] = "--enable-testserv-plugin,--disable-testserv-plugin"

TARGET_CFLAGS += "-DFINIT_NOLOGIN_PATH=\\"${NOLOGINS_FILE}\\""

inherit autotools gettext pkgconfig update-alternatives

SRC_URI = "git://github.com/troglobit/finit;protocol=https;branch=master;name=finit \
           file://machine-id-setup.sh \
           file://01-machine-id.conf \
           file://10-hotplug.conf \
"

SRCREV_finit = "417abf90007512e61d9b26e6d9150183c17aad62"

PV = "5.0-rc1"

S = "${WORKDIR}/git"

PACKAGES =+ "${PN}-plugins ${PN}-bash-completion"

DEPENDS += "libuev libite libconfuse virtual/crypt"
RDEPENDS:${PN} += "${PN}-plugins util-linux-fsck ${@bb.utils.contains('PACKAGECONFIG','dbus-plugin','','util-linux-uuidgen',d)}"

FILES:${PN} += "${nonarch_libdir}/tmpfiles.d ${datadir}/dbus-1"
FILES:${PN}-plugins = "${libdir}/finit/plugins"
FILES:${PN}-bash-completion = "${datadir}/bash-completion"

ALTERNATIVE_PRIORITY = "100"
ALTERNATIVE:${PN} = "coldplug getty logit runparts"

ALTERNATIVE_LINK_NAME[coldplug] = "${base_sbindir}/coldplug"
ALTERNATIVE_LINK_NAME[getty] = "${base_sbindir}/getty"
ALTERNATIVE_LINK_NAME[logit] = "${base_sbindir}/logit"
ALTERNATIVE_LINK_NAME[runparts] = "${base_sbindir}/runparts"

do_install:append() {
    # Make a empty finit.conf
    install -d ${D}${sysconfdir}
    echo "" > ${D}${sysconfdir}/finit.conf

    # For compatibility with sysvinit
    install -d ${D}${base_sbindir}

    ln -sf ${sbindir}/halt ${D}${base_sbindir}/halt
    ln -sf ${sbindir}/reboot ${D}${base_sbindir}/reboot
    ln -sf ${sbindir}/shutdown ${D}${base_sbindir}/shutdown
    ln -sf ${sbindir}/finit ${D}${base_sbindir}/init
    ln -sf ${libexecdir}/finit/coldplug ${D}${base_sbindir}/coldplug
    ln -sf ${libexecdir}/finit/getty ${D}${base_sbindir}/getty
    ln -sf ${libexecdir}/finit/logit ${D}${base_sbindir}/logit
    ln -sf ${libexecdir}/finit/runparts ${D}${base_sbindir}/runparts

    if ${@bb.utils.contains('PACKAGECONFIG','dbus-plugin','true','false',d)}; then
        ln -sf  ${localstatedir}/lib/dbus/machine-id ${D}${sysconfdir}/machine-id
    else
        install -m 0755 ${WORKDIR}/machine-id-setup.sh ${D}${base_sbindir}/machine-id-setup
        install -m 0644 ${WORKDIR}/01-machine-id.conf ${D}${libdir}/finit/system
    fi

    if ${@bb.utils.contains('PACKAGECONFIG','hotplug-plugin','true','false',d)}; then
        # Install a customized 10-hotplug.conf
        rm -f ${D}${libdir}/finit/system/10-hotplug.conf
        install -m 0644 ${WORKDIR}/10-hotplug.conf ${D}${libdir}/finit/system
    fi

    if ${@bb.utils.contains('PACKAGECONFIG','reboot-watchdog','true','false',d)}; then
        echo -e "\n# Controls whether the system should reboot via the watchdog timer (WDT)" >> ${D}${sysconfdir}/finit.conf
        echo "reboot-watchdog = on" >> ${D}${sysconfdir}/finit.conf
    fi

    # /var/tmp in finit's tmpfiles does not comply with OE's meta/files/fs-perms.txt
    sed -i -e "/d.*var\/tmp/d" ${D}${nonarch_libdir}/tmpfiles.d/var.conf
}
