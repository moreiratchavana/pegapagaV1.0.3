package heis.berg.pega_paga.dto;

public record AdminSummaryResponse(
        long totalUsers,
        long totalCustomers,
        long totalOperators,
        long totalPointsOfSale,
        long totalInvoices,
        long totalTransactions
) {
}
