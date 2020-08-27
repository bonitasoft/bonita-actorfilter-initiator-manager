/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.actorfilter.initiator.manager;

import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.filter.AbstractUserFilter;
import org.bonitasoft.engine.filter.UserFilterException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 */
public class ProcessInitiatorManagerUserFilter extends AbstractUserFilter {

    static final String AUTO_ASSIGN = "autoAssign";

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessInitiatorManagerUserFilter.class);

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
    }

    @Override
    public List<Long> filter(final String actorName) throws UserFilterException {
        final long processInstanceId = getExecutionContext().getProcessInstanceId();
        try {
            long processInitiator = getAPIAccessor().getProcessAPI().getProcessInstance(processInstanceId).getStartedBy();
            User user = getAPIAccessor().getIdentityAPI().getUser(processInitiator);
            long managerUserId = user.getManagerUserId();
            // A '0' value for the user id means that it is not assign.
            if (managerUserId == 0) {
                // If a user has no manager, just return an empty candidate list.
                LOGGER.warn("No manager found for user: {}", user);
                return emptyList();
            }
            return asList(managerUserId);
        } catch (final UserNotFoundException e) {
            LOGGER.warn("No process initiator found for process instance {}. Process may have been started by the system", processInstanceId);
            throw new UserFilterException(e);
        } catch (final BonitaException e) {
            throw new UserFilterException(e);
        }
    }

    @Override
    public boolean shouldAutoAssignTaskIfSingleResult() {
        final Boolean autoAssign = (Boolean) getInputParameter(AUTO_ASSIGN);
        return autoAssign == null || autoAssign;
    }

}

