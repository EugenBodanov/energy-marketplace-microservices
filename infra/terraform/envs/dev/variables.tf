variable "project_id" {
  description = "Google Cloud project ID."
  type        = string
}

variable "region" {
  description = "Region for Cloud Run, Cloud SQL, and network resources."
  type        = string
  default     = "europe-west1"
}

variable "zone" {
  description = "Zone for the RabbitMQ VM."
  type        = string
  default     = "europe-west1-b"
}

variable "environment" {
  description = "Environment name used in resource names and labels."
  type        = string
  default     = "dev"

  validation {
    condition     = can(regex("^[a-z][a-z0-9-]{0,14}$", var.environment))
    error_message = "environment must start with a lowercase letter and contain only lowercase letters, numbers, and dashes; max length is 15."
  }
}

variable "service_images" {
  description = "Container image URIs keyed by Cloud Run service name."
  type        = map(string)
  default     = {}

  validation {
    condition = alltrue([
      for service_name in keys(var.service_images) :
      contains(["api-gateway", "user-service", "listing-service", "trade-service", "billing-service"], service_name)
    ])
    error_message = "service_images keys must be one of: api-gateway, user-service, listing-service, trade-service, billing-service."
  }
}

variable "placeholder_image" {
  description = "Fallback image for local Terraform plans before CI passes real image URIs."
  type        = string
  default     = "us-docker.pkg.dev/cloudrun/container/hello"
}

variable "runtime_service_account_emails" {
  description = "Optional service account email overrides keyed by Cloud Run service name. Defaults match bootstrap-created accounts."
  type        = map(string)
  default     = {}
}

variable "cloud_run_ingress" {
  description = "Ingress mode for Cloud Run services."
  type        = string
  default     = "INGRESS_TRAFFIC_ALL"
}

variable "require_authenticated_invocation" {
  description = "When true, only public_service_names get allUsers invoker. Callers must send Google ID tokens."
  type        = bool
  default     = false
}

variable "public_service_names" {
  description = "Services exposed to unauthenticated internet when require_authenticated_invocation is true."
  type        = set(string)
  default     = ["api-gateway"]

  validation {
    condition = alltrue([
      for service_name in var.public_service_names :
      contains(["api-gateway", "user-service", "listing-service", "trade-service", "billing-service"], service_name)
    ])
    error_message = "public_service_names must contain only known Cloud Run service names."
  }
}

variable "min_instance_count" {
  description = "Minimum Cloud Run instances per service."
  type        = number
  default     = 0
}

variable "max_instance_count" {
  description = "Maximum Cloud Run instances per service."
  type        = number
  default     = 2
}

variable "deletion_protection" {
  description = "Protect stateful resources and Cloud Run services from accidental Terraform deletion."
  type        = bool
  default     = false
}

variable "network_cidr" {
  description = "Primary CIDR range for the dev VPC subnet."
  type        = string
  default     = "10.42.0.0/24"
}

variable "cloud_sql_database_version" {
  description = "Cloud SQL PostgreSQL version."
  type        = string
  default     = "POSTGRES_15"
}

variable "cloud_sql_tier" {
  description = "Cloud SQL machine tier."
  type        = string
  default     = "db-f1-micro"
}

variable "cloud_sql_disk_size_gb" {
  description = "Cloud SQL disk size in GB."
  type        = number
  default     = 10
}

variable "cloud_sql_backup_enabled" {
  description = "Enable automated backups for the dev Cloud SQL instance."
  type        = bool
  default     = false
}

variable "rabbitmq_machine_type" {
  description = "Machine type for the RabbitMQ VM."
  type        = string
  default     = "e2-small"
}

variable "rabbitmq_disk_size_gb" {
  description = "Persistent disk size for RabbitMQ data."
  type        = number
  default     = 10
}

variable "rabbitmq_image" {
  description = "Docker image used by the RabbitMQ VM startup script."
  type        = string
  default     = "rabbitmq:3.13-management"
}

variable "rabbitmq_bootstrap_wait" {
  description = "Best-effort wait after RabbitMQ VM creation before deploying services that connect to RabbitMQ."
  type        = string
  default     = "90s"
}

variable "rabbitmq_username" {
  description = "RabbitMQ application username."
  type        = string
  default     = "energy"

  validation {
    condition     = can(regex("^[A-Za-z0-9_][A-Za-z0-9_-]{2,31}$", var.rabbitmq_username))
    error_message = "rabbitmq_username must be 3-32 characters and contain only letters, numbers, underscores, or dashes."
  }
}

variable "allow_iap_ssh" {
  description = "Allow SSH to the RabbitMQ VM through Google Cloud IAP."
  type        = bool
  default     = true
}

variable "secret_name_prefix" {
  description = "Prefix for Secret Manager secret IDs."
  type        = string
  default     = "energy-marketplace"
}

variable "jwt_expiration_minutes" {
  description = "JWT expiration used by user-service."
  type        = number
  default     = 60
}

variable "labels" {
  description = "Extra labels applied to supported resources."
  type        = map(string)
  default     = {}
}
