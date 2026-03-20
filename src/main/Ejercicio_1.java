// sub punto 2

public static long factorialIterativo(int n) {
    long acumulado = 1;
    for (int i = 2; i <= n; i++) {
        acumulado *= i;
    }
    return acumulado;
}

//subpunto 5
public static long factorialConCola(int n, long acumulado) {
    if (n <= 1) {
        return acumulado;
    }
    return factorialCola(n - 1, n * acumulado);
}