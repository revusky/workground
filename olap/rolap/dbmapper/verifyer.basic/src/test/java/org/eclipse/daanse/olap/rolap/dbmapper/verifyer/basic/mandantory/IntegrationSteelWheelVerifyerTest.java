package org.eclipse.daanse.olap.rolap.dbmapper.verifyer.basic.mandantory;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.daanse.olap.rolap.dbmapper.model.api.Schema;
import org.eclipse.daanse.olap.rolap.dbmapper.provider.api.DbMappingSchemaProvider;
import org.eclipse.daanse.olap.rolap.dbmapper.verifyer.api.Verifyer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
class IntegrationSteelWheelVerifyerTest {

	public static final String COMPONENT_NAME = "org.eclipse.daanse.olap.rolap.dbmapper.verifyer.basic.mandantory"
			+ ".MandantoriesVerifyer";
	@InjectService(filter = "(component.name=" + COMPONENT_NAME + ")")
	Verifyer verifyer;

	@Test
	void testSteelWheelsXml(
			@InjectService(filter = "(&(sample.type=xml)(sample.name=SteelWheels))") DbMappingSchemaProvider provider)
			throws CloneNotSupportedException {
		doTest(provider);
	}

	@Test
	void testSteelWheelsRecord(
			@InjectService(filter = "(&(sample.type=record)(sample.name=SteelWheels))") DbMappingSchemaProvider provider)
			throws CloneNotSupportedException {
		doTest(provider);
	}

	private void doTest(DbMappingSchemaProvider provider) {
		Schema schema = provider.get();
		assertThat(schema).isNotNull();
	}
}
