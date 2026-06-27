#!/usr/bin/env python3
"""
check_kt_braces.py  —  Kotlin brace-balance checker
Usage:
    python3 check_kt_braces.py [file1.kt file2.kt ...]   # check specific files
    python3 check_kt_braces.py                            # scan everything under cwd

Exits 0 if all files are clean, 1 if any file has imbalanced braces.
"""

import sys
import os


def check_file(path: str) -> tuple[int, list[str]]:
    """
    Return (balance, underflow_lines).
    balance == 0 means clean.
    Correctly skips:
      - // line comments
      - /* block comments */
      - triple-quoted strings (including """"...."""" four-quote variant)
      - regular double-quoted strings with \\ escapes and ${ } templates
      - character literals  'x'  '\\n'
    """
    try:
        with open(path, encoding="utf-8", errors="replace") as fh:
            src = fh.read()
    except OSError as exc:
        print(f"  ERROR reading {path}: {exc}", file=sys.stderr)
        return 0, []

    depth = 0
    i = 0
    n = len(src)
    underflows: list[str] = []

    def line_num(pos: int) -> int:
        return src[:pos].count("\n") + 1

    while i < n:
        # ── line comment ────────────────────────────────────────────────────
        if src[i : i + 2] == "//":
            end = src.find("\n", i)
            i = end + 1 if end != -1 else n

        # ── block comment ───────────────────────────────────────────────────
        elif src[i : i + 2] == "/*":
            end = src.find("*/", i + 2)
            i = end + 2 if end != -1 else n

        # ── triple-quoted string  (handles """"...."""" too) ─────────────────
        elif src[i : i + 3] == '"""':
            end = src.find('"""', i + 3)
            if end == -1:
                i = n
            else:
                i = end + 3
                # Consume any extra leading/trailing quotes that are part of
                # the """"...."""" four-quote Kotlin pattern (e.g. Regex literals)
                while i < n and src[i] == '"':
                    i += 1

        # ── regular double-quoted string ────────────────────────────────────
        elif src[i] == '"':
            i += 1
            while i < n and src[i] != '"':
                if src[i] == "\\":          # escape sequence
                    i += 1
                elif src[i : i + 2] == "${":  # string template ${ ... }
                    i += 2
                    tmpl = 1
                    while i < n and tmpl > 0:
                        if src[i] == "{":
                            tmpl += 1
                        elif src[i] == "}":
                            tmpl -= 1
                            if tmpl == 0:
                                i += 1
                                break
                        i += 1
                    continue
                i += 1
            i += 1  # closing "

        # ── character literal  'x'  '\\n'  '\uXXXX' ────────────────────────
        elif src[i] == "'":
            i += 1
            if i < n and src[i] == "\\":
                i += 2          # skip escaped char
            elif i < n:
                i += 1          # skip plain char
            if i < n and src[i] == "'":
                i += 1          # closing '

        # ── open brace ──────────────────────────────────────────────────────
        elif src[i] == "{":
            depth += 1
            i += 1

        # ── close brace ─────────────────────────────────────────────────────
        elif src[i] == "}":
            depth -= 1
            if depth < 0:
                underflows.append(f"line {line_num(i)}")
            i += 1

        else:
            i += 1

    return depth, underflows


def main() -> int:
    if len(sys.argv) > 1:
        targets = sys.argv[1:]
    else:
        targets = []
        for root, _dirs, files in os.walk("."):
            for f in files:
                if f.endswith(".kt"):
                    targets.append(os.path.join(root, f))

    if not targets:
        print("No .kt files found.")
        return 0

    failures: list[str] = []

    for path in sorted(targets):
        balance, underflows = check_file(path)
        if balance == 0 and not underflows:
            print(f"  ✓  {path}")
        else:
            tag = f"balance={balance:+d}"
            if underflows:
                tag += f"  underflows at {', '.join(underflows[:5])}"
                if len(underflows) > 5:
                    tag += f" (+{len(underflows) - 5} more)"
            print(f"  ✗  {path}  [{tag}]")
            failures.append(path)

    print()
    if failures:
        print(f"FAILED: {len(failures)} file(s) have brace imbalances.")
        return 1
    else:
        print(f"All {len(targets)} file(s) are clean.")
        return 0


if __name__ == "__main__":
    sys.exit(main())
