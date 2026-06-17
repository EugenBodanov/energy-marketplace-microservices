locals {
  common_labels = merge(
    {
      app         = "energy-marketplace"
      environment = var.environment
      managed_by  = "terraform"
    },
    var.labels
  )

  required_apis = toset([
    "artifactregistry.googleapis.com",
    "cloudresourcemanager.googleapis.com",
    "compute.googleapis.com",
    "iam.googleapis.com",
    "iamcredentials.googleapis.com",
    "run.googleapis.com",
    "secretmanager.googleapis.com",
    "servicenetworking.googleapis.com",
    "serviceusage.googleapis.com",
    "sqladmin.googleapis.com",
    "storage.googleapis.com"
  ])

  deployer_project_roles = toset([
    "roles/artifactregistry.writer",
    "roles/cloudsql.admin",
    "roles/compute.admin",
    "roles/iam.serviceAccountUser",
    "roles/run.admin",
    "roles/secretmanager.admin",
    "roles/servicenetworking.networksAdmin",
    "roles/serviceusage.serviceUsageAdmin",
    "roles/storage.admin"
  ])

  runtime_service_accounts = {
    "api-gateway" = {
      account_id   = "em-${var.environment}-api"
      display_name = "Energy Marketplace ${var.environment} API Gateway"
    }
    "user-service" = {
      account_id   = "em-${var.environment}-user"
      display_name = "Energy Marketplace ${var.environment} User Service"
    }
    "listing-service" = {
      account_id   = "em-${var.environment}-listing"
      display_name = "Energy Marketplace ${var.environment} Listing Service"
    }
    "trade-service" = {
      account_id   = "em-${var.environment}-trade"
      display_name = "Energy Marketplace ${var.environment} Trade Service"
    }
    "billing-service" = {
      account_id   = "em-${var.environment}-billing"
      display_name = "Energy Marketplace ${var.environment} Billing Service"
    }
  }
}
