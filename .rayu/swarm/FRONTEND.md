# FRONTEND — Jobs + Applications — status

Frontend slice of the Jobs + Applications feature. Built against the shared brief
(`.rayu/swarm/shared.json`) and the existing patterns (MemberList dark-slate admin
design, bilingual TEXTS {en,kh}, ApiResponse `res.data` unwrap).

## Done (files)

- `D:\1.B.Groceries\Frontend\B-Frontend\src\api\api.js` — jobAPI repointed to
  `/admin/jobs` (getAll/getById/create/update/delete, JSON body) + added
  `jobAPI.applyJob(jobId, payload)` → POST `/public/jobs/{jobId}/apply`.
  applicationAPI repointed to `/admin/applications` (getAll/getById/updateStatus
  PATCH `{status}`/delete). publicAPI gained `getJobs()` → `/public/jobs` and
  `getJobById(id)` → `/public/jobs/{id}`. authAPI/memberAPI/productAPI untouched;
  old `applicationAPI.create` (FormData) removed — public apply is JSON base64.
- `D:\1.B.Groceries\Frontend\B-Frontend\src\Pages\Auth\Addjobs.jsx` — wired to
  jobAPI: fetch list on mount (loading + refreshKey pattern), create/update/delete
  via API with reload after writes, backend errors (incl. 409 delete-with-
  applications) surfaced in a dismissible red banner with retry, NEW **Benefits**
  textarea (form state + live preview + list row), postedDate rendered from
  backend `createdAt` (formatted). Bilingual, orange accent, single-page
  form+live-list kept. Delete uses window.confirm + per-row spinner.
- `D:\1.B.Groceries\Frontend\B-Frontend\src\Pages\Auth\Applications.jsx` — NEW
  admin report (indigo accent): hero + 5 stat cards (total/NEW/REVIEWED/ACCEPTED/
  REJECTED), table (jobTitle, applicant+avatar, email, phone, status badge, date,
  View/Delete), right-side detail slide-over (all fields, mailto/tel/linkedin
  links, cover letter pre-wrap, resume link built as
  `data:{resumeContentType};base64,{resumeData}` with `download={resumeName}` +
  target _blank, status select → PATCH, delete with confirm). Loading/error+retry/
  empty states. Bilingual.
- `D:\1.B.Groceries\Frontend\B-Frontend\src\Pages\Auth\AdminD.jsx` — added
  `Applications` import, ADMIN-only sidebar link (Management section, ClipboardIcon,
  indigo accent) → `/admin/applications`, `renderContent` case, and
  `/admin/applications` (incl. subpaths) in the `adminOnly` guard (STORE cannot see
  it). Dashboard Applications stat card now links to `/admin/applications`.
- `D:\1.B.Groceries\Frontend\B-Frontend\src\Pages\Home\Career.jsx` — fetches
  `publicAPI.getJobs()` on mount (loading + error+retry + empty states), removed
  hardcoded JOBS/DEPARTMENTS; department filter options derived from data; cards
  render string fields; title/Details → `/career-detail/{id}`, Apply →
  `/apply-now?job={id}`; posted from `createdAt`. Hero/benefits/stats untouched.
- `D:\1.B.Groceries\Frontend\B-Frontend\src\Pages\Home\Careerdetail.jsx` — reads
  `useParams().id` and fetches `publicAPI.getJobById(id)` (location.state fallback
  for legacy `/career-detail` links); line-oriented rendering: description line 1 →
  Job Overview, lines 2+ → Key Responsibilities <li>s; requirements/benefits lines
  → <li>s; sections hidden when empty. Apply → `/apply-now?job={id}`. Loading /
  not-found (+retry) states. CSS additions in Careerdetail.css.
- `D:\1.B.Groceries\Frontend\B-Frontend\src\App.jsx` — added
  `<Route path="/career-detail/:id">` (kept `/career-detail` + `/career-details`).
- `D:\1.B.Groceries\Frontend\B-Frontend\src\Pages\Home\Applynow.jsx` — reads
  `?job=` query param (react-router `useSearchParams`); position dropdown populated
  from `publicAPI.getJobs()` (value=id, label=title, pre-selected from param via
  derived value, no effect-setState); on submit FileReader → base64 data (prefix
  stripped) + resumeName + resumeContentType, POST `jobAPI.applyJob(jobId, payload)`;
  file capped at 2MB with inline error; submitting spinner + submit error; success
  screen + Telegram button kept.

## Contract notes / deltas

- All pages read `ApiResponse.data` (`res.data`); 201/200 both fine for apply.
- JobDto fields consumed: `{id,title,department,location,type,salary,description,
  requirements,benefits,createdAt}` (strings; multi-line fields are newline lists).
  Application item: `{id,jobId,jobTitle,fullName,email,phone,linkedinUrl,
  coverLetter,resumeName,resumeData,resumeContentType,status,createdAt}`.
- **Delta:** apply POST uses `jobAPI.applyJob` (per the frontend-api slice), not
  `applicationAPI.applyJob` — applicationAPI is admin-only. `linkedinUrl`/
  `coverLetter` sent as `null` when empty; `jobId` is a Number.
- Status strings exactly NEW/REVIEWED/ACCEPTED/REJECTED (select limited to these).
- eslint react-hooks v7: fetch effects set state only after `await` (cancelled
  flag); retry/delete handlers bump a `refreshKey`. No synchronous setState in
  effect bodies.

## Verification

- `npx eslint` on all changed/created files: 0 problems; only the known
  pre-existing AdminD.jsx `Date.now` `react-hooks/purity` error remains (untouched).
- `npm run build` passes (pre-existing chunk-size warning only).
- Live use requires the backend /api/admin/jobs, /api/admin/applications and
  /api/public/jobs endpoints (built in parallel) to be up; admin pages need an
  ADMIN JWT.
