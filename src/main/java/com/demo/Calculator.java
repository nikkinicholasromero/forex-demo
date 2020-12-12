package com.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class Calculator {
    public static void main(String[] args) {
        CalculationInput calculationInput = new CalculationInput();
        calculationInput.setBalance(new BigDecimal("104513.58"));
        calculationInput.setSpotPosition(new BigDecimal("1.21249"));

        Position position0 = new Position(PositionType.BUY, new BigDecimal("1.21420"), new BigDecimal("5"));
        Position position1 = new Position(PositionType.BUY, new BigDecimal("1.21421"), new BigDecimal("10"));
        Position position2 = new Position(PositionType.BUY, new BigDecimal("1.21563"), new BigDecimal("10"));
        calculationInput.setPositionList(Arrays.asList(position0, position1, position2));

        CalculationResult calculateAccount = calculateAccount(calculationInput);

        System.out.println(calculationInput);
        System.out.println(calculateAccount);
    }

    public static CalculationResult calculateAccount(CalculationInput calculationInput) {
        BigDecimal totalProfit = calculateTotalProfit(calculationInput.getPositionList(), calculationInput.getSpotPosition());
        BigDecimal equity = calculateEquity(calculationInput.getBalance(), totalProfit);
        BigDecimal totalMargin = calculateTotalMarginValue(calculationInput.getPositionList());
        BigDecimal freeMargin = calculateFreeMargin(equity, totalMargin);
        BigDecimal marginLevel = calculateMarginLevel(equity, totalMargin);

        CalculationResult calculationResult = new CalculationResult();
        calculationResult.setTotalProfit(totalProfit);
        calculationResult.setEquity(equity);
        calculationResult.setTotalMargin(totalMargin);
        calculationResult.setFreeMargin(freeMargin);
        calculationResult.setMarginLevel(marginLevel);
        return calculationResult;
    }

    public static BigDecimal calculateTotalProfit(List<Position> positionList, BigDecimal spotPosition) {
        BigDecimal totalProfit = new BigDecimal(0);

        for (Position position : positionList) {
            totalProfit = totalProfit.add(calculatePositionProfit(position, spotPosition));
        }

        return totalProfit;
    }

    public static BigDecimal calculatePositionProfit(Position position, BigDecimal spotPosition) {
        BigDecimal positionProfit = position.getOpenPosition()
                .subtract(spotPosition)
                .multiply(new BigDecimal(100_000))
                .multiply(position.getLotSize())
                .setScale(2, RoundingMode.HALF_UP);

        if (position.getPositionType() == PositionType.BUY) {
            return positionProfit.multiply(new BigDecimal(-1));
        }

        return positionProfit;
    }

    public static BigDecimal calculateEquity(BigDecimal balance, BigDecimal profit) {
        return balance
                .add(profit)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateTotalMarginValue(List<Position> positionList) {
        BigDecimal totalMargin = new BigDecimal(0);

        for (Position position : positionList) {
            totalMargin = totalMargin.add(calculateMarginValue(position));
        }

        return totalMargin;
    }

    public static BigDecimal calculateMarginValue(Position position) {
        return position.getOpenPosition()
                .multiply(position.getLotSize())
                .multiply(new BigDecimal(1_000))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateFreeMargin(BigDecimal equity, BigDecimal margin) {
        return equity
                .subtract(margin)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateMarginLevel(BigDecimal equity, BigDecimal margin) {
        MathContext mc = new MathContext(6, RoundingMode.HALF_UP);
        return equity
                .divide(margin, mc)
                .multiply(new BigDecimal(100))
                .setScale(2, RoundingMode.HALF_UP);

    }

    public static String formatCurrency(BigDecimal value) {
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        return formatter.format(value);
    }

    public static String formatPosition(BigDecimal value) {
        DecimalFormat formatter = new DecimalFormat("#,###.00000");
        return formatter.format(value);
    }
}

@Data
@AllArgsConstructor
class Position {
    private PositionType positionType;
    private BigDecimal openPosition;
    private BigDecimal lotSize;

    @Override
    public String toString() {
        String stringValue = "";
        stringValue += "Position Type : " + positionType + "\n";
        stringValue += "Opening Position : " + Calculator.formatPosition(openPosition) + "\n";
        stringValue += "Lot Size : " + Calculator.formatPosition(lotSize) + "\n";
        return stringValue;
    }
}

enum PositionType {
    BUY, SELL
}

@Getter
@Setter
class CalculationInput {
    private BigDecimal balance;
    private BigDecimal spotPosition;
    private List<Position> positionList;

    @Override
    public String toString() {
        String stringValue = "";
        stringValue += "Balance : " + Calculator.formatCurrency(balance) + "\n";
        stringValue += "Spot Position : " + Calculator.formatPosition(spotPosition) + "\n";
        stringValue += "Positions : " + positionList + "\n";
        return stringValue;
    }
}

@Getter
@Setter
class CalculationResult {
    private BigDecimal totalProfit;
    private BigDecimal equity;
    private BigDecimal totalMargin;
    private BigDecimal freeMargin;
    private BigDecimal marginLevel;

    @Override
    public String toString() {
        String stringValue = "";
        stringValue += "Equity : " + Calculator.formatCurrency(equity) + "\n";
        stringValue += "Total Profit : " + Calculator.formatCurrency(totalProfit) + "\n";
        stringValue += "Total Margin : " + Calculator.formatCurrency(totalMargin) + "\n";
        stringValue += "Free Margin : " + Calculator.formatCurrency(freeMargin) + "\n";
        stringValue += "Margin Level : " + Calculator.formatCurrency(marginLevel) + "\n";
        return stringValue;
    }
}
