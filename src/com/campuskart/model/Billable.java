package com.campuskart.model;

// ★ RUBRIC: Interfaces (part of the 1M inheritance mark)
//   Asg 11 : an interface is a contract — Receipt MUST provide both methods
//   Asg 7  : interfaces give Java the multiple "contract" capability that classes cannot
public interface Billable {

    double calculateTotal();

    String generateReceipt();
}
