package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.JobApplicationDto;
import com.bgroceries.backend.dto.JobDto;
import com.bgroceries.backend.exception.ConflictException;
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
 * CRUD + delete-blocked-by-applications behavior of {@link JobService} against
 * a real H2 database (dev profile). Hand-rolled — no Mockito.
 */
@DataJpaTest
@ActiveProfiles("dev")
class JobServiceTest {

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
                .title("Store Supervisor")
                .department("Operations")
                .location("Siem Reap")
                .type("Full-time")
                .salary("$500 - $600")
                .description("Run the daily store operations.\nManage the shift team")
                .requirements("2+ years retail experience\nLeadership skills")
                .benefits("Health insurance\n13th month bonus")
                .build();
    }

    private JobApplicationDto sampleApplication(Long jobId) {
        return JobApplicationDto.builder()
                .jobId(jobId)
                .fullName("Dara Sok")
                .email("dara@bgroceries.demo")
                .phone("+85512345678")
                .resumeName("dara-resume.pdf")
                .resumeData("aGVsbG8=")
                .resumeContentType("application/pdf")
                .build();
    }

    @Test
    void createAndGetJobRoundTripsAllFields() {
        JobDto created = jobService.createJob(sampleJob());

        assertThat(created.getId()).isNotNull();
        assertThat(created.getCreatedAt()).isNotNull();

        JobDto fetched = jobService.getJobById(created.getId());
        assertThat(fetched.getTitle()).isEqualTo("Store Supervisor");
        assertThat(fetched.getDepartment()).isEqualTo("Operations");
        assertThat(fetched.getLocation()).isEqualTo("Siem Reap");
        assertThat(fetched.getType()).isEqualTo("Full-time");
        assertThat(fetched.getSalary()).isEqualTo("$500 - $600");
        assertThat(fetched.getDescription()).contains("\n");
        assertThat(fetched.getRequirements()).contains("Leadership skills");
        assertThat(fetched.getBenefits()).contains("13th month bonus");
    }

    @Test
    void listOrdersNewestFirst() {
        JobDto first = jobService.createJob(sampleJob());

        JobDto second = sampleJob();
        second.setTitle("Cashier");
        jobService.createJob(second);

        List<JobDto> all = jobService.getAllJobs();
        assertThat(all).hasSize(2);
        assertThat(all.get(0).getTitle()).isEqualTo("Cashier");
        assertThat(all.get(1).getTitle()).isEqualTo("Store Supervisor");
    }

    @Test
    void updateJobMergesFields() {
        JobDto created = jobService.createJob(sampleJob());

        created.setTitle("Store Supervisor (Night)");
        created.setLocation("Phnom Penh");
        created.setSalary("$600 - $700");

        JobDto updated = jobService.updateJob(created.getId(), created);

        assertThat(updated.getTitle()).isEqualTo("Store Supervisor (Night)");
        assertThat(updated.getLocation()).isEqualTo("Phnom Penh");
        assertThat(updated.getSalary()).isEqualTo("$600 - $700");
        assertThat(updated.getDepartment()).isEqualTo("Operations"); // untouched
    }

    @Test
    void getJobByIdThrowsNotFoundForMissingJob() {
        assertThatThrownBy(() -> jobService.getJobById(999_999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteJobWithoutApplicationsSucceeds() {
        JobDto created = jobService.createJob(sampleJob());

        jobService.deleteJob(created.getId());

        assertThat(jobRepository.existsById(created.getId())).isFalse();
        assertThatThrownBy(() -> jobService.getJobById(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteJobBlockedWhenApplicationsExist() {
        JobDto created = jobService.createJob(sampleJob());
        jobApplicationService.apply(created.getId(), sampleApplication(created.getId()));

        assertThatThrownBy(() -> jobService.deleteJob(created.getId()))
                .isInstanceOf(ConflictException.class);

        // Job and its application both survive.
        assertThat(jobRepository.existsById(created.getId())).isTrue();
        assertThat(jobApplicationRepository.countByJobId(created.getId())).isEqualTo(1);
    }
}
