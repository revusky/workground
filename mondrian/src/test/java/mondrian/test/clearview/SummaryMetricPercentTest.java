/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/
package mondrian.test.clearview;

import org.junit.jupiter.params.ParameterizedTest;
import org.opencube.junit5.ContextSource;
import org.opencube.junit5.context.TestingContext;
import org.opencube.junit5.dataloader.FastFoodmardDataLoader;
import org.opencube.junit5.propupdator.AppandFoodMartCatalogAsFile;

import mondrian.olap.MondrianProperties;
import mondrian.test.DiffRepository;
import mondrian.util.Bug;

/**
 * <code>SummaryMetricPercentTest</code> is a test suite which tests scenarios
 * of computing sums and percentages against the FoodMart database.
 * MDX queries and their expected results are maintained separately in
 * SummaryMetricPercentTest.ref.xml file.If you would prefer to see them as
 * inlined Java string literals, run ant target "generateDiffRepositoryJUnit"
 * and then use file SummaryMetricPercentTestJUnit.java which will be
 * generated in this directory.
 *
 * @author Khanh Vu
 */
public class SummaryMetricPercentTest extends ClearViewBase {

    @Override
	public DiffRepository getDiffRepos() {
        return getDiffReposStatic();
    }

    private static DiffRepository getDiffReposStatic() {
        return DiffRepository.lookup(SummaryMetricPercentTest.class);
    }


    @Override
	@ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    protected void runTest(TestingContext context) {
        DiffRepository diffRepos = getDiffRepos();
        for (String name : diffRepos.getTestCaseNames()) {
            setName(name);
            diffRepos.setCurrentTestCaseName(name);

            if (!Bug.BugMondrian2452Fixed
                    && (getName().equals("testSpecialMetricPctOfCol"))
                    && !MondrianProperties.instance().EnableNativeCrossJoin.get()) {
                // Tests give wrong results if native crossjoin is disabled.
                return;
            }
            super.runTest(context);
        }
    }
}
