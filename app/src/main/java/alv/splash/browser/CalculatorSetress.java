package alv.splash.browser;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class CalculatorSetress {
    private final Context context;

    public CalculatorSetress(Context context) {
        this.context = context;
    }

    public void showError(String message) {
        ((Activity) context).runOnUiThread(() ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        );
    }

    public String calculate(String input) {
        try {
            String processedInput = processInput(input);
            return evaluateExpression(processedInput);
        } catch (Exception e) {
            showError(e.getMessage()); // Tampilkan error ke user
            return "Error: " + e.getMessage();
        }
    }

    private String processInput(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Ekspresi kosong");
        }

        String sanitized = sanitizeInput(input);
        String processedE = processScientificNotation(sanitized);
        String normalized = normalizePercent(processedE);
        String withOperators = addImplicitOperators(normalized);
        validateInput(withOperators);

        return withOperators;
    }

    private String sanitizeInput(String input) {
        return input
                .replace('x', '*')
                .replace(':', '/')
                .replaceAll("\\s+", "");
    }

    private String processScientificNotation(String input) {
        // Handle notasi ilmiah (e.g., 3E5 -> 3*10^5, 2.5E-3 -> 2.5*10^-3)
        return input.replaceAll("([\\d.]+)E([+-]?\\d+)", "($1*10^$2)");
    }

    private String normalizePercent(String input) {
        return input.replaceAll("([\\d.]+)%", "($1/100)");
    }

    private String addImplicitOperators(String input) {
        return input
                .replaceAll("(?<=\\d)(\\()", "*$1")
                .replaceAll("(\\))(?=\\d|\\()", "$1*");
    }

    private void validateInput(String input) throws Exception {
        // Izinkan karakter ^ untuk pangkat
        if (!input.matches("^[\\d\\+\\-*/()\\.^]+$")) {
            throw new IllegalArgumentException("Karakter tidak valid");
        }

        validateParentheses(input);
    }

    private void validateParentheses(String input) throws Exception {
        int count = 0;
        for (char c : input.toCharArray()) {
            if (c == '(') count++;
            if (c == ')') count--;
            if (count < 0) break;
        }
        if (count != 0) {
            throw new IllegalArgumentException("Tanda kurung tidak seimbang");
        }
    }

    private String evaluateExpression(String input) throws Exception {
        try {
            Expression expression = new ExpressionBuilder(input).build();
            double result = expression.evaluate();

            if (Double.isInfinite(result)) {
                throw new ArithmeticException("Hasil tak terhingga");
            }
            if (Double.isNaN(result)) {
                throw new ArithmeticException("Hasil tidak valid");
            }

            return formatResult(result);
        } catch (ArithmeticException e) {
            throw new ArithmeticException("Perhitungan tidak valid: " + e.getMessage());
        } catch (Exception e) {
            throw new IllegalArgumentException("Ekspresi tidak dapat diproses: " + e.getMessage());
        }
    }

    private String formatResult(double result) {
        if (result % 1 == 0) {
            return String.valueOf((long) result);
        } else {
            // Hapus trailing zeros dan titik desimal jika perlu
            return String.valueOf(result)
                    .replaceAll("\\.?0+$", "");
        }
    }
}