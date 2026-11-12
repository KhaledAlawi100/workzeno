package com.workzeno.workzeno.collaboration.membership.service;

import com.workzeno.workzeno.collaboration.membership.domain.ProjectMembership;
import com.workzeno.workzeno.collaboration.membership.domain.ProjectMembershipRole;
import com.workzeno.workzeno.collaboration.membership.repository.ProjectMembershipRepository;
import com.workzeno.workzeno.identity.user.domain.User;
import com.workzeno.workzeno.projects.domain.Project;
import com.workzeno.workzeno.shared.exception.ApiException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectAuthorizationServiceTest {

    @Mock
    private ProjectMembershipRepository membershipRepository;

    @InjectMocks
    private ProjectAuthorizationService authorizationService;

    private Project project;
    private User owner;

    private UUID ownerId;
    private UUID adminId;
    private UUID memberId;
    private UUID nonMemberId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        adminId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        nonMemberId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        owner = mock(User.class);
        project = mock(Project.class);

        when(owner.getId()).thenReturn(ownerId);
        when(project.getOwner()).thenReturn(owner);
    }

    @Test
    void requireOwner_shouldAllowProjectOwner() {

        authorizationService.requireOwner(project, ownerId);

        verifyNoInteractions(membershipRepository);
    }

    @Test
    void requireOwner_shouldRejectAdmin() {

        assertThatThrownBy(() -> authorizationService.requireOwner(project, adminId))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(ex.getMessage())
                            .isEqualTo("Only the project owner can perform this action");
                });

        verifyNoInteractions(membershipRepository);
    }

    @Test
    void requireOwnerOrAdmin_shouldAllowProjectOwner() {

        authorizationService.requireOwnerOrAdmin(project, ownerId);

        verifyNoInteractions(membershipRepository);
    }


    @Nested
    class RequireOwnerOrAdminWithRoleLookup {

        @BeforeEach
        void stubProjectId() {
            when(project.getId()).thenReturn(projectId);   // only these tests see it
        }


        @Test
        void requireOwnerOrAdmin_shouldAllowAdmin() {

            ProjectMembership adminMembership = mock(ProjectMembership.class);

            when(membershipRepository.findByProjectIdAndUserId(
                    projectId,
                    adminId
            )).thenReturn(Optional.of(adminMembership));

            when(adminMembership.getRole())
                    .thenReturn(ProjectMembershipRole.ADMIN);

            authorizationService.requireOwnerOrAdmin(project, adminId);

            verify(membershipRepository).findByProjectIdAndUserId(
                    projectId,
                    adminId
            );
        }

        @Test
        void requireOwnerOrAdmin_shouldRejectMember() {

            ProjectMembership memberMembership = mock(ProjectMembership.class);

            when(membershipRepository.findByProjectIdAndUserId(
                    projectId,
                    memberId
            )).thenReturn(Optional.of(memberMembership));

            when(memberMembership.getRole())
                    .thenReturn(ProjectMembershipRole.MEMBER);

            assertThatThrownBy(() ->
                    authorizationService.requireOwnerOrAdmin(project, memberId)
            )
                    .isInstanceOfSatisfying(ApiException.class, ex -> {
                        assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                        assertThat(ex.getMessage())
                                .isEqualTo("Only project owners and admins can perform this action");
                    });

        }

        @Test
        void requireOwnerOrAdmin_shouldRejectNonMember() {

            when(membershipRepository.findByProjectIdAndUserId(
                    projectId,
                    nonMemberId
            )).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    authorizationService.requireOwnerOrAdmin(project, nonMemberId)
            )
                    .isInstanceOfSatisfying(ApiException.class, ex -> {
                        assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    });

            verify(membershipRepository).findByProjectIdAndUserId(
                    projectId,
                    nonMemberId
            );
        }


        // end of nested class
    }



}