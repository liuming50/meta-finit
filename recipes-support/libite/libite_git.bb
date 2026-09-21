SUMMARY = "Frog DNA, basically"
DESCRIPTION = "Libite is a lightweight library of *frog DNA* that can be used to fill \
the gaps in any dinosaur project.  It holds useful functions and macros \
developed by both [Finit][1] and the [OpenBSD][2] project.  Most notably \
the string functions: [strlcpy(3)][3], [strlcat(3)][3] and the highly \
useful *BSD [sys/queue.h][4] and [sys/tree.h][7] API's."
SECTION = "libs"
HOMEPAGE = "https://github.com/troglobit/libite"

LICENSE = "MIT & ISC"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3f81ad298cfae5094ebb68e1b9fdab96"

SRC_URI = "git://github.com/troglobit/libite.git;protocol=https;branch=master"

SRCREV = "b69d8a6013b50cbdfc7586cffd2f36c145e9e53a"
PV = "2.6.2+git${SRCPV}"

S = "${WORKDIR}/git"

inherit autotools gettext
