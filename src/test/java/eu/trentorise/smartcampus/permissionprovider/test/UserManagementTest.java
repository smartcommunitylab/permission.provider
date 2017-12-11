/*******************************************************************************
 * Copyright 2015 Fondazione Bruno Kessler
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/

package eu.trentorise.smartcampus.permissionprovider.test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.trentorise.smartcampus.permissionprovider.common.RegistrationException;
import eu.trentorise.smartcampus.permissionprovider.manager.MailSender;
import eu.trentorise.smartcampus.permissionprovider.manager.RegistrationManager;
import eu.trentorise.smartcampus.permissionprovider.manager.RegistrationService;
import eu.trentorise.smartcampus.permissionprovider.model.Registration;
import eu.trentorise.smartcampus.permissionprovider.model.User;
import eu.trentorise.smartcampus.permissionprovider.repository.RegistrationRepository;
import eu.trentorise.smartcampus.permissionprovider.repository.UserRepository;
import junit.framework.Assert;

/**
 * @author raman
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext.xml")
public class UserManagementTest {

	private static final String NAME = "TESTNAME";
	private static final String SURNAME = "TESTSURNAME";
	private static final String PWD = "123456";
	private static final String FULLNAME = NAME+" "+SURNAME;
	private static final String EMAIL = "test.test.registration@test.com";

	@Mock
	private MailSender mailSender;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RegistrationRepository regRepository;

	@Autowired
	@InjectMocks
	private RegistrationManager regManager;

	@Autowired
	private RegistrationService service;
	
	
	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
		RegistrationManager rm = (RegistrationManager) unwrapProxy(regManager);
		ReflectionTestUtils.setField(rm, "sender", mailSender);
		
		List<User> users = userRepository.findByFullNameLike(FULLNAME);
		if (users != null) {
			userRepository.delete(users);
		}
		regRepository.delete(regRepository.findAllByEmail(EMAIL));
	}

	@Test
	public void testRegistration() {
		try {
			Registration register = service.register(NAME, SURNAME, EMAIL, PWD, null);
			Assert.assertNotNull(register);
			
			register = regRepository.findByEmail(EMAIL);
			Assert.assertNotNull(register);
			
		} catch (RegistrationException e) {
			Assert.assertTrue("Error testing registration", false);
		}
	}

	@Test
	public void testRegistrationFailureMail() {
		try {
			Registration register = regRepository.findByEmail(EMAIL);
			Assert.assertNull(register);

			Mockito.doThrow(new RegistrationException());
			register = service.register(NAME, SURNAME, EMAIL, PWD, null);
			
			
		} catch (RegistrationException e) {
			Registration register = regRepository.findByEmail(EMAIL);
			Assert.assertNull(register);
		}
	}
	@Test
	public void testRegistrationInParallel() {
		final CountDownLatch lock = new CountDownLatch(2);
		
		Runnable body = new Runnable(){
			@Override
			public void run() {
				try {
					service.register(NAME, SURNAME, EMAIL, PWD, null);
					List<Registration> list = regRepository.findAllByEmail(EMAIL);
					Thread.sleep(1000L);
					Assert.assertTrue(list.size() == 1);
					lock.countDown();
				} catch (InterruptedException e) {
					// DO NOTHING
					lock.countDown();
				} catch (Exception e) {
					// DO NOTHING
					e.printStackTrace();
				}
			}
			
		}; 
		new Thread(body).start();
		new Thread(body).start();
		
		try {
			if (!lock.await(3000, TimeUnit.MILLISECONDS)) {
				Assert.assertTrue("Error testing parallel registration", false);				
			}
		} catch (InterruptedException e) {
			// DO NOTHING
		}
	}
	
	public static final Object unwrapProxy(Object bean) throws Exception {
	    /*
	     * If the given object is a proxy, set the return value as the object
	     * being proxied, otherwise return the given object.
	     */
	    if (AopUtils.isAopProxy(bean) && bean instanceof Advised) {
	        Advised advised = (Advised) bean;
	        bean = advised.getTargetSource().getTarget();
	    }
	    return bean;
	}
	
}
