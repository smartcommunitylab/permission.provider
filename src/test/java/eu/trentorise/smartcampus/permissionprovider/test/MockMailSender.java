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

import java.io.IOException;
import java.util.Map;

import eu.trentorise.smartcampus.permissionprovider.common.RegistrationException;
import eu.trentorise.smartcampus.permissionprovider.manager.MailSender;

/**
 * @author raman
 *
 */
public class MockMailSender extends MailSender {

	public MockMailSender() throws IOException {
		super();
	}

	@Override
	public void init() throws IOException {
		// DO NOTHING
	}

	@Override
	public void sendEmail(String email, String template, String subject, Map<String, Object> vars) throws RegistrationException {
		// DO NOTHING
	}

	
}
