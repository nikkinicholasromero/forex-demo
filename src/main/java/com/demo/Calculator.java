package com.demo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class Calculator {
    public static void main(String[] args) {
        BigDecimal balance = new BigDecimal("104513.58");
        BigDecimal spotPosition = new BigDecimal("1.21249");

        Position position0 = new Position(PositionType.BUY, new BigDecimal("1.21420"), new BigDecimal("5"));
        Position position1 = new Position(PositionType.BUY, new BigDecimal("1.21421"), new BigDecimal("10"));
        Position position2 = new Position(PositionType.BUY, new BigDecimal("1.21563"), new BigDecimal("10"));

        List<Position> positionList = Arrays.asList(position0, position1, position2);

        BigDecimal totalProfit = calculateTotalProfit(positionList, spotPosition);
        BigDecimal equity = calculateEquity(balance, totalProfit);
        BigDecimal totalMargin = calculateTotalMarginValue(positionList);
        BigDecimal freeMargin = calculateFreeMargin(equity, totalMargin);
        BigDecimal marginLevel = calculateMarginLevel(equity, totalMargin);

        System.out.println("Balance : " + formatCurrency(balance));
        System.out.println("Equity : " + formatCurrency(equity));
        System.out.println("Free Margin : " + formatCurrency(freeMargin));
        System.out.println("Margin Level : " + formatCurrency(marginLevel));
        System.out.println("Margin : " + formatCurrency(totalMargin));
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

    private static String formatCurrency(BigDecimal value) {
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        return formatter.format(value);
    }
}

@Data
@AllArgsConstructor
class Position {
    private PositionType positionType;
    private BigDecimal openPosition;
    private BigDecimal lotSize;
}

enum PositionType {
    BUY, SELL
}