/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.mdx.parser.ccc;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.daanse.mdx.model.api.MdxStatement;
import org.eclipse.daanse.mdx.model.record.SelectStatementR;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.cccx.MdxParserWrapper;
import org.junit.jupiter.api.Test;

class LargeStatementTest {

	static String MDX = """
			            WITH
            /* This is a nested comment. Outer part.
			         /* Inner comment */
				Outer again.
			*/
			MEMBER [Customer].[Customer Geography].[Weight of Measure] AS CASE
			WHEN [Measures].CurrentMember IS [Measures].[Internet Sales Amount] THEN Round(1/2, 4)
			WHEN [Measures].CurrentMember IS [Measures].[Internet Tax Amount] THEN Round(1/2, 4)
			 ELSE NULL
			END, FORMAT_STRING = "Percent"
			MEMBER [Customer].[Customer Geography].[Normative of Measure] AS CASE
			WHEN [Measures].CurrentMember IS [Measures].[Internet Sales Amount] THEN 0
			WHEN [Measures].CurrentMember IS [Measures].[Internet Tax Amount] THEN 0
			 ELSE NULL
			END
			MEMBER [Customer].[Customer Geography].[Trend of Measure] AS CASE
			WHEN [Measures].CurrentMember IS [Measures].[Internet Sales Amount] THEN 1
			WHEN [Measures].CurrentMember IS [Measures].[Internet Tax Amount] THEN 1
			 ELSE NULL
			END
			MEMBER [Measures].[Cumulative, Internet Sales Amount] AS [Measures].[Internet Sales Amount] + Iif([Measures].[Rank, Internet Sales Amount] = 0, 0, (Subset(Order([City: Rank by Internet Sales Amount], [Measures].[Internet Sales Amount], BDesc), [Measures].[Rank, Internet Sales Amount]-2, 1).Item(0), [Measures].[Cumulative, Internet Sales Amount]) ), CAPTION = 'Cumulative, Internet Sales Amount', FORMAT_STRING = "Standard"
			MEMBER [Measures].[% Of Max, Internet Sales Amount] AS Round(([Measures].[Internet Sales Amount], [Customer].[Customer Geography].CurrentMember)/([Measures].[Internet Sales Amount], [Customer].[Customer Geography].[Head 100, High]), 4), CAPTION = '% Of Max, Internet Sales Amount', SOLVE_ORDER = 5, FORMAT_STRING = "Percent"
			MEMBER [Measures].[% Of Top Total, Internet Sales Amount] AS Round(([Measures].[Internet Sales Amount], [Customer].[Customer Geography].CurrentMember)/([Measures].[Internet Sales Amount], [Customer].[Customer Geography].[Head 100, Total]), 4), CAPTION = '% Of Top Total, Internet Sales Amount', SOLVE_ORDER = 5, FORMAT_STRING = "Percent"
			MEMBER [Measures].[% Of Grand total, Internet Sales Amount] AS Round(([Customer].[Customer Geography].CurrentMember, [Measures].[Internet Sales Amount])/([Customer].[Customer Geography].[GRAND TOTAL], [Measures].[Internet Sales Amount]), 4), CAPTION = '% Of Grand total, Internet Sales Amount', FORMAT_STRING = "Percent"
			MEMBER [Measures].[% Cumulative, Internet Sales Amount] AS Round(([Customer].[Customer Geography].CurrentMember, [Measures].[Cumulative, Internet Sales Amount])/([Customer].[Customer Geography].[GRAND TOTAL], [Measures].[Cumulative, Internet Sales Amount]), 4), CAPTION = '% Cumulative, Internet Sales Amount', FORMAT_STRING = "Percent"
			MEMBER [Measures].[Rank, Internet Sales Amount] AS Rank([Customer].[Customer Geography].CurrentMember, Order([City: Rank by Internet Sales Amount], [Measures].[Internet Sales Amount], BDesc)), CAPTION = 'Rank, Internet Sales Amount', FORMAT_STRING = "#"
			MEMBER [Measures].[ABC*, Internet Sales Amount] AS Iif(([Customer].[Customer Geography].CurrentMember, [Measures].[Internet Sales Amount]) * Count(Order([City: Rank by Internet Sales Amount], [Measures].[Internet Sales Amount], BDesc)) >= ([Customer].[Customer Geography].[GRAND TOTAL], [Measures].[Internet Sales Amount]), 'AB', 'BC'), CAPTION = 'ABC*, Internet Sales Amount', SOLVE_ORDER = 5
			MEMBER [Measures].[ABC, Internet Sales Amount] AS CASE
			WHEN (Intersect([Customer].[Customer Geography].CurrentMember, [City: Class A]).Count > 0) THEN "A"
			WHEN (Intersect([Customer].[Customer Geography].CurrentMember, [City: Class B]).Count > 0) THEN "B"
			WHEN (Intersect([Customer].[Customer Geography].CurrentMember, [City: Class C]).Count > 0) THEN "C"
			ELSE "D"
			END, CAPTION = 'ABC, Internet Sales Amount', SOLVE_ORDER = 4, BACK_COLOR = Iif([Measures].[ABC, Internet Sales Amount]='A', RGB(144,238,144), Iif([Measures].[ABC, Internet Sales Amount]='B', RGB(255,255,224), Iif([Measures].[ABC, Internet Sales Amount]='C', RGB(255,182,193), Iif([Measures].[ABC, Internet Sales Amount]='D', RGB(135,206,250), RGB(255,255,255))))), FORE_COLOR = RGB(0,0,0)
			MEMBER [Measures].[Std. Dev., Internet Sales Amount] AS StdevP([Calendar Quarter: Time series], CoalesceEmpty([Measures].[Internet Sales Amount],0)), CAPTION = 'Std. Dev., Internet Sales Amount', FORMAT_STRING = "Standard"
			MEMBER [Measures].[Mean, Internet Sales Amount] AS Sum([Calendar Quarter: Time series], CoalesceEmpty([Measures].[Internet Sales Amount],0))/Count([Calendar Quarter: Time series]), CAPTION = 'Mean, Internet Sales Amount', FORMAT_STRING = "Standard"
			MEMBER [Measures].[% CV, Internet Sales Amount] AS Iif([Measures].[Mean, Internet Sales Amount] = 0, NULL, [Measures].[Std. Dev., Internet Sales Amount]/[Measures].[Mean, Internet Sales Amount]), CAPTION = '% CV, Internet Sales Amount', SOLVE_ORDER = 5, FORMAT_STRING = "Percent", BACK_COLOR = Iif([Measures].[XYZ, Internet Sales Amount]='X', RGB(144,238,144), Iif([Measures].[XYZ, Internet Sales Amount]='Y', RGB(255,255,224), Iif([Measures].[XYZ, Internet Sales Amount]='Z', RGB(255,182,193), Iif([Measures].[XYZ, Internet Sales Amount]='Z', RGB(135,206,250), RGB(255,255,255))))), FORE_COLOR = RGB(0,0,0)
			MEMBER [Measures].[Rank, % CV, Internet Sales Amount] AS Rank([Customer].[Customer Geography].CurrentMember, Order([City: Source Set], [Measures].[% CV, Internet Sales Amount], BAsc))-1, CAPTION = 'Rank, % CV, Internet Sales Amount'
			MEMBER [Measures].[XYZ, Internet Sales Amount] AS CASE
			WHEN (Intersect([Customer].[Customer Geography].CurrentMember, [City: Class X]).Count > 0) THEN "X"
			WHEN (Intersect([Customer].[Customer Geography].CurrentMember, [City: Class Y]).Count > 0) THEN "Y"
			WHEN (Intersect([Customer].[Customer Geography].CurrentMember, [City: Class Z]).Count > 0) THEN "Z"
			ELSE "N"
			END, CAPTION = 'XYZ, Internet Sales Amount', SOLVE_ORDER = 6, BACK_COLOR = Iif([Measures].[XYZ, Internet Sales Amount]='X', RGB(144,238,144), Iif([Measures].[XYZ, Internet Sales Amount]='Y', RGB(255,255,224), Iif([Measures].[XYZ, Internet Sales Amount]='Z', RGB(255,182,193), Iif([Measures].[XYZ, Internet Sales Amount]='Z', RGB(135,206,250), RGB(255,255,255))))), FORE_COLOR = RGB(0,0,0)
			MEMBER [Measures].[PrevP Var., Internet Sales Amount] AS [Measures].[Internet Sales Amount] - (ParallelPeriod([Date].[Calendar].[Calendar Quarter], 1, [Date].[Calendar].CurrentMember), [Measures].[Internet Sales Amount]), CAPTION = 'PrevP Var., Internet Sales Amount', BACK_COLOR = Iif([Measures].[PrevP Var., Internet Sales Amount]<0, RGB(255,182,193), Iif([Measures].[PrevP Var., Internet Sales Amount]>0, RGB(144,238,144), Iif([Measures].[PrevP Var., Internet Sales Amount]=NULL, RGB(255,255,255), RGB(255,255,224)))), FORE_COLOR = RGB(0,0,0)
			MEMBER [Measures].[PoP Var., Internet Sales Amount] AS [Measures].[Internet Sales Amount] - (ParallelPeriod([Date].[Calendar].[Calendar Quarter], 1, [Date].[Calendar].CurrentMember), [Measures].[Internet Sales Amount]), CAPTION = 'PoP Var., Internet Sales Amount', BACK_COLOR = Iif([Measures].[PoP Var., Internet Sales Amount]<0, RGB(255,182,193), Iif([Measures].[PoP Var., Internet Sales Amount]>0, RGB(144,238,144), Iif([Measures].[PoP Var., Internet Sales Amount]=NULL, RGB(255,255,255), RGB(255,255,224)))), FORE_COLOR = RGB(0,0,0)
			MEMBER [Measures].[Sparklile Internet Sales Amount by Periods] AS Iif(NonEmpty([Calendar Quarter: Time series], ([Measures].[Internet Sales Amount])).Count > 0, Generate([Calendar Quarter: Time series], ([Date].[Calendar].CurrentMember,[Measures].[Internet Sales Amount]), ' | '), NULL), CAPTION = 'Sparklile Internet Sales Amount by Periods', SOLVE_ORDER = 5, FORMAT_STRING = "Sparkline"
			MEMBER [Date].[Calendar].[ABC-XYZ Analysis Indicators, Internet Sales Amount] AS Aggregate ([Calendar Quarter: Time series], [Measures].CurrentMember)
			MEMBER [Measures].[ABC-XYZ Combined, Internet Sales Amount] AS [Measures].[ABC, Internet Sales Amount]+[Measures].[XYZ, Internet Sales Amount], CAPTION = 'ABC-XYZ Combined, Internet Sales Amount', SOLVE_ORDER = 4
			MEMBER [Measures].[GRAND TOTAL, Internet Sales Amount] AS Aggregate([City: Rank by Internet Sales Amount], [Measures].[Internet Sales Amount]), CAPTION = 'GRAND TOTAL, Internet Sales Amount', FORMAT_STRING = "Standard"
			MEMBER [Measures].[Estimated Rating, Internet Sales Amount, %] AS (([Customer].[Customer Geography].CurrentMember, [Measures].[Internet Sales Amount]) - Min({[Customer].[Customer Geography].[Normative of Measure], [City: Rank by Internet Sales Amount]}, [Measures].[Internet Sales Amount])) / (Max({[Customer].[Customer Geography].[Normative of Measure], [City: Rank by Internet Sales Amount]}, [Measures].[Internet Sales Amount]) - Min({[Customer].[Customer Geography].[Normative of Measure], [City: Rank by Internet Sales Amount]}, [Measures].[Internet Sales Amount])), CAPTION = 'Estimated Rating, Internet Sales Amount, %', FORMAT_STRING = "Percent"
			MEMBER [Measures].[Cumulative, Internet Tax Amount] AS [Measures].[Internet Tax Amount] + Iif([Measures].[Rank, Internet Tax Amount] = 0, 0, (Subset(Order([City: Rank by Internet Tax Amount], [Measures].[Internet Tax Amount], BDesc), [Measures].[Rank, Internet Tax Amount]-2, 1).Item(0), [Measures].[Cumulative, Internet Tax Amount]) ), CAPTION = 'Cumulative, Internet Tax Amount', FORMAT_STRING = "Standard"
			MEMBER [Measures].[% Of Max, Internet Tax Amount] AS Round(([Measures].[Internet Tax Amount], [Customer].[Customer Geography].CurrentMember)/([Measures].[Internet Tax Amount], [Customer].[Customer Geography].[Head 100, High]), 4), CAPTION = '% Of Max, Internet Tax Amount', SOLVE_ORDER = 5, FORMAT_STRING = "Percent"
			MEMBER [Measures].[% Of Top Total, Internet Tax Amount] AS Round(([Measures].[Internet Tax Amount], [Customer].[Customer Geography].CurrentMember)/([Measures].[Internet Tax Amount], [Customer].[Customer Geography].[Head 100, Total]), 4), CAPTION = '% Of Top Total, Internet Tax Amount', SOLVE_ORDER = 5, FORMAT_STRING = "Percent"
			MEMBER [Measures].[% Of Grand total, Internet Tax Amount] AS Round(([Customer].[Customer Geography].CurrentMember, [Measures].[Internet Tax Amount])/([Customer].[Customer Geography].[GRAND TOTAL], [Measures].[Internet Tax Amount]), 4), CAPTION = '% Of Grand total, Internet Tax Amount', FORMAT_STRING = "Percent"
			MEMBER [Measures].[% Cumulative, Internet Tax Amount] AS Round(([Customer].[Customer Geography].CurrentMember, [Measures].[Cumulative, Internet Tax Amount])/([Customer].[Customer Geography].[GRAND TOTAL], [Measures].[Cumulative, Internet Tax Amount]), 4), CAPTION = '% Cumulative, Internet Tax Amount', FORMAT_STRING = "Percent"
			MEMBER [Measures].[Rank, Internet Tax Amount] AS Rank([Customer].[Customer Geography].CurrentMember, Order([City: Rank by Internet Tax Amount], [Measures].[Internet Tax Amount], BDesc)), CAPTION = 'Rank, Internet Tax Amount', FORMAT_STRING = "#"
			MEMBER [Measures].[ABC*, Internet Tax Amount] AS Iif(([Customer].[Customer Geography].CurrentMember, [Measures].[Internet Tax Amount]) * Count(Order([City: Rank by Internet Tax Amount], [Measures].[Internet Tax Amount], BDesc)) >= ([Customer].[Customer Geography].[GRAND TOTAL], [Measures].[Internet Tax Amount]), 'AB', 'BC'), CAPTION = 'ABC*, Internet Tax Amount', SOLVE_ORDER = 5
			MEMBER [Measures].[ABC, Internet Tax Amount] AS Iif([Measures].[% Cumulative, Internet Tax Amount] > 1, 'D', Iif([Measures].[% Cumulative, Internet Tax Amount] > 0.95, 'C', Iif([Measures].[% Cumulative, Internet Tax Amount] > 0.8, 'B', 'A'))), CAPTION = 'ABC, Internet Tax Amount', SOLVE_ORDER = 4, BACK_COLOR = Iif([Measures].[ABC, Internet Tax Amount]='A', RGB(144,238,144), Iif([Measures].[ABC, Internet Tax Amount]='B', RGB(255,255,224), Iif([Measures].[ABC, Internet Tax Amount]='C', RGB(255,182,193), Iif([Measures].[ABC, Internet Tax Amount]='D', RGB(135,206,250), RGB(255,255,255))))), FORE_COLOR = RGB(0,0,0)
			MEMBER [Measures].[Std. Dev., Internet Tax Amount] AS StdevP([Calendar Quarter: Time series], CoalesceEmpty([Measures].[Internet Tax Amount],0)), CAPTION = 'Std. Dev., Internet Tax Amount', FORMAT_STRING = "Standard"
			MEMBER [Measures].[Mean, Internet Tax Amount] AS Sum([Calendar Quarter: Time series], CoalesceEmpty([Measures].[Internet Tax Amount],0))/Count([Calendar Quarter: Time series]), CAPTION = 'Mean, Internet Tax Amount', FORMAT_STRING = "Standard"
			MEMBER [Measures].[% CV, Internet Tax Amount] AS Iif([Measures].[Mean, Internet Tax Amount] = 0, NULL, [Measures].[Std. Dev., Internet Tax Amount]/[Measures].[Mean, Internet Tax Amount]), CAPTION = '% CV, Internet Tax Amount', SOLVE_ORDER = 5, FORMAT_STRING = "Percent", BACK_COLOR = Iif([Measures].[XYZ, Internet Tax Amount]='X', RGB(144,238,144), Iif([Measures].[XYZ, Internet Tax Amount]='Y', RGB(255,255,224), Iif([Measures].[XYZ, Internet Tax Amount]='Z', RGB(255,182,193), Iif([Measures].[XYZ, Internet Tax Amount]='Z', RGB(135,206,250), RGB(255,255,255))))), FORE_COLOR = RGB(0,0,0)
			MEMBER [Measures].[Rank, % CV, Internet Tax Amount] AS Rank([Customer].[Customer Geography].CurrentMember, Order([City: Source Set], [Measures].[% CV, Internet Tax Amount], BAsc))-1, CAPTION = 'Rank, % CV, Internet Tax Amount'
			MEMBER [Measures].[XYZ, Internet Tax Amount] AS CASE
			WHEN (Intersect([Customer].[Customer Geography].CurrentMember, [City: Class X]).Count > 0) THEN "X"
			WHEN (Intersect([Customer].[Customer Geography].CurrentMember, [City: Class Y]).Count > 0) THEN "Y"
			WHEN (Intersect([Customer].[Customer Geography].CurrentMember, [City: Class Z]).Count > 0) THEN "Z"
			ELSE "N"
			END, CAPTION = 'XYZ, Internet Tax Amount', SOLVE_ORDER = 6, BACK_COLOR = Iif([Measures].[XYZ, Internet Tax Amount]='X', RGB(144,238,144), Iif([Measures].[XYZ, Internet Tax Amount]='Y', RGB(255,255,224), Iif([Measures].[XYZ, Internet Tax Amount]='Z', RGB(255,182,193), Iif([Measures].[XYZ, Internet Tax Amount]='Z', RGB(135,206,250), RGB(255,255,255))))), FORE_COLOR = RGB(0,0,0)
			MEMBER [Measures].[PrevP Var., Internet Tax Amount] AS [Measures].[Internet Tax Amount] - (ParallelPeriod([Date].[Calendar].[Calendar Quarter], 1, [Date].[Calendar].CurrentMember), [Measures].[Internet Tax Amount]), CAPTION = 'PrevP Var., Internet Tax Amount', BACK_COLOR = Iif([Measures].[PrevP Var., Internet Tax Amount]<0, RGB(255,182,193), Iif([Measures].[PrevP Var., Internet Tax Amount]>0, RGB(144,238,144), Iif([Measures].[PrevP Var., Internet Tax Amount]=NULL, RGB(255,255,255), RGB(255,255,224)))), FORE_COLOR = RGB(0,0,0)
			MEMBER [Measures].[PoP Var., Internet Tax Amount] AS [Measures].[Internet Tax Amount] - (ParallelPeriod([Date].[Calendar].[Calendar Quarter], 1, [Date].[Calendar].CurrentMember), [Measures].[Internet Tax Amount]), CAPTION = 'PoP Var., Internet Tax Amount', BACK_COLOR = Iif([Measures].[PoP Var., Internet Tax Amount]<0, RGB(255,182,193), Iif([Measures].[PoP Var., Internet Tax Amount]>0, RGB(144,238,144), Iif([Measures].[PoP Var., Internet Tax Amount]=NULL, RGB(255,255,255), RGB(255,255,224)))), FORE_COLOR = RGB(0,0,0)
			MEMBER [Measures].[Sparklile Internet Tax Amount by Periods] AS Iif(NonEmpty([Calendar Quarter: Time series], ([Measures].[Internet Tax Amount])).Count > 0, Generate([Calendar Quarter: Time series], ([Date].[Calendar].CurrentMember,[Measures].[Internet Tax Amount]), ' | '), NULL), CAPTION = 'Sparklile Internet Tax Amount by Periods', SOLVE_ORDER = 5, FORMAT_STRING = "Sparkline"
			MEMBER [Date].[Calendar].[ABC-XYZ Analysis Indicators, Internet Tax Amount] AS Aggregate ([Calendar Quarter: Time series], [Measures].CurrentMember)
			MEMBER [Measures].[ABC-XYZ Combined, Internet Tax Amount] AS [Measures].[ABC, Internet Tax Amount]+[Measures].[XYZ, Internet Tax Amount], CAPTION = 'ABC-XYZ Combined, Internet Tax Amount', SOLVE_ORDER = 4
			MEMBER [Measures].[GRAND TOTAL, Internet Tax Amount] AS Aggregate([City: Rank by Internet Tax Amount], [Measures].[Internet Tax Amount]), CAPTION = 'GRAND TOTAL, Internet Tax Amount', FORMAT_STRING = "Standard"
			MEMBER [Measures].[Estimated Rating, Internet Tax Amount, %] AS (([Customer].[Customer Geography].CurrentMember, [Measures].[Internet Tax Amount]) - Min({[Customer].[Customer Geography].[Normative of Measure], [City: Rank by Internet Tax Amount]}, [Measures].[Internet Tax Amount])) / (Max({[Customer].[Customer Geography].[Normative of Measure], [City: Rank by Internet Tax Amount]}, [Measures].[Internet Tax Amount]) - Min({[Customer].[Customer Geography].[Normative of Measure], [City: Rank by Internet Tax Amount]}, [Measures].[Internet Tax Amount])), CAPTION = 'Estimated Rating, Internet Tax Amount, %', FORMAT_STRING = "Percent"
			MEMBER [Measures].[Hamburg Score, %] AS [Measures].[Estimated Rating, Internet Sales Amount, %] * ([Customer].[Customer Geography].[Weight of Measure], [Measures].[Internet Sales Amount]) + [Measures].[Estimated Rating, Internet Tax Amount, %] * ([Customer].[Customer Geography].[Weight of Measure], [Measures].[Internet Tax Amount]), CAPTION = 'Hamburg Score, %', SOLVE_ORDER = 4, FORMAT_STRING = "Percent"
			MEMBER [Measures].[Wighted estimate] AS [Measures].[Internet Sales Amount] * ([Customer].[Customer Geography].[Weight of Measure], [Measures].[Internet Sales Amount]) + [Measures].[Internet Tax Amount] * ([Customer].[Customer Geography].[Weight of Measure], [Measures].[Internet Tax Amount]), CAPTION = 'Wighted estimate', SOLVE_ORDER = 4, FORMAT_STRING = "#,#.00"
			MEMBER [Measures].[Rating on Ranks] AS [Measures].[Rank, Internet Sales Amount] * ([Customer].[Customer Geography].[Weight of Measure], [Measures].[Internet Sales Amount]) + [Measures].[Rank, Internet Tax Amount] * ([Customer].[Customer Geography].[Weight of Measure], [Measures].[Internet Tax Amount]), CAPTION = 'Rating on Ranks', SOLVE_ORDER = 4, FORMAT_STRING = "#,#.00"
			MEMBER [Measures].[Ranks, Sparkline] AS str([Measures].[Rank, Internet Sales Amount]) + " | " + str([Measures].[Rank, Internet Tax Amount]), CAPTION = 'Ranks, Sparkline', SOLVE_ORDER = 4, FORMAT_STRING = "Sparkline"
			MEMBER [Measures].[ABC Combined] AS [Measures].[ABC, Internet Sales Amount] + [Measures].[ABC, Internet Tax Amount], CAPTION = 'ABC Combined', SOLVE_ORDER = 4
			MEMBER [Measures].[XYZ Combined] AS [Measures].[XYZ, Internet Sales Amount] + [Measures].[XYZ, Internet Tax Amount], CAPTION = 'XYZ Combined', SOLVE_ORDER = 4
			MEMBER [Customer].[Customer Geography].[GRAND TOTAL] AS Aggregate([City: Source Set])
			MEMBER [Date].[Calendar].[GRAND TOTAL] AS Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[A], Aggregate(Intersect([City: Source Set], [City: Class A]), [Date].[Calendar].Members.Item(0)),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[B], Aggregate(Intersect([City: Source Set], [City: Class B]), [Date].[Calendar].Members.Item(0)),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[C], Aggregate(Intersect([City: Source Set], [City: Class C]), [Date].[Calendar].Members.Item(0)),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[D], Aggregate(Intersect([City: Source Set], [City: Class D]), [Date].[Calendar].Members.Item(0)),
			Aggregate([City: Source Set], [Date].[Calendar].Members.Item(0))))))
			MEMBER [Customer].[Customer Geography].[A] AS Aggregate([City: Class A])
			MEMBER [Customer].[Customer Geography].[B] AS Aggregate([City: Class B])
			MEMBER [Customer].[Customer Geography].[C] AS Aggregate([City: Class C])
			MEMBER [Customer].[Customer Geography].[D] AS Aggregate([City: Class D])
			MEMBER [Measures].[% of Total ABC Class] AS Round(([Customer].[Customer Geography].CurrentMember, [Date].[Calendar].[GRAND TOTAL], [Measures].[Internet Sales Amount])/([Customer].[Customer Geography].[GRAND TOTAL], [Date].[Calendar].[GRAND TOTAL], [Measures].[Internet Sales Amount]), 4), CAPTION = '% of Total ABC Class', FORMAT_STRING = "Percent"
			MEMBER [Measures].[Count Members in ABC Class] AS Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[A], Count([City: Class A]),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[B], Count([City: Class B]),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[C], Count([City: Class C]),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[D], Count([City: Class D]),
			COUNT([City: Source Set]))))), CAPTION = 'Count Members in ABC Class'
			MEMBER [Measures].[% Count Members in ABC Class] AS Round(([Customer].[Customer Geography].CurrentMember, [Date].[Calendar].[GRAND TOTAL], [Measures].[Count Members in ABC Class])/([Customer].[Customer Geography].[GRAND TOTAL], [Date].[Calendar].[GRAND TOTAL], [Measures].[Count Members in ABC Class]), 4), CAPTION = '% Count Members in ABC Class', FORMAT_STRING = "Percent"
			MEMBER [Measures].[Breakdown of ABC Classes] AS Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[A], Round(0.8, 4),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[B], Round(0.15, 4),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[C], Round(0.05, 4),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[D], Round(0, 4),
			1)))), CAPTION = 'Breakdown of ABC Classes', FORMAT_STRING = "Percent"
			MEMBER [Date].[Calendar].[X] AS Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[A], Aggregate(Intersect([City: Class X], [City: Class A]), [Date].[Calendar].Members.Item(0)),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[B], Aggregate(Intersect([City: Class X], [City: Class B]), [Date].[Calendar].Members.Item(0)),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[C], Aggregate(Intersect([City: Class X], [City: Class C]), [Date].[Calendar].Members.Item(0)),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[D], Aggregate(Intersect([City: Class X], [City: Class D]), [Date].[Calendar].Members.Item(0)),
			Aggregate([City: Class X], [Date].[Calendar].Members.Item(0))))))
			MEMBER [Date].[Calendar].[Y] AS Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[A], Aggregate(Intersect([City: Class Y], [City: Class A]), [Date].[Calendar].Members.Item(0)),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[B], Aggregate(Intersect([City: Class Y], [City: Class B]), [Date].[Calendar].Members.Item(0)),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[C], Aggregate(Intersect([City: Class Y], [City: Class C]), [Date].[Calendar].Members.Item(0)),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[D], Aggregate(Intersect([City: Class Y], [City: Class D]), [Date].[Calendar].Members.Item(0)),
			Aggregate([City: Class Y], [Date].[Calendar].Members.Item(0))))))
			MEMBER [Date].[Calendar].[Z] AS Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[A], Aggregate(Intersect([City: Class Z], [City: Class A]), [Date].[Calendar].Members.Item(0)),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[B], Aggregate(Intersect([City: Class Z], [City: Class B]), [Date].[Calendar].Members.Item(0)),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[C], Aggregate(Intersect([City: Class Z], [City: Class C]), [Date].[Calendar].Members.Item(0)),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[D], Aggregate(Intersect([City: Class Z], [City: Class D]), [Date].[Calendar].Members.Item(0)),
			Aggregate([City: Class Z], [Date].[Calendar].Members.Item(0))))))
			MEMBER [Measures].[% of Total XYZ Class] AS Round(([Date].[Calendar].CurrentMember, [Measures].[Internet Sales Amount])/([Date].[Calendar].[GRAND TOTAL], [Measures].[Internet Sales Amount]),4), CAPTION = '% of Total XYZ Class', FORMAT_STRING = "Percent"
			MEMBER [Measures].[% of Total ABC-XYZ Class] AS Round(([Date].[Calendar].CurrentMember, [Customer].[Customer Geography].CurrentMember, [Measures].[Internet Sales Amount])/([Date].[Calendar].[GRAND TOTAL], [Customer].[Customer Geography].[GRAND TOTAL], [Measures].[Internet Sales Amount]), 4), CAPTION = '% of Total ABC-XYZ Class', FORMAT_STRING = "Percent"
			MEMBER [Measures].[Count Members in XYZ Class] AS Iif([Date].[Calendar].CurrentMember IS [Date].[Calendar].[X], Count([City: Class X]),
			Iif([Date].[Calendar].CurrentMember IS [Date].[Calendar].[Y], Count([City: Class Y]),
			Iif([Date].[Calendar].CurrentMember IS [Date].[Calendar].[Z], Count([City: Class Z]),
			COUNT([City: Source Set])))), CAPTION = 'Count Members in XYZ Class'
			MEMBER [Measures].[Count Members in ABC-XYZ Class] AS Iif([Date].[Calendar].CurrentMember IS [Date].[Calendar].[X],
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[A], Count(Intersect([City: Class X], [City: Class A])),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[B], Count(Intersect([City: Class X], [City: Class B])),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[C], Count(Intersect([City: Class X], [City: Class C])),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[D], Count(Intersect([City: Class X], [City: Class D])),
			Count([City: Class X]))))),
			Iif([Date].[Calendar].CurrentMember IS [Date].[Calendar].[Y],
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[A], Count(Intersect([City: Class Y], [City: Class A])),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[B], Count(Intersect([City: Class Y], [City: Class B])),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[C], Count(Intersect([City: Class Y], [City: Class C])),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[D], Count(Intersect([City: Class Y], [City: Class D])),
			Count([City: Class Y]))))),
			Iif([Date].[Calendar].CurrentMember IS [Date].[Calendar].[Z],
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[A], Count(Intersect([City: Class Z], [City: Class A])),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[B], Count(Intersect([City: Class Z], [City: Class B])),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[C], Count(Intersect([City: Class Z], [City: Class C])),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[D], Count(Intersect([City: Class Z], [City: Class D])),
			Count([City: Class Z]))))),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[A], Count(Intersect([City: Source Set], [City: Class A])),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[B], Count(Intersect([City: Source Set], [City: Class B])),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[C], Count(Intersect([City: Source Set], [City: Class C])),
			Iif([Customer].[Customer Geography].CurrentMember IS [Customer].[Customer Geography].[D], Count(Intersect([City: Source Set], [City: Class D])),
			Count([City: Source Set])))))))), CAPTION = 'Count Members in ABC-XYZ Class'
			MEMBER [Measures].[% Count Members in XYZ Class] AS Round(([Date].[Calendar].CurrentMember, [Measures].[Count Members in XYZ Class])/([Date].[Calendar].[GRAND TOTAL], [Measures].[Count Members in XYZ Class]), 4), CAPTION = '% Count Members in XYZ Class', FORMAT_STRING = "Percent"
			MEMBER [Measures].[% Count Members in ABC-XYZ Class] AS Round(([Date].[Calendar].CurrentMember, [Customer].[Customer Geography].CurrentMember, [Measures].[Count Members in ABC-XYZ Class])/([Date].[Calendar].[GRAND TOTAL], [Customer].[Customer Geography].[GRAND TOTAL], [Measures].[Count Members in ABC-XYZ Class]), 4), CAPTION = '% Count Members in ABC-XYZ Class', FORMAT_STRING = "Percent"
			MEMBER [Measures].[Breakdown of XYZ Classes] AS Iif([Date].[Calendar].CurrentMember IS [Date].[Calendar].[X], Round(0.1, 4),
			Iif([Date].[Calendar].CurrentMember IS [Date].[Calendar].[Y], Round(0.25, 4),
			1)), CAPTION = 'Breakdown of XYZ Classes', FORMAT_STRING = "Percent"
			MEMBER [Customer].[Customer Geography].[Head 100, High] AS Max([City: Head 100], Iif(isNumeric([Measures].CurrentMember),[Measures].CurrentMember, null)), SOLVE_ORDER = 4
			MEMBER [Customer].[Customer Geography].[Head 100, Low] AS Min([City: Head 100], Iif(isNumeric([Measures].CurrentMember),[Measures].CurrentMember, null)), SOLVE_ORDER = 4
			MEMBER [Customer].[Customer Geography].[Head 100, Median] AS Median([City: Head 100], Iif(isNumeric([Measures].CurrentMember),[Measures].CurrentMember, null)), SOLVE_ORDER = 4
			MEMBER [Customer].[Customer Geography].[Head 100, Average] AS Avg([City: Head 100], Iif(isNumeric([Measures].CurrentMember),[Measures].CurrentMember, null)), SOLVE_ORDER = 4
			MEMBER [Customer].[Customer Geography].[Head 100, Std. Dev.] AS Stdev([City: Head 100], Iif(isNumeric([Measures].CurrentMember),[Measures].CurrentMember, null)), SOLVE_ORDER = 4
			MEMBER [Customer].[Customer Geography].[Head 100, Count] AS [City: Head 100].Count, SOLVE_ORDER = 4, FORMAT_STRING = "#"
			MEMBER [Customer].[Customer Geography].[Head 100, Total] AS Aggregate([City: Head 100], Iif(isNumeric([Measures].CurrentMember),[Measures].CurrentMember, null)), SOLVE_ORDER = 4
			MEMBER [Customer].[Customer Geography].[Others, outside "Head 100", High] AS Max([City: Others, outside Head 100], Iif(isNumeric([Measures].CurrentMember),[Measures].CurrentMember, null)), SOLVE_ORDER = 4
			MEMBER [Customer].[Customer Geography].[Others, outside "Head 100", Low] AS Min([City: Others, outside Head 100], Iif(isNumeric([Measures].CurrentMember),[Measures].CurrentMember, null)), SOLVE_ORDER = 4
			MEMBER [Customer].[Customer Geography].[Others, outside "Head 100", Median] AS Median([City: Others, outside Head 100], Iif(isNumeric([Measures].CurrentMember),[Measures].CurrentMember, null)), SOLVE_ORDER = 4
			MEMBER [Customer].[Customer Geography].[Others, outside "Head 100", Average] AS Avg([City: Others, outside Head 100], Iif(isNumeric([Measures].CurrentMember),[Measures].CurrentMember, null)), SOLVE_ORDER = 4
			MEMBER [Customer].[Customer Geography].[Others, outside "Head 100", Std. Dev.] AS Stdev([City: Others, outside Head 100], Iif(isNumeric([Measures].CurrentMember),[Measures].CurrentMember, null)), SOLVE_ORDER = 4
			MEMBER [Customer].[Customer Geography].[Others, outside "Head 100", Count] AS [City: Others, outside Head 100].Count, SOLVE_ORDER = 4, FORMAT_STRING = "#"
			MEMBER [Customer].[Customer Geography].[Others, outside "Head 100", Total] AS Aggregate([City: Others, outside Head 100], Iif(isNumeric([Measures].CurrentMember),[Measures].CurrentMember, null)), SOLVE_ORDER = 4
			SET [City: Settings of Measures of Template] AS {[Customer].[Customer Geography].[Weight of Measure],[Customer].[Customer Geography].[Normative of Measure],[Customer].[Customer Geography].[Trend of Measure]}
			SET [Measures: Report Template Measures] AS {[Measures].[Internet Sales Amount],[Measures].[Internet Tax Amount]}
			SET [City: Source Set] AS {Filter([Customer].[Customer Geography].[City].Members, NOT (IsEmpty([Measures].[Internet Sales Amount]) AND ([Measures].[Internet Sales Amount] = 0)))}
			SET [City: Rank by Internet Sales Amount] AS {Order(Filter([Customer].[Customer Geography].[City].Members, NOT (IsEmpty([Measures].[Internet Sales Amount]) AND ([Measures].[Internet Sales Amount] = 0))), [Measures].[Internet Sales Amount], BDesc)}
			SET [Measures: Rank by Internet Sales Amount] AS {{ [Measures].[Internet Sales Amount], [Measures].[% Of Max, Internet Sales Amount], [Measures].[% Of Top Total, Internet Sales Amount], [Measures].[% Of Grand total, Internet Sales Amount], [Measures].[Rank, Internet Sales Amount], [Measures].[ABC*, Internet Sales Amount], [Measures].[ABC, Internet Sales Amount] }}
			SET [Measures: XYZ Indicators by Internet Sales Amount] AS {{ [Measures].[Internet Sales Amount], [Measures].[Std. Dev., Internet Sales Amount], [Measures].[Mean, Internet Sales Amount], [Measures].[% CV, Internet Sales Amount], [Measures].[XYZ, Internet Sales Amount] }}
			SET [Calendar Quarter*Indicators of ABC-XYZ Analysis, Internet Sales Amount] AS {CrossJoin({[Date].[Calendar].[ABC-XYZ Analysis Indicators, Internet Sales Amount]}, UNION([Measures: XYZ Indicators by Internet Sales Amount], {[Measures].[ABC, Internet Sales Amount], [Measures].[ABC-XYZ Combined, Internet Sales Amount], [Measures].[Sparklile Internet Sales Amount by Periods]}))}
			SET [City: Rank by Internet Tax Amount] AS {Order(Filter([Customer].[Customer Geography].[City].Members, NOT (IsEmpty([Measures].[Internet Tax Amount]) AND ([Measures].[Internet Tax Amount] = 0))), [Measures].[Internet Tax Amount], BDesc)}
			SET [Measures: Rank by Internet Tax Amount] AS {{ [Measures].[Internet Tax Amount], [Measures].[% Of Max, Internet Tax Amount], [Measures].[% Of Top Total, Internet Tax Amount], [Measures].[% Of Grand total, Internet Tax Amount], [Measures].[Rank, Internet Tax Amount], [Measures].[ABC*, Internet Tax Amount], [Measures].[ABC, Internet Tax Amount] }}
			SET [Measures: XYZ Indicators by Internet Tax Amount] AS {{ [Measures].[Internet Tax Amount], [Measures].[Std. Dev., Internet Tax Amount], [Measures].[Mean, Internet Tax Amount], [Measures].[% CV, Internet Tax Amount], [Measures].[XYZ, Internet Tax Amount] }}
			SET [Calendar Quarter*Indicators of ABC-XYZ Analysis, Internet Tax Amount] AS {CrossJoin({[Date].[Calendar].[ABC-XYZ Analysis Indicators, Internet Tax Amount]}, UNION([Measures: XYZ Indicators by Internet Tax Amount], {[Measures].[ABC, Internet Tax Amount], [Measures].[ABC-XYZ Combined, Internet Tax Amount], [Measures].[Sparklile Internet Tax Amount by Periods]}))}
			SET [Calendar Quarter*Indicators of ABC-XYZ Analysis] AS {{[Calendar Quarter*Indicators of ABC-XYZ Analysis, Internet Sales Amount] + [Calendar Quarter*Indicators of ABC-XYZ Analysis, Internet Tax Amount], CrossJoin([Calendar Quarter: Time series], { [Measures].[Internet Sales Amount], [Measures].[PrevP Var., Internet Sales Amount], [Measures].[PoP Var., Internet Sales Amount] } + { [Measures].[Internet Tax Amount], [Measures].[PrevP Var., Internet Tax Amount], [Measures].[PoP Var., Internet Tax Amount] })}}
			SET [Measures: All Report Indicators] AS {UNION({[Measures].[Hamburg Score, %], [Measures].[Wighted estimate], [Measures].[Rating on Ranks], [Measures].[ABC Combined]}, [Measures: Rank by Internet Sales Amount], [Measures: Rank by Internet Tax Amount])}
			SET [Calendar Quarter: Time series] AS {Filter([Date].[Calendar].[Calendar Quarter].Members, NOT (IsEmpty([Measures].[Internet Sales Amount]) AND ([Measures].[Internet Sales Amount] = 0)))}
			SET [City: Class A] AS {TopPercent([City: Source Set], 80, [Measures].[Internet Sales Amount])}
			SET [City: Class B] AS {TopPercent([City: Source Set], 95, [Measures].[Internet Sales Amount]) - [City: Class A]}
			SET [City: Class C] AS {[City: Source Set] - [City: Class A] - [City: Class B]}
			SET [City: Class D] AS {{}}
			SET [City: Statistics ABC Classes] AS {{ [Customer].[Customer Geography].[A], [Customer].[Customer Geography].[B], [Customer].[Customer Geography].[C], [Customer].[Customer Geography].[D], [Customer].[Customer Geography].[GRAND TOTAL] }}
			SET [Measures: Statistics ABC Classes] AS {{ [Measures].[Internet Sales Amount], [Measures].[% of Total ABC Class], [Measures].[Count Members in ABC Class], [Measures].[% Count Members in ABC Class], [Measures].[Breakdown of ABC Classes] }}
			SET [City: Class X] AS {Filter(Order([City: Source Set], [Measures].[Internet Sales Amount], BDesc), [Measures].[% CV, Internet Tax Amount] <= 0.1)}
			SET [City: Class Y] AS {Filter(Order([City: Source Set], [Measures].[Internet Sales Amount], BDesc) - [City: Class X], [Measures].[% CV, Internet Tax Amount] <= 0.25)}
			SET [City: Class Z] AS {Order([City: Source Set], [Measures].[Internet Sales Amount], BDesc) - [City: Class X] - [City: Class Y]}
			SET [Calendar Quarter: Statistics XYZ Classes] AS {{ [Date].[Calendar].[X], [Date].[Calendar].[Y], [Date].[Calendar].[Z], [Date].[Calendar].[GRAND TOTAL] }}
			SET [Measures: Statistics XYZ Classes] AS {{ [Measures].[Internet Sales Amount], [Measures].[% of Total XYZ Class], [Measures].[Count Members in XYZ Class], [Measures].[% Count Members in XYZ Class], [Measures].[Breakdown of XYZ Classes] }}
			SET [Measures: Statistics ABC-XYZ Classes] AS {{ [Measures].[Internet Sales Amount], [Measures].[% of Total ABC-XYZ Class], [Measures].[Count Members in ABC-XYZ Class], [Measures].[% Count Members in ABC-XYZ Class] }}
			SET [City: Head 100] AS {Head(Order([City: Source Set], [Measures].[Internet Sales Amount], BDesc), 100)}
			SET [City: Others, outside Head 100] AS {Except(Order([City: Source Set], [Measures].[Internet Sales Amount], BDesc), [City: Head 100])}
			SET [City: Source Set, Aggregate Members] AS {{[Customer].[Customer Geography].[Head 100, High], [Customer].[Customer Geography].[Head 100, Low], [Customer].[Customer Geography].[Head 100, Median], [Customer].[Customer Geography].[Head 100, Average], [Customer].[Customer Geography].[Head 100, Std. Dev.], [Customer].[Customer Geography].[Head 100, Count], [Customer].[Customer Geography].[Head 100, Total]}, Iif([City: Others, outside Head 100].Count > 0, {[Customer].[Customer Geography].[Others, outside "Head 100", High], [Customer].[Customer Geography].[Others, outside "Head 100", Low], [Customer].[Customer Geography].[Others, outside "Head 100", Median], [Customer].[Customer Geography].[Others, outside "Head 100", Average], [Customer].[Customer Geography].[Others, outside "Head 100", Std. Dev.], [Customer].[Customer Geography].[Others, outside "Head 100", Count], [Customer].[Customer Geography].[Others, outside "Head 100", Total]}, {}), [Customer].[Customer Geography].[GRAND TOTAL]}
			SET [City: Head 100 and Others] AS {[City: Head 100], [City: Source Set, Aggregate Members]}
			SELECT
			NON EMPTY [Calendar Quarter*Indicators of ABC-XYZ Analysis] DIMENSION PROPERTIES PARENT_UNIQUE_NAME, HIERARCHY_UNIQUE_NAME, CUSTOM_ROLLUP, UNARY_OPERATOR, KEY0, MEMBER_TYPE ON 0,
			NON EMPTY [City: Head 100 and Others] DIMENSION PROPERTIES PARENT_UNIQUE_NAME, HIERARCHY_UNIQUE_NAME, CUSTOM_ROLLUP, UNARY_OPERATOR, KEY0, MEMBER_TYPE ON 1
			 FROM
			[Adventure Works]
			CELL PROPERTIES BACK_COLOR, CELL_ORDINAL, FORE_COLOR, FONT_NAME, FONT_SIZE, FONT_FLAGS, FORMAT_STRING, VALUE, FORMATTED_VALUE, UPDATEABLE, ACTION_TYPE
			            """;

	@Test
	void testTypeSelectStatement() throws MdxParserException {

		MdxStatement clause = new MdxParserWrapper(MDX).parseMdxStatement();
		assertThat(clause).isNotNull().isInstanceOf(SelectStatementR.class);

	}
}
