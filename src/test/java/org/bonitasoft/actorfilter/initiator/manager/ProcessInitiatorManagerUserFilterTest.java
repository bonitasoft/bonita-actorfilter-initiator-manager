package org.bonitasoft.actorfilter.initiator.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.actorfilter.initiator.manager.ProcessInitiatorManagerUserFilter.AUTO_ASSIGN;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.connector.EngineExecutionContext;
import org.bonitasoft.engine.filter.UserFilterException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessInitiatorManagerUserFilterTest {

    public static final long NO_ID = 0;
    private static final long PROCESS_INSTANCE_ID = 1L;
    private static final Long INITIATOR_ID = 2L;
    private static final Long MANAGER_ID = 3L;
    @InjectMocks
    private ProcessInitiatorManagerUserFilter filter;

    @Mock(lenient = true)
    private APIAccessor apiAccessor;
    @Mock(lenient = true)
    private ProcessAPI processApi;
    @Mock(lenient = true)
    private IdentityAPI indentityApi;

    @Mock(lenient = true)
    private EngineExecutionContext executionContext;
    @Mock(lenient = true)
    private ProcessInstance processInstance;

    @BeforeEach
    void setUp() throws Exception {
        when(apiAccessor.getProcessAPI()).thenReturn(processApi);
        when(apiAccessor.getIdentityAPI()).thenReturn(indentityApi);
        when(executionContext.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);

        when(processApi.getProcessInstance(PROCESS_INSTANCE_ID)).thenReturn(processInstance);
        when(processInstance.getStartedBy()).thenReturn(INITIATOR_ID);
    }

    @Test
    void should_auto_assign() {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(AUTO_ASSIGN, true);
        filter.setInputParameters(parameters);

        // When
        boolean autoAssign = filter.shouldAutoAssignTaskIfSingleResult();

        // Then
        assertThat(autoAssign).isTrue();
    }

    @Test
    void should_not_auto_assign() {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(AUTO_ASSIGN, false);
        filter.setInputParameters(parameters);

        // When
        boolean autoAssign = filter.shouldAutoAssignTaskIfSingleResult();

        // Then
        assertThat(autoAssign).isFalse();
    }

    @Test
    void should_default_to_auto_assign() {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        filter.setInputParameters(parameters);

        // When
        boolean autoAssign = filter.shouldAutoAssignTaskIfSingleResult();

        // Then
        assertThat(autoAssign).isTrue();
    }

    @Test
    void should_return_a_list_of_candidates() throws Exception {
        // Given
        User initiator = mock(User.class);
        when(indentityApi.getUser(INITIATOR_ID)).thenReturn(initiator);
        when(initiator.getManagerUserId()).thenReturn(MANAGER_ID);

        // When
        List<Long> candidates = filter.filter("MyActor");

        // Then
        assertThat(candidates).as("Only the manager from the initiating user can be candidate.")
                .containsExactly(MANAGER_ID);

    }

    @Test
    void should_return_an_empty_list_if_no_manager_candidates() throws Exception {
        // Given
        User initiator = mock(User.class);
        when(indentityApi.getUser(INITIATOR_ID)).thenReturn(initiator);
        when(initiator.getManagerUserId()).thenReturn(NO_ID);

        // When
        List<Long> candidates = filter.filter("MyActor");

        // Then
        assertThat(candidates).as("Only the manager from the initiating user can be candidate.")
                .isEmpty();

    }

    @Test
    void should_throw_ex_if_process_started_by_system() throws Exception {
        // Given
        when(indentityApi.getUser(INITIATOR_ID)).thenThrow(new UserNotFoundException("No user found for id:" + INITIATOR_ID));

        // When
        assertThrows(UserFilterException.class, () ->
                filter.filter("MyActor")
        );
    }

}
