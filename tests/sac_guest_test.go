// Copyright 2026 Duck Apps Contributor
// SPDX-License-Identifier: Apache-2.0

package sacqa

import (
	"context"
	"os"
	"os/exec"
	"strings"
	"testing"
	"time"
)

func TestSacGuest(t *testing.T) {
	adb := os.Getenv("CDH_ADB")
	serial := os.Getenv("CDH_SERIAL")
	bin := os.Getenv("CDH_SAC_BINARY")
	if adb == "" || serial == "" || bin == "" {
		t.Fatal("CDH_ADB, CDH_SERIAL and CDH_SAC_BINARY are required")
	}
	run := func(t *testing.T, args ...string) string {
		t.Helper()
		ctx, cancel := context.WithTimeout(context.Background(), 45*time.Second)
		defer cancel()
		args = append([]string{"-s", serial}, args...)
		out, err := exec.CommandContext(ctx, adb, args...).CombinedOutput()
		if err != nil {
			t.Fatalf("adb %v: %v\n%s", args, err, out)
		}
		return strings.TrimSpace(string(out))
	}
	if got := run(t, "shell", "id", "-u"); got != "2000" {
		t.Fatalf("shell UID=%s", got)
	}
	if got := run(t, "shell", "getenforce"); got != "Enforcing" {
		t.Fatalf("SELinux=%s", got)
	}
	guest := "/data/local/tmp/duck-sac-qa"
	if run(t, "shell", "test", "!", "-e", guest) != "" {
		t.Fatal("guest candidate exists")
	}
	run(t, "push", bin, guest)
	t.Cleanup(func() { run(t, "shell", "rm", "-f", guest) })
	run(t, "shell", "chmod", "755", guest)
	for _, mode := range []string{"openat2", "statx", "memfd", "pidfd", "rename", "other", "user", "allowed", "pack"} {
		t.Run(mode, func(t *testing.T) {
			out := run(t, "shell", guest, mode)
			t.Log(out)
			if mode == "pack" {
				parts := strings.Split(out, "\nAGAIN\n")
				if len(parts) != 2 {
					t.Fatalf("missing repeat result: %s", out)
				}
				check := func(text string, want map[string]string) {
					t.Helper()
					fields := map[string]string{}
					for _, line := range strings.Split(text, "\n") {
						key, val, ok := strings.Cut(line, "=")
						if ok {
							fields[key] = val
						}
					}
					for key, val := range want {
						if fields[key] != val {
							t.Errorf("%s=%q, want %q", key, fields[key], val)
						}
					}
				}
				check(parts[0], map[string]string{"AVAILABLE": "1", "SUPPORTED": "0", "DISABLED": "1", "CHILD_CODE": "1", "CHILD_STATUS": "159"})
				check(parts[1], map[string]string{"AVAILABLE": "1", "SUPPORTED": "0", "DISABLED": "1", "CHILD_CODE": "0", "CHILD_STATUS": "0", "PARENT_SAME": "1"})
				return
			}
			want := "EXIT=159\nSIGNAL=0"
			if mode == "other" || mode == "user" {
				want = "EXIT=0\nSIGNAL=31"
			} else if mode == "allowed" {
				want = "EXIT=0\nSIGNAL=0"
			}
			if out != want {
				t.Errorf("got %q, want %q", out, want)
			}
		})
	}
}
