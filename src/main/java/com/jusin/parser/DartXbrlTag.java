package com.jusin.parser;

import java.util.List;

public class DartXbrlTag {

    private DartXbrlTag() {}

    public static final List<String> REVENUE = List.of(
            "ifrs-full:Revenue",
            "dart:Revenue",
            "ifrs_full:Revenue",
            "us-gaap:Revenues"
    );

    public static final List<String> OPERATING_INCOME = List.of(
            "dart:OperatingIncomeLoss",
            "ifrs-full:ProfitLossFromOperatingActivities",
            "dart:OperatingProfit"
    );

    public static final List<String> NET_INCOME = List.of(
            "ifrs-full:ProfitLoss",
            "dart:ProfitLoss",
            "ifrs-full:ProfitLossAttributableToOwnersOfParent"
    );

    public static final List<String> SHARE_COUNT = List.of(
            "dart:IssuedCapitalSharesOfParentEntity",
            "ifrs-full:NumberOfSharesOutstanding",
            "dart:NumberOfSharesOutstanding"
    );

    public static final List<String> TOTAL_ASSETS = List.of(
            "ifrs-full:Assets",
            "dart:Assets"
    );

    public static final List<String> CURRENT_ASSETS = List.of(
            "ifrs-full:CurrentAssets",
            "dart:CurrentAssets"
    );

    public static final List<String> TOTAL_LIABILITIES = List.of(
            "ifrs-full:Liabilities",
            "dart:Liabilities"
    );

    public static final List<String> CURRENT_LIABILITIES = List.of(
            "ifrs-full:CurrentLiabilities",
            "dart:CurrentLiabilities"
    );

    public static final List<String> EQUITY = List.of(
            "ifrs-full:Equity",
            "dart:Equity",
            "ifrs-full:EquityAttributableToOwnersOfParent"
    );

    public static final List<String> OPERATING_CASH_FLOW = List.of(
            "ifrs-full:CashFlowsFromUsedInOperatingActivities",
            "dart:CashFlowsFromOperatingActivities"
    );
}
