/*
 * Copyright 2026 Duck Apps Contributor
 * SPDX-License-Identifier: Apache-2.0
 */

#include "virtualization/honeypot_traps.h"
#include "virtualization/sac_signal.h"

#include <linux/filter.h>
#include <linux/seccomp.h>
#include <stddef.h>
#include <stdio.h>
#include <string.h>
#include <sys/prctl.h>
#include <sys/syscall.h>
#include <sys/wait.h>
#include <unistd.h>

namespace {

    volatile sig_atomic_t child_code = 0;
    volatile sig_atomic_t child_status = 0;

    void on_child(int, siginfo_t *info, void *) {
        child_code = info->si_code;
        child_status = info->si_status;
    }

    bool trap_call(int nr) {
        struct sock_filter rules[] = {
                BPF_STMT(BPF_LD | BPF_W | BPF_ABS, offsetof(struct seccomp_data, nr)),
                BPF_JUMP(BPF_JMP | BPF_JEQ | BPF_K, static_cast<unsigned>(nr), 0, 1),
                BPF_STMT(BPF_RET | BPF_K, SECCOMP_RET_TRAP),
                BPF_STMT(BPF_RET | BPF_K, SECCOMP_RET_ALLOW),
        };
        struct sock_fprog prog{4, rules};
        return prctl(PR_SET_NO_NEW_PRIVS, 1, 0, 0, 0) == 0 &&
               prctl(PR_SET_SECCOMP, SECCOMP_MODE_FILTER, &prog) == 0;
    }

}  // namespace

int main(int argc, char **argv) {
    if (argc != 2) return 2;
    if (strcmp(argv[1], "pack") == 0) {
        struct sigaction action{};
        action.sa_sigaction = on_child;
        action.sa_flags = SA_SIGINFO;
        sigemptyset(&action.sa_mask);
        if (sigaction(SIGCHLD, &action, nullptr) != 0 || !trap_call(__NR_openat2)) return 3;
        struct sigaction before{}, after{};
        if (sigaction(SIGSYS, nullptr, &before) != 0) return 4;
        const auto first = duckdetector::virtualization::run_sac_pack();
        printf("%sCHILD_CODE=%d\nCHILD_STATUS=%d\n", first.c_str(),
               static_cast<int>(child_code), static_cast<int>(child_status));
        child_code = 0;
        child_status = 0;
        const auto again = duckdetector::virtualization::run_sac_pack();
        printf("AGAIN\n%sCHILD_CODE=%d\nCHILD_STATUS=%d\n", again.c_str(),
               static_cast<int>(child_code), static_cast<int>(child_status));
        if (sigaction(SIGSYS, nullptr, &after) != 0) return 4;
        printf("PARENT_SAME=%d\n", before.sa_sigaction == after.sa_sigaction &&
               before.sa_flags == after.sa_flags ? 1 : 0);
        return 0;
    }

    int nr = 0;
    if (strcmp(argv[1], "openat2") == 0) nr = __NR_openat2;
    else if (strcmp(argv[1], "statx") == 0) nr = __NR_statx;
    else if (strcmp(argv[1], "memfd") == 0) nr = __NR_memfd_create;
    else if (strcmp(argv[1], "pidfd") == 0) nr = __NR_pidfd_open;
    else if (strcmp(argv[1], "rename") == 0) nr = __NR_renameat2;
    else if (strcmp(argv[1], "other") == 0) nr = __NR_getppid;
    else if (strcmp(argv[1], "user") != 0 && strcmp(argv[1], "allowed") != 0) return 2;

    const pid_t child = fork();
    if (child < 0) return 5;
    if (child == 0) {
        if (!duckdetector::virtualization::sac_signal()) _exit(6);
        if (nr != 0) {
            if (!trap_call(nr)) _exit(7);
            syscall(nr, 0, 0, 0, 0, 0, 0);
        } else if (strcmp(argv[1], "user") == 0) {
            raise(SIGSYS);
        } else {
            syscall(__NR_getppid);
        }
        _exit(0);
    }
    int status = 0;
    if (waitpid(child, &status, 0) != child) return 8;
    printf("EXIT=%d\nSIGNAL=%d\n", WIFEXITED(status) ? WEXITSTATUS(status) : 0,
           WIFSIGNALED(status) ? WTERMSIG(status) : 0);
    return 0;
}
