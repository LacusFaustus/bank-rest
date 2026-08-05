package com.bank;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Bank REST API - All Tests")
@SelectPackages({
        "com.bank.controller",
        "com.bank.service",
        "com.bank.entity",
        "com.bank.dto",
        "com.bank.repository",
        "com.bank.security",
        "com.bank.validation",
        "com.bank.event.handler",
        "com.bank.interceptor"
})
public class AllTests {
    // Test suite that runs all tests
}
