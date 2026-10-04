/*
 * Copyright 2026 Duck Apps Contributor
 * SPDX-License-Identifier: Apache-2.0
 */

#include "virtualization/sac_signal.h"

#include <linux/audit.h>
#include <stdlib.h>
#include <sys/syscall.h>
#include <unistd.h>

namespace duckdetector::virtualization {

    namespace {

        struct sigaction old_sig{};

        void on_sigsys(int sig, siginfo_t *info, void *) {
#if defined(__aarch64__)
            if (sig == SIGSYS && info != nullptr && info->si_code == SYS_SECCOMP &&
                info->si_arch == AUDIT_ARCH_AARCH64 &&
                (info->si_syscall == __NR_openat2 || info->si_syscall == __NR_statx ||
                 info->si_syscall == __NR_memfd_create || info->si_syscall == __NR_pidfd_open ||
                 info->si_syscall == __NR_renameat2)) {
                _exit(sac_sig_exit);
            }
#endif
            if (sigaction(SIGSYS, &old_sig, nullptr) != 0 || kill(getpid(), SIGSYS) != 0) {
                abort();
            }
        }

    }  // namespace

    bool sac_signal() {
        struct sigaction action{};
        action.sa_sigaction = on_sigsys;
        action.sa_flags = SA_SIGINFO;
        sigemptyset(&action.sa_mask);
        return sigaction(SIGSYS, &action, &old_sig) == 0;
    }

}  // namespace duckdetector::virtualization
