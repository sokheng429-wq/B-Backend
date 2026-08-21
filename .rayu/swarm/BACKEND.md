# BACKEND — Jobs + Applications (contract)

Spring Boot 3.3.4 / Java 17. All endpoints wrap in `ApiResponse {success, message, data}`.
Errors: `ApiException` subclasses → `NotFoundException` (404), `BadRequestException` (400),
`ConflictException` (409). Auth is the existing SecurityConfig — `/api/admin/**` = ROLE_ADMIN,
`/api/public/**` = permitAll, CSRF off. **No security changes, no new security imports.**
Unauthenticated protected routes return 302 to `/login` (pre-existing oauth2Login redirect, unchanged).

## Entities / tables
- `job` — id, title, department, location, type, salary, description (TEXT), requirements (TEXT),
  benefits (TEXT), created_at, updated_at. `@PrePersist/@PreUpdate` timestamps (Member style).
- `job_application` — id, job_id (FK → job, LAZY, no cascade), full_name, email, phone,
  linkedin_url, cover_letter (TEXT), resume_name, resume_data (TEXT, base64), resume_content_type,
  status (default "NEW", String, limited to NEW/REVIEWED/ACCEPTED/REJECTED), created_at.

## Routes

### Admin (ROLE_ADMIN via `/api/admin/**`)
| Method | Path | Request | Response data |
|---|---|---|---|
| GET | /api/admin/jobs | — | `List<JobDto>` (newest first) |
| GET | /api/admin/jobs/{id} | — | `JobDto` (404 if missing) |
| POST | /api/admin/jobs | `JobDto` (201) | `JobDto` |
| PUT | /api/admin/jobs/{id} | `JobDto` | `JobDto` (404 if missing) |
| DELETE | /api/admin/jobs/{id} | — | null (409 if the job has applications) |
| GET | /api/admin/applications | — | `List<JobApplicationDto>` (newest first, jobTitle populated) |
| GET | /api/admin/applications/{id} | — | `JobApplicationDto` (404 if missing) |
| PATCH | /api/admin/applications/{id}/status | `{"status":"REVIEWED"}` | `JobApplicationDto` (400 on invalid status, 404 if missing) |
| DELETE | /api/admin/applications/{id} | — | null (404 if missing) |

### Public (permitAll via `/api/public/**`)
| Method | Path | Request | Response data |
|---|---|---|---|
| GET | /api/public/jobs | — | `List<JobDto>` (full safe fields) |
| GET | /api/public/jobs/{id} | — | `JobDto` (404 if missing) |
| POST | /api/public/jobs/{id}/apply | `JobApplicationDto` (201; path id wins over body jobId) | `JobApplicationDto` status NEW |

## DTO shapes
- `JobDto`: `{id, title*, department*, location*, type*, salary, description, requirements, benefits, createdAt}`
  (\* @NotBlank). Multi-line: description line 1 = Overview, lines 2+ = Responsibilities bullets;
  requirements/benefits lines = bullet lists.
- `JobApplicationDto`: `{id, jobId, jobTitle, fullName*, email*, phone*, linkedinUrl, coverLetter,
  resumeName, resumeData, resumeContentType, status, createdAt}` (* @NotBlank). Request omits
  id/jobTitle/status/createdAt (jobId optional on apply). `resumeData` echoed back for the admin
  data-URI resume view. `resumeData` > 5,000,000 chars → 400.

## Files (all new)
- `src/main/java/com/bgroceries/backend/entity/Job.java`, `entity/JobApplication.java`
- `src/main/java/com/bgroceries/backend/repository/JobRepository.java`,
  `repository/JobApplicationRepository.java` (`@EntityGraph("job")` on findAll/findById, `countByJobId`)
- `src/main/java/com/bgroceries/backend/dto/JobDto.java`, `dto/JobApplicationDto.java`,
  `dto/request/StatusUpdateRequest.java`
- `src/main/java/com/bgroceries/backend/service/JobService.java`, `service/JobApplicationService.java`
  (both `@Transactional`; JobService injects JobApplicationRepository for the delete-conflict check)
- `src/main/java/com/bgroceries/backend/controller/JobController.java`,
  `controller/PublicJobController.java`, `controller/AdminApplicationController.java`,
  `controller/PublicApplyController.java`
- Tests: `src/test/java/com/bgroceries/backend/service/JobServiceTest.java`,
  `service/JobApplicationServiceTest.java` (`@DataJpaTest` dev/H2, hand-rolled, no Mockito)

## Contract deltas vs shared.json (all additive, none rename)
1. `JobDto` includes `createdAt` (needed by AdminD Addjobs "postedDate" and list display; read-only).
2. `JobApplicationDto` response includes `resumeData` (echo) — required by the admin Applications
   data-URI resume view.
3. New small request DTO `StatusUpdateRequest {status}` for the PATCH body.

## Verified
- `mvn clean test-compile` OK; `mvn clean test` → 49 tests green (32 existing + 11 JobApplicationService + 6 JobService).
- Live smoke (dev/H2, :8082, shut down after): admin login → job POST 201 / GET / PUT / DELETE;
  job delete with applications → 409; public GET jobs 200; unauthenticated apply POST → 201 (status NEW,
  jobTitle populated); admin GET/PATCH status (400 invalid, 200 valid)/DELETE applications;
  USER token → 403 on both /api/admin/jobs and /api/admin/applications.

## Prior slice (Member Management — already implemented, frontend already wired)
- `/api/members` CRUD (authenticated): GET ?department=&category=, GET/{id}, POST, PUT, DELETE
  (hard delete cascades detail). MemberDto `{id, memberCode*, fullName*, position, rank, department,
  category, photoUrl, detail{...}}`; 409 on duplicate memberCode (case-insensitive). See git history
  / `entity/Member.java` for the schema. NOTE: no `member` table migration exists for prod Neon —
  `src/main/resources/db/member_tables.sql` holds manual DDL.
