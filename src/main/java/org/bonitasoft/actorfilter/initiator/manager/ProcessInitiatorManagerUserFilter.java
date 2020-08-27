package org.bonitasoft.actorfilter.initiator.manager;

import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.filter.AbstractUserFilter;
import org.bonitasoft.engine.filter.UserFilterException;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 */
public class ProcessInitiatorManagerUserFilter extends AbstractUserFilter {

    static final String AUTO_ASSIGN = "autoAssign";

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
    }

    @Override
    public List<Long> filter(final String actorName) throws UserFilterException {
        try {
            final long processInstanceId = getExecutionContext().getProcessInstanceId();
            long processInitiator = getAPIAccessor().getProcessAPI().getProcessInstance(processInstanceId).getStartedBy();
            return asList(getAPIAccessor().getIdentityAPI().getUser(processInitiator).getManagerUserId());
        } catch (final BonitaException e) {
            throw new UserFilterException(e);
        }
    }

    @Override
    public boolean shouldAutoAssignTaskIfSingleResult() {
        final Boolean autoAssignO = (Boolean) getInputParameter(AUTO_ASSIGN);
        return autoAssignO == null || autoAssignO;
    }

}

