# Pruebas unitarias

Las pruebas están en `src/test/java/pe/cibertec/gym`:

| Clase | Qué comprueba |

| `EnrollmentServiceTest` | Rechaza una inscripción duplicada del mismo miembro en una clase. |
| `ReportServiceTest` | Genera los PDF de miembros y de pagos. |
| `ApplicationSmokeTest` | La página de login es pública y el panel exige autenticación. |

## Cómo ejecutarlas
En Eclipse: clic derecho sobre `src/test/java` y `Run As > JUnit Test`. Con Maven: `mvn test`.

## Resultado de la ejecución
- Fecha: 23/10/2026
- Pruebas ejecutadas: 4
- Errores / fallos: 0
