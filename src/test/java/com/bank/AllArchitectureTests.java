package com.bank;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Bank REST API - Architecture Tests")
@SelectClasses({
        com.bank.architecture.ArchitectureTest.class
})
public class AllArchitectureTests {
    // Architecture test suite
}
