
package com.workzeno.workzeno.collaboration.invitation.exception;

import com.workzeno.workzeno.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Special exception used when an invitation has expired.
 *
 * Unlike ordinary ApiException errors, this exception must not
 * roll back the transaction that changes the invitation status
 * from PENDING to EXPIRED.
 */
public class InvitationExpiredException extends ApiException {

    public InvitationExpiredException() {
        super(
                HttpStatus.CONFLICT,
                "Invitation expired",
                "This invitation has expired"
        );
    }
}
