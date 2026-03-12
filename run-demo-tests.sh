don#!/bin/bash

# 🚀 Demo Death Case Test Runner
# This script runs the demo death case tests

echo "🎯 DEMO DEATH CASE TEST SUITE"
echo "═══════════════════════════════════════════"
echo ""

echo "📋 Running Entity Tests (DeathCaseDemoTest)..."
echo "─────────────────────────────────────────────"
mvn test -Dtest=DeathCaseDemoTest -q

echo ""
echo "🔧 Running Integration Tests (DeathCaseDemoIntegrationTest)..."
echo "─────────────────────────────────────────────────────────────"
mvn test -Dtest=DeathCaseDemoIntegrationTest -q

echo ""
echo "💾 Running Demo Data Creator (DemoDeathCaseCreator)..."
echo "───────────────────────────────────────────────────────"
mvn test -Dtest=DemoDeathCaseCreator -q

echo ""
echo "✅ All demo death case tests completed!"
echo "═══════════════════════════════════════════"
