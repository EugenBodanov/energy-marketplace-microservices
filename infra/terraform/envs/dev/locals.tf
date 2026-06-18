locals {
  prefix        = "em-${var.environment}"
  secret_prefix = "${var.secret_name_prefix}-${var.environment}"

  service_names = toset([
    "api-gateway",
    "user-service",
    "listing-service",
    "trade-service",
    "billing-service"
  ])

  common_labels = merge(
    {
      app         = "energy-marketplace"
      environment = var.environment
      managed_by  = "terraform"
    },
    var.labels
  )

  runtime_service_account_emails = merge(
    {
      "api-gateway"     = "em-${var.environment}-api@${var.project_id}.iam.gserviceaccount.com"
      "user-service"    = "em-${var.environment}-user@${var.project_id}.iam.gserviceaccount.com"
      "listing-service" = "em-${var.environment}-listing@${var.project_id}.iam.gserviceaccount.com"
      "trade-service"   = "em-${var.environment}-trade@${var.project_id}.iam.gserviceaccount.com"
      "billing-service" = "em-${var.environment}-billing@${var.project_id}.iam.gserviceaccount.com"
    },
    var.runtime_service_account_emails
  )

  sql_databases = {
    user = {
      database = "users_db"
      username = "user_service"
    }
    listing = {
      database = "listing_db"
      username = "listing_service"
    }
    trade = {
      database = "trade_db"
      username = "trade_service"
    }
    billing = {
      database = "billing_db"
      username = "billing_service"
    }
  }

  service_secret_env = {
    "api-gateway" = {}

    "user-service" = {
      APP_JWT_SECRET   = "app-jwt-secret"
      USER_DB_PASSWORD = "user-db-password"
      USER_DB_URL      = "user-db-url"
      USER_DB_USERNAME = "user-db-username"
    }

    "listing-service" = {
      LISTING_DB_PASSWORD      = "listing-db-password"
      LISTING_DB_URL           = "listing-db-url"
      LISTING_DB_USERNAME      = "listing-db-username"
      SPRING_RABBITMQ_HOST     = "rabbitmq-host"
      SPRING_RABBITMQ_PASSWORD = "rabbitmq-password"
      SPRING_RABBITMQ_PORT     = "rabbitmq-port"
      SPRING_RABBITMQ_USERNAME = "rabbitmq-username"
    }

    "trade-service" = {
      SPRING_RABBITMQ_HOST     = "rabbitmq-host"
      SPRING_RABBITMQ_PASSWORD = "rabbitmq-password"
      SPRING_RABBITMQ_PORT     = "rabbitmq-port"
      SPRING_RABBITMQ_USERNAME = "rabbitmq-username"
      TRADE_DB_PASSWORD        = "trade-db-password"
      TRADE_DB_URL             = "trade-db-url"
      TRADE_DB_USERNAME        = "trade-db-username"
    }

    "billing-service" = {
      DB_HOST           = "billing-db-host"
      DB_NAME           = "billing-db-name"
      DB_PASSWORD       = "billing-db-password"
      DB_PORT           = "billing-db-port"
      DB_USER           = "billing-db-user"
      RABBITMQ_HOST     = "rabbitmq-host"
      RABBITMQ_PASSWORD = "rabbitmq-password"
      RABBITMQ_PORT     = "rabbitmq-port"
      RABBITMQ_USER     = "rabbitmq-username"
    }
  }

  runtime_secret_ids = toset(distinct(flatten([
    for envs in values(local.service_secret_env) : values(envs)
  ])))

  runtime_secret_access = merge([
    for service_name, envs in local.service_secret_env : {
      for secret_id in distinct(values(envs)) : "${service_name}/${secret_id}" => {
        service_name = service_name
        secret_id    = secret_id
      }
    }
  ]...)

  public_cloud_run_services = var.require_authenticated_invocation ? var.public_service_names : local.service_names

  private_cloud_run_invokers = var.require_authenticated_invocation ? {
    "api-gateway-to-user" = {
      target_service = "user-service"
      caller_service = "api-gateway"
    }
    "api-gateway-to-listing" = {
      target_service = "listing-service"
      caller_service = "api-gateway"
    }
    "api-gateway-to-trade" = {
      target_service = "trade-service"
      caller_service = "api-gateway"
    }
    "trade-to-user" = {
      target_service = "user-service"
      caller_service = "trade-service"
    }
    "trade-to-billing" = {
      target_service = "billing-service"
      caller_service = "trade-service"
    }
  } : {}

  rabbitmq_network_tag = "${local.prefix}-rabbitmq"
}
