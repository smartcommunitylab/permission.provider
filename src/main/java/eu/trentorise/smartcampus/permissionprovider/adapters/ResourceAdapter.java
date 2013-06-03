/**
 *    Copyright 2012-2013 Trento RISE
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
 */

package eu.trentorise.smartcampus.permissionprovider.adapters;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.Resources;
import eu.trentorise.smartcampus.permissionprovider.model.Resource;
import eu.trentorise.smartcampus.permissionprovider.oauth.ResourceStorage;

/**
 * @author raman
 *
 */
@Component
public class ResourceAdapter {

	private static Log logger = LogFactory.getLog(ResourceAdapter.class);
	@Autowired
	private ResourceStorage resourceStorage;
	
	@PostConstruct 
	public void init() {
		List<Resource> resources = loadDefaultResources();
		resourceStorage.storeResources(resources); 
	}
	
	/**
	 * @return
	 */
	private List<Resource> loadDefaultResources() {
		// TODO change implementation:
		// - load resource templates
		// - store completely matched resources
		// - create mapping for parametric templates 
		try {
			JAXBContext jaxb = JAXBContext.newInstance(Resource.class,
					Resources.class);
			Unmarshaller unm = jaxb.createUnmarshaller();
			JAXBElement<Resources> element = (JAXBElement<Resources>) unm
					.unmarshal(
							new StreamSource(getClass().getResourceAsStream(
									"resources.xml")), Resources.class);
			return element.getValue().getResource();
		} catch (JAXBException e) {
			logger.error("Failed to load default resources: "+e.getMessage(),e);
			return Collections.emptyList();
		}
	}
}
