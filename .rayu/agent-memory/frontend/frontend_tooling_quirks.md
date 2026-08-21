---
name: Frontend tooling quirks (win32 bash)
description: Writing new frontend files on this machine — the Write tool can be denied, and bash heredoc commands get truncated around ~150 lines; chunk the content
type: project
---

On this machine (win32, bash via the tool wrapper) creating NEW source files for the
B-Frontend app:
- The Write tool was denied once mid-task (file creation still permitted via bash).
- `cat > file <<'EOF'` heredocs work, BUT a single Bash command gets truncated at
  roughly 150 lines / ~9 KB — the remainder is dropped, causing
  "unexpected EOF while looking for matching quote". MemberList/MemberForm/
  MemberDetailPage were written successfully in ~120-line chunks with `>>` appends.

**Why:** Tool-level command length limit; not a shell syntax issue (short heredocs
with single quotes work fine).

**How to apply:** When creating new files, split content into chunks of <= ~120 lines
(heredoc per chunk, `>` for the first, `>>` for appends), then `wc -l` to confirm
growth matches expectations before linting/building.

Refinement (Jobs + Applications session, 2026-08-21): the Write tool succeeded for a
BRAND-NEW file (Applications.jsx) but was DENIED when overwriting an EXISTING file
(Career.jsx). For existing files, use targeted Edit calls (multiple non-overlapping
edits per file work fine in one message) instead of Write/rewrite — this also avoids
the ~150-line heredoc truncation for large files.
