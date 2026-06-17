variable "project_id" {
  description = "Google Cloud project ID."
  type        = string
}

variable "region" {
  description = "Cloud Run region."
  type        = string
  default     = "europe-west1"
}

variable "artifact_registry_location" {
  description = "Artifact Registry location for Docker images."
  type        = string
  default     = "europe-west1"
}

variable "artifact_registry_repository" {
  description = "Artifact Registry Docker repository ID."
  type        = string
  default     = "energy-marketplace"
}

variable "environment" {
  description = "Environment name used in labels and resource names."
  type        = string
  default     = "dev"

  validation {
    condition     = can(regex("^[a-z][a-z0-9-]{0,14}$", var.environment))
    error_message = "environment must start with a lowercase letter and contain only lowercase letters, numbers, and dashes; max length is 15."
  }
}

variable "github_repository" {
  description = "GitHub repository allowed to impersonate the deploy service account, in owner/repo format."
  type        = string

  validation {
    condition     = can(regex("^[^/]+/[^/]+$", var.github_repository))
    error_message = "github_repository must be in owner/repo format."
  }
}

variable "github_wif_pool_id" {
  description = "Workload Identity Pool ID for GitHub Actions."
  type        = string
  default     = "github-actions"
}

variable "github_wif_provider_id" {
  description = "Workload Identity Pool Provider ID for GitHub Actions."
  type        = string
  default     = "github"
}

variable "deploy_service_account_id" {
  description = "Service account used by GitHub Actions for deployments."
  type        = string
  default     = "em-github-deployer"
}

variable "labels" {
  description = "Extra labels applied to supported resources."
  type        = map(string)
  default     = {}
}
