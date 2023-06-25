package org.eclipse.daanse.mdx.parser.ccc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.daanse.mdx.model.api.ReturnItem;
import org.eclipse.daanse.mdx.model.api.expression.CompoundId;
import org.eclipse.daanse.mdx.model.api.expression.NameObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.cccx.MdxParserWrapper;
import org.junit.jupiter.api.Test;

class ReturnItemTest {

	@Test
	void test() throws MdxParserException {
		List<? extends ReturnItem> clauseList = new MdxParserWrapper("[a].[b]").parseReturnItems();
		assertThat(clauseList).isNotNull().hasSize(1);
		CompoundId compoundId = clauseList.get(0).compoundId();
		assertThat(compoundId.objectIdentifiers()).hasSize(2);
		assertThat(compoundId.objectIdentifiers().get(0)).isInstanceOf(NameObjectIdentifier.class);
		assertThat(compoundId.objectIdentifiers().get(1)).isInstanceOf(NameObjectIdentifier.class);
		NameObjectIdentifier nameObjectIdentifier1 = (NameObjectIdentifier) compoundId.objectIdentifiers().get(0);
		NameObjectIdentifier nameObjectIdentifier2 = (NameObjectIdentifier) compoundId.objectIdentifiers().get(1);
		assertThat(nameObjectIdentifier1.name()).isEqualTo("a");
		assertThat(nameObjectIdentifier1.quoting()).isEqualTo(ObjectIdentifier.Quoting.QUOTED);
		assertThat(nameObjectIdentifier2.name()).isEqualTo("b");
		assertThat(nameObjectIdentifier2.quoting()).isEqualTo(ObjectIdentifier.Quoting.QUOTED);
	}
}
