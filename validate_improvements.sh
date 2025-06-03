#!/bin/bash

# Script de validación para las mejoras del proyecto Comercia Fintech Automation

echo "🚀 Validando mejoras implementadas en el proyecto..."
echo "=================================================="

# Validar estructura del proyecto
echo "✅ 1. Validando estructura del proyecto..."
if [ -f "pom.xml" ]; then
    echo "   ✓ pom.xml encontrado"
else
    echo "   ❌ pom.xml no encontrado"
    exit 1
fi

if [ -f "src/test/resources/config.properties" ]; then
    echo "   ✓ config.properties encontrado"
else
    echo "   ❌ config.properties no encontrado"
    exit 1
fi

# Validar runners
echo "✅ 2. Validando test runners..."
RUNNERS=("TestRunner.java" "UITestRunner.java" "APITestRunner.java")
for runner in "${RUNNERS[@]}"; do
    if [ -f "src/test/java/com/comercia/fintech/runners/$runner" ]; then
        echo "   ✓ $runner encontrado"
    else
        echo "   ❌ $runner no encontrado"
    fi
done

# Validar features
echo "✅ 3. Validando feature files..."
if [ -f "src/test/resources/features/payment_ui.feature" ]; then
    echo "   ✓ payment_ui.feature encontrado"
else
    echo "   ❌ payment_ui.feature no encontrado"
fi

# Validar page objects
echo "✅ 4. Validando page objects..."
if [ -f "src/test/java/com/comercia/fintech/pageObjects/CheckoutPage.java" ]; then
    echo "   ✓ CheckoutPage.java encontrado"
else
    echo "   ❌ CheckoutPage.java no encontrado"
fi

# Intentar compilar el proyecto
echo "✅ 5. Intentando compilar el proyecto..."
if mvn clean compile -q; then
    echo "   ✓ Proyecto compilado correctamente"
else
    echo "   ❌ Error en la compilación"
fi

echo "=================================================="
echo "🎉 Validación completada!"

# Comandos útiles
echo ""
echo "📋 Comandos útiles para ejecutar las pruebas:"
echo "mvn test                                    # Todas las pruebas @Smoke"
echo "mvn test -Dtest=UITestRunner               # Solo pruebas UI"
echo "mvn test -Dtest=APITestRunner              # Solo pruebas API"
echo "mvn test -Dcucumber.filter.tags=\"@Positive\" # Solo pruebas positivas"
echo "mvn test -Dcucumber.filter.tags=\"@Negative\" # Solo pruebas negativas"
