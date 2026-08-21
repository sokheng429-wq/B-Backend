---
name: react-hooks v7 strictness (frontend)
description: eslint react-hooks v7 in B-Frontend treats set-state-in-effect and purity as ERRORS — fetch effects must set state only after the async boundary
type: feedback
---

In B-Frontend (eslint 10 + eslint-plugin-react-hooks 7.1.1 flat config), the new rules
`react-hooks/set-state-in-effect` and `react-hooks/purity` are hard ERRORS, not warnings.

**Why:** The flat `recommended` config enables them; any synchronous `setState` in an
effect body (even via a helper called from the effect, e.g. `loadMembers(filters)` that
calls `setLoading(true)`) fails lint and breaks the build pipeline convention here.
MemberList.jsx initially failed lint for exactly this and had to be restructured.

**How to apply:** For fetch-on-mount/refresh in any page under src/Pages:
- Keep an initial `loading=true` state; set loading/error/data only AFTER an `await`
  inside the effect (with a `cancelled` flag cleanup).
- Re-trigger fetches by bumping a `refreshKey` state included in the effect deps;
  event handlers (retry, delete) may call `setState` freely, including
  `setLoading(true)` before bumping the key.
- Avoid `Date.now()`/`Math.random()` directly in a component body during render
  (`react-hooks/purity`) — AdminD.jsx line ~127 has a pre-existing such error.
