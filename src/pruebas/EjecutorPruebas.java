package pruebas;

/**
 * Ejecuta las tres suites de pruebas del emulador MIPS.
 * Ejecucion: java pruebas.EjecutorPruebas
 */
public class EjecutorPruebas {

    public static void main(String[] args) {
        System.out.println("Plan de pruebas del emulador MIPS");
        System.out.println();

        int totalFallidas = 0;

        System.out.println("== Pruebas unitarias ==");
        totalFallidas += ejecutarSuite(PruebasUnitarias.class);

        System.out.println();
        System.out.println("== Pruebas de integracion ==");
        totalFallidas += ejecutarSuite(PruebasIntegracion.class);

        System.out.println();
        System.out.println("== Pruebas de sistema ==");
        totalFallidas += ejecutarSuite(PruebasSistema.class);

        System.out.println();
        if (totalFallidas == 0) {
            System.out.println("Resultado global: todas las pruebas pasaron");
        } else {
            System.out.println("Resultado global: " + totalFallidas + " suites con fallos");
        }
        System.exit(totalFallidas == 0 ? 0 : 1);
    }

    // Cada suite termina con System.exit, por eso se lanza como proceso.
    private static int ejecutarSuite(Class<?> clase) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    System.getProperty("java.home") + "/bin/java",
                    "-cp", System.getProperty("java.class.path"),
                    clase.getName());
            pb.inheritIO();
            Process p = pb.start();
            return p.waitFor() == 0 ? 0 : 1;
        } catch (Exception e) {
            System.err.println("No se pudo ejecutar " + clase.getSimpleName() + ": " + e);
            return 1;
        }
    }
}
