package com.waes.filediff;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.waes.filediff.rest.controller.DiffControllerIntegrationTest;
import com.waes.filediff.rest.controller.DiffControllerTest;
import com.waes.filediff.service.DiffServiceTest;

/**
 * Test suite which allows for running all test classes at once.
 * 
 * @author Rodrigo Hackbarth
 */
@RunWith(Suite.class)
@SuiteClasses({DiffControllerTest.class,
		      DiffServiceTest.class,
		      DiffControllerIntegrationTest.class})
public class DiffApplicationTestSuite {}
