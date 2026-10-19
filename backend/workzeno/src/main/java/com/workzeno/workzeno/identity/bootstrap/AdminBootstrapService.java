package com.workzeno.workzeno.identity.bootstrap;

public interface AdminBootstrapService {

    void createInitialAdmin(String email, String rawPassword);
}