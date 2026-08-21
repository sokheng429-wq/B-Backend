package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.JobApplicationDto;
import com.bgroceries.backend.dto.JobDto;
import com.bgroceries.backend.exception.BadRequestException;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.JobApplicationRepository;
import com.bgroceries.backend.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Apply + admin workflow behavior of {@link JobApplicationService} against a
 * real H2 database (dev profile). Hand-rolled — no Mockito (class mocking is
 * broken on JDK 26).
 */
@DataJpaTest
@ActiveProfiles("dev")
class JobApplicationServiceTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    private JobService jobService;
    private JobApplicationService jobApplicationService;

    @BeforeEach
    void setUp() {
        jobService = new JobService(jobRepository, jobApplicationRepository);
        jobApplicationService = new JobApplicationService(jobRepository, jobApplicationRepository);
    }

    private JobDto sampleJob() {
        return JobDto.builder()
                .title("Cashier")
                .department("Sales")
                .location("Phnom Penh")
                .type("Full-time")
                .salary("$300 - $400")
                .description("Handle customer checkout.\nProcess payments")
                .requirements("Honest\nFriendly")
                .benefits("Free lunch\nStaff discount")
                .build();
    }

    private JobApplicationDto sampleApplication(Long jobId) {
        return JobApplicationDto.builder()
                .jobId(jobId)
                .fullName("Dara Sok")
                .email("dara@bgroceries.demo")
                .phone("+85512345678")
                .linkedinUrl("https://linkedin.com/in/dara")
                .coverLetter("I am a great fit for this role.")
                .resumeName("dara-resume.pdf")
                .resumeData("aGVsbG8gd29ybGQ=")
                .resumeContentType("application/pdf")
                .build();
    }

    @Test
    void applyCreatesApplicationWithStatusNew() {
        JobDto job = jobService.createJob(sampleJob());

        JobApplicationDto created = jobApplicationService.apply(job.getId(), sampleApplication(job.getId()));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getJobId()).isEqualTo(job.getId());
        assertThat(created.getJobTitle()).isEqualTo("Cashier");
        assertThat(created.getStatus()).isEqualTo("NEW");
        assertThat(created.getFullName()).isEqualTo("Dara Sok");
        assertThat(created.getEmail()).isEqualTo("dara@bgroceries.demo");
        assertThat(created.getResumeData()).isEqualTo("aGVsbG8gd29ybGQ=");
        assertThat(created.getCreatedAt()).isNotNull();
    }

    @Test
    void applyThrowsNotFoundForMissingJob() {
        assertThatThrownBy(() -> jobApplicationService.apply(999_999L, sampleApplication(999_999L)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void applyRejectsResumeDataOverSizeCap() {
        JobDto job = jobService.createJob(sampleJob());

        JobApplicationDto application = sampleApplication(job.getId());
        application.setResumeData("a".repeat(JobApplicationService.MAX_RESUME_DATA_LENGTH + 1));

        assertThatThrownBy(() -> jobApplicationService.apply(job.getId(), application))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void listIncludesJobTitleOrderedNewestFirst() {
        JobDto job = jobService.createJob(sampleJob());
        JobApplicationDto first = jobApplicationService.apply(job.getId(), sampleApplication(job.getId()));
        JobApplicationDto second = jobApplicationService.apply(job.getId(), sampleApplication(job.getId()));

        List<JobApplicationDto> all = jobApplicationService.getAllApplications();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(JobApplicationDto::getJobTitle).containsOnly("Cashier");
        assertThat(all.get(0).getId()).isEqualTo(second.getId());
        assertThat(all.get(1).getId()).isEqualTo(first.getId());
    }

    @Test
    void getByIdReturnsApplicationWithJobTitle() {
        JobDto job = jobService.createJob(sampleJob());
        JobApplicationDto created = jobApplicationService.apply(job.getId(), sampleApplication(job.getId()));

        JobApplicationDto fetched = jobApplicationService.getApplicationById(created.getId());

        assertThat(fetched.getJobTitle()).isEqualTo("Cashier");
        assertThat(fetched.getStatus()).isEqualTo("NEW");
    }

    @Test
    void getByIdThrowsNotFoundForMissingApplication() {
        assertThatThrownBy(() -> jobApplicationService.getApplicationById(999_999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateStatusAcceptsEachValidStatus() {
        JobDto job = jobService.createJob(sampleJob());
        JobApplicationDto created = jobApplicationService.apply(job.getId(), sampleApplication(job.getId()));

        for (String status : List.of("REVIEWED", "ACCEPTED", "REJECTED")) {
            JobApplicationDto updated = jobApplicationService.updateStatus(created.getId(), status);
            assertThat(updated.getStatus()).isEqualTo(status);
        }
    }

    @Test
    void updateStatusRejectsInvalidValue() {
        JobDto job = jobService.createJob(sampleJob());
        JobApplicationDto created = jobApplicationService.apply(job.getId(), sampleApplication(job.getId()));

        assertThatThrownBy(() -> jobApplicationService.updateStatus(created.getId(), "PENDING"))
                .isInstanceOf(BadRequestException.class);

        JobApplicationDto unchanged = jobApplicationService.getApplicationById(created.getId());
        assertThat(unchanged.getStatus()).isEqualTo("NEW");
    }

    @Test
    void updateStatusRejectsNull() {
        JobDto job = jobService.createJob(sampleJob());
        JobApplicationDto created = jobApplicationService.apply(job.getId(), sampleApplication(job.getId()));

        assertThatThrownBy(() -> jobApplicationService.updateStatus(created.getId(), null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void updateStatusThrowsNotFoundForMissingApplication() {
        assertThatThrownBy(() -> jobApplicationService.updateStatus(999_999L, "REVIEWED"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteRemovesApplication() {
        JobDto job = jobService.createJob(sampleJob());
        JobApplicationDto created = jobApplicationService.apply(job.getId(), sampleApplication(job.getId()));

        jobApplicationService.deleteApplication(created.getId());

        assertThat(jobApplicationRepository.existsById(created.getId())).isFalse();
        assertThatThrownBy(() -> jobApplicationService.getApplicationById(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }
}
