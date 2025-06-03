#!/bin/bash

# Comercia Fintech Automation - Enterprise Deployment Script
# This script sets up the entire testing infrastructure

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
NAMESPACE="fintech-testing"
DOCKER_REGISTRY="comercia"
IMAGE_TAG="latest"
ENVIRONMENT="${ENVIRONMENT:-development}"

echo -e "${BLUE}🚀 Starting Comercia Fintech Automation Enterprise Deployment${NC}"
echo -e "${BLUE}Environment: ${ENVIRONMENT}${NC}"
echo "=================================================="

# Function to print status
print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Check prerequisites
check_prerequisites() {
    echo -e "${BLUE}🔍 Checking prerequisites...${NC}"
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed"
        exit 1
    fi
    print_status "Docker is installed"
    
    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed"
        exit 1
    fi
    print_status "Docker Compose is installed"
    
    # Check Java
    if ! command -v java &> /dev/null; then
        print_warning "Java is not installed - will use containerized version"
    else
        print_status "Java is installed"
    fi
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        print_warning "Maven is not installed - will use containerized version"
    else
        print_status "Maven is installed"
    fi
    
    # Check Kubernetes (optional)
    if command -v kubectl &> /dev/null; then
        print_status "Kubernetes CLI is available"
        export K8S_AVAILABLE=true
    else
        print_warning "Kubernetes CLI not found - K8s deployment will be skipped"
        export K8S_AVAILABLE=false
    fi
}

# Build Docker images
build_images() {
    echo -e "${BLUE}🏗️ Building Docker images...${NC}"
    
    # Build main test framework image
    docker build -t ${DOCKER_REGISTRY}/fintech-automation:${IMAGE_TAG} .
    print_status "Built main test framework image"
    
    # Tag for different environments
    if [ "$ENVIRONMENT" != "development" ]; then
        docker tag ${DOCKER_REGISTRY}/fintech-automation:${IMAGE_TAG} \
                   ${DOCKER_REGISTRY}/fintech-automation:${ENVIRONMENT}
        print_status "Tagged image for ${ENVIRONMENT}"
    fi
}

# Setup monitoring stack
setup_monitoring() {
    echo -e "${BLUE}📊 Setting up monitoring stack...${NC}"
    
    # Create monitoring directories
    mkdir -p monitoring/grafana/provisioning/dashboards
    mkdir -p monitoring/grafana/provisioning/datasources
    mkdir -p monitoring/prometheus
    mkdir -p monitoring/logstash/pipeline
    
    # Generate Grafana dashboard
    cat > monitoring/grafana/provisioning/dashboards/test-dashboard.json << 'EOF'
{
  "dashboard": {
    "id": null,
    "title": "Comercia Fintech Test Metrics",
    "tags": ["fintech", "testing"],
    "timezone": "browser",
    "panels": [
      {
        "title": "Test Execution Rate",
        "type": "stat",
        "targets": [
          {
            "expr": "rate(tests_completed_total[5m])",
            "legendFormat": "Tests/sec"
          }
        ]
      },
      {
        "title": "Pass Rate",
        "type": "stat",
        "targets": [
          {
            "expr": "tests_passed_total / (tests_passed_total + tests_failed_total) * 100",
            "legendFormat": "Pass Rate %"
          }
        ]
      },
      {
        "title": "Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "api_response_duration_seconds",
            "legendFormat": "API Response Time"
          }
        ]
      }
    ],
    "time": {
      "from": "now-1h",
      "to": "now"
    },
    "refresh": "5s"
  }
}
EOF
    
    print_status "Created Grafana dashboard"
}

# Deploy with Docker Compose
deploy_docker() {
    echo -e "${BLUE}🐳 Deploying with Docker Compose...${NC}"
    
    # Stop any existing containers
    docker-compose down 2>/dev/null || true
    
    # Start the full stack
    docker-compose up -d
    
    # Wait for services to be ready
    echo -e "${YELLOW}⏳ Waiting for services to be ready...${NC}"
    sleep 30
    
    # Check service health
    check_service_health
    
    print_status "Docker deployment completed"
}

# Deploy to Kubernetes
deploy_kubernetes() {
    if [ "$K8S_AVAILABLE" = "false" ]; then
        print_warning "Skipping Kubernetes deployment - kubectl not available"
        return
    fi
    
    echo -e "${BLUE}☸️ Deploying to Kubernetes...${NC}"
    
    # Create namespace
    kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -
    
    # Apply Kubernetes manifests
    kubectl apply -f k8s/ -n ${NAMESPACE}
    
    # Wait for deployments
    kubectl wait --for=condition=available --timeout=300s deployment/selenium-hub -n ${NAMESPACE}
    kubectl wait --for=condition=available --timeout=300s deployment/chrome-node -n ${NAMESPACE}
    
    print_status "Kubernetes deployment completed"
}

# Check service health
check_service_health() {
    echo -e "${BLUE}🏥 Checking service health...${NC}"
    
    # Check Selenium Grid
    if curl -s http://localhost:4444/wd/hub/status > /dev/null; then
        print_status "Selenium Grid is healthy"
    else
        print_error "Selenium Grid is not responding"
    fi
    
    # Check Grafana
    if curl -s http://localhost:3000 > /dev/null; then
        print_status "Grafana is healthy"
    else
        print_warning "Grafana is not responding"
    fi
    
    # Check Allure
    if curl -s http://localhost:5050 > /dev/null; then
        print_status "Allure server is healthy"
    else
        print_warning "Allure server is not responding"
    fi
    
    # Check Prometheus
    if curl -s http://localhost:9090 > /dev/null; then
        print_status "Prometheus is healthy"
    else
        print_warning "Prometheus is not responding"
    fi
}

# Run sample tests
run_sample_tests() {
    echo -e "${BLUE}🧪 Running sample tests...${NC}"
    
    if [ "$1" = "docker" ]; then
        docker-compose exec test-runner mvn clean test -Dtest="**/smoke/*Test" || true
    else
        mvn clean test -Dtest="**/smoke/*Test" || true
    fi
    
    print_status "Sample tests completed"
}

# Generate initial reports
generate_reports() {
    echo -e "${BLUE}📊 Generating initial reports...${NC}"
    
    # Generate Allure report
    if command -v allure &> /dev/null; then
        allure generate target/allure-results --clean -o target/allure-report
        print_status "Allure report generated"
    else
        print_warning "Allure CLI not found - using containerized version"
    fi
    
    # Generate test summary
    cat > deployment-summary.md << EOF
# Comercia Fintech Automation - Deployment Summary

## Deployment Information
- **Date**: $(date)
- **Environment**: ${ENVIRONMENT}
- **Docker Registry**: ${DOCKER_REGISTRY}
- **Image Tag**: ${IMAGE_TAG}

## Service Endpoints
- **Selenium Grid**: http://localhost:4444
- **Grafana Dashboard**: http://localhost:3000 (admin/fintech123)
- **Allure Reports**: http://localhost:5050
- **Prometheus**: http://localhost:9090
- **Kibana**: http://localhost:5601

## Quick Commands
\`\`\`bash
# Run all tests
docker-compose exec test-runner mvn clean test

# Run specific test suite
docker-compose exec test-runner mvn test -Dtest=UITestRunner

# View logs
docker-compose logs -f test-runner

# Scale Chrome nodes
docker-compose up -d --scale chrome-node=5

# Stop all services
docker-compose down
\`\`\`

## Monitoring
- **Grafana**: Real-time test metrics and dashboards
- **Prometheus**: Metrics collection and alerting
- **Allure**: Detailed test reports with screenshots
- **Kibana**: Log analysis and debugging

## Next Steps
1. Configure Slack/Teams notifications
2. Set up automated test schedules
3. Configure production environment
4. Implement custom dashboards
EOF
    
    print_status "Deployment summary generated"
}

# Cleanup function
cleanup() {
    echo -e "${BLUE}🧹 Cleaning up...${NC}"
    
    if [ "$1" = "all" ]; then
        docker-compose down -v
        docker system prune -f
        if [ "$K8S_AVAILABLE" = "true" ]; then
            kubectl delete namespace ${NAMESPACE} --ignore-not-found=true
        fi
        print_status "Full cleanup completed"
    else
        docker-compose down
        print_status "Basic cleanup completed"
    fi
}

# Main deployment function
main() {
    case "${1:-full}" in
        "prereq")
            check_prerequisites
            ;;
        "build")
            check_prerequisites
            build_images
            ;;
        "docker")
            check_prerequisites
            build_images
            setup_monitoring
            deploy_docker
            run_sample_tests docker
            generate_reports
            ;;
        "k8s")
            check_prerequisites
            build_images
            deploy_kubernetes
            ;;
        "test")
            run_sample_tests
            generate_reports
            ;;
        "cleanup")
            cleanup ${2:-basic}
            ;;
        "full"|*)
            check_prerequisites
            build_images
            setup_monitoring
            deploy_docker
            if [ "$K8S_AVAILABLE" = "true" ]; then
                deploy_kubernetes
            fi
            run_sample_tests docker
            generate_reports
            ;;
    esac
    
    echo "=================================================="
    echo -e "${GREEN}🎉 Deployment completed successfully!${NC}"
    echo -e "${BLUE}📖 Check deployment-summary.md for details${NC}"
    echo -e "${BLUE}🌐 Access services at:${NC}"
    echo "   - Selenium Grid: http://localhost:4444"
    echo "   - Grafana: http://localhost:3000"
    echo "   - Allure Reports: http://localhost:5050"
    echo "   - Prometheus: http://localhost:9090"
}

# Handle script interruption
trap 'echo -e "\n${RED}Deployment interrupted${NC}"; exit 1' INT TERM

# Run main function with arguments
main "$@"
