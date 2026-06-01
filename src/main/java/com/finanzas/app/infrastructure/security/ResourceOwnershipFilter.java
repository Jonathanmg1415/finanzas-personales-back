package com.finanzas.app.infrastructure.security;

import com.finanzas.app.domain.model.Budget;
import com.finanzas.app.domain.model.Transaction;
import com.finanzas.app.domain.repository.BudgetRepository;
import com.finanzas.app.domain.repository.TransactionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ResourceOwnershipFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = jwtUtil.extractUserId(
                authHeader.substring(7)
        );

        // Transactions
        if (uri.matches("^/api/v1/transactions/\\d+$")
                && (method.equals("PUT") || method.equals("DELETE"))) {

            Long transactionId = extractId(uri);

            Transaction transaction = transactionRepository
                    .findById(transactionId)
                    .orElse(null);

            if (transaction == null ||
                    !transaction.getUser().getId().equals(userId)) {

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "No tienes permiso para modificar este recurso"
                );
                return;
            }
        }

        // Budgets
        if (uri.matches("^/api/v1/budgets/\\d+$")
                && method.equals("PUT")) {

            Long budgetId = extractId(uri);

            Budget budget = budgetRepository
                    .findById(budgetId)
                    .orElse(null);

            if (budget == null ||
                    !budget.getUser().getId().equals(userId)) {

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "No tienes permiso para modificar este recurso"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private Long extractId(String uri) {
        return Long.parseLong(
                uri.substring(uri.lastIndexOf("/") + 1)
        );
    }
}
