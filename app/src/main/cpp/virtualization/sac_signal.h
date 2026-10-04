/*
 * Copyright 2026 Duck Apps Contributor
 * SPDX-License-Identifier: Apache-2.0
 */

#pragma once

#include <signal.h>

namespace duckdetector::virtualization {

    constexpr int sac_sig_exit = 128 + SIGSYS;

    bool sac_signal();

}  // namespace duckdetector::virtualization
