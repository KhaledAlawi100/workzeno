package com.workzeno.workzeno.identity.user;

import java.util.Locale;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkzenoUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        log.debug("Loading account for authentication");

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    log.debug("No account found during authentication");
                    return new UsernameNotFoundException(
                            "Invalid email or password"
                    );
                });

        return new WorkzenoUserDetails(user);
    }
}

