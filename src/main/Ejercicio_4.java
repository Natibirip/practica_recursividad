/// Fragmento A rediseñado

public static int potenciaRapida(int x, int n) {
    if (n == 0) return 1;

    int mitad = potenciaRapida(x, n / 2);

    if (n % 2 == 0) {
        return mitad * mitad;
    } else {// Si es impar
        return x * mitad * mitad;
    }
}

/// fragmento B Rediseñado
public static String invertirMejorado(String s) {
    char[] caracteres = s.toCharArray();
    invertirRecursivo(caracteres, 0, caracteres.length - 1);
    return new String(caracteres);
}

private static void invertirRecursivo(char[] arr, int inicio, int fin) {
    if (inicio >= fin) return;

    char temp = arr[inicio];
    arr[inicio] = arr[fin];
    arr[fin] = temp;

    invertirRecursivo(arr, inicio + 1, fin - 1);
}